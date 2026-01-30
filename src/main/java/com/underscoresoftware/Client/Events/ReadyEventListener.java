package com.underscoresoftware.Client.Events;
import com.underscoresoftware.Client.Commands.Ping;
import com.underscoresoftware.Client.Commands.Play;
import com.underscoresoftware.Services.Music.AudioLoader;
import com.underscoresoftware.Services.Music.GuildMusicManager;
import com.underscoresoftware.Services.Music.TrackSelectionCache;
import com.underscoresoftware.Structures.BotClient;
import com.underscoresoftware.Structures.Command;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import reactor.util.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReadyEventListener extends ListenerAdapter {
    private static final Map<String, Command> Commands = new HashMap<>();
    public final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    private final LavalinkClient client;

    public ReadyEventListener(LavalinkClient client) {
        this.client = client;
    }

    /* --> Evento Principal do BOT(Inicia Toda Estrutura) <-- */
    @Override
    public void onReady(ReadyEvent Event) {
        try {
            PresenceManager.Start(Event.getJDA()); /* --> Liga o Status do BOT <-- */
            BotClient.SetJDA(Event.getJDA());
            System.out.println("O Bot Está ONLINE!");
        } catch (Exception Error) {
            Error.printStackTrace();
        }

        registerCommand(new Ping(Event.getJDA()));
        registerCommand(new Play(Event.getJDA()));

        /* --> Estrutura Para Registrar Comandos Slash <-- */
        Event.getJDA().updateCommands().addCommands(
                Commands.values().stream()
                        .map(cmd -> net.dv8tion.jda.api.interactions.commands.build.Commands.slash(cmd.name, cmd.description))
                        .toList()
        ).queue();

    }
    /* --> Registra Comando Por Prefixo <-- */
    private static void registerCommand(Command CMD) {
        Commands.put(CMD.name, CMD);
    }
    /* --> Interação/Resposta de Comandos Slash <-- */
    public void onSlashCommandInteraction(SlashCommandInteractionEvent Event) {
        Command CMD = Commands.get(Event.getName());
        if (CMD != null && CMD.enabled) {
            CMD.runSlash(Event, null, this.client, this);
        }
    }
    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent Event) {
        if (Event.getModalId().equals("ModalSearch")) {
            String Search = Event.getValue("Search").getAsString();
            String Regex = "^(https?:\\/\\/)?(www\\.)?(youtube\\.com|youtu\\.be|open\\.spotify\\.com|music\\.apple\\.com)\\/.+$";
            String Identifier = Search.matches(Regex) ? Search : "ytsearch:" + Search;
            long isGuildId = Event.getGuild().getIdLong();
            Link isLink = this.client.getOrCreateLink(isGuildId);
            var Manager = this.getOrCreateMusicManager(Event.getGuild().getIdLong());

            if(Search.matches(Regex)) {
                Event.deferReply().queue();
                isLink.loadItem(Identifier).subscribe(new AudioLoader(Event, Manager));
            }
            if(!Search.matches(Regex)) {
                Event.deferReply().queue();
                isLink.loadItem(Identifier).subscribe(new AudioLoader(Event, Manager));
            }
        }
    }
    public void onStringSelectInteraction(StringSelectInteractionEvent Event) {
        if (!Event.getComponentId().startsWith("SelectSearch:")) return;
        long isUserId = Long.parseLong(Event.getComponentId().split(":")[1]);
        int Index = Integer.parseInt(Event.getValues().get(0));
        long isGuildId = Event.getGuild().getIdLong();
        Link isLink = this.client.getOrCreateLink(isGuildId);
        var Manager = this.getOrCreateMusicManager(Event.getGuild().getIdLong());

        List<Track> Tracks = TrackSelectionCache.get(isUserId);

        if (Event.getUser().getIdLong() != isUserId) {
            Event.getHook().sendMessage("Uso Exclusivo do Autor do Comando")
                    .setEphemeral(true)
                    .queue();
        }

        if (Tracks == null || Index >= Tracks.size()) {
            Event.getHook().sendMessage("Essa Seleção Expirou. Use /Play de Novo.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Track Chosen = Tracks.get(Index);

        TrackSelectionCache.remove(isUserId);

        Event.deferEdit().queue();
        Event.getHook()
                .editOriginalComponents(ActionRow.of(Event.getSelectMenu().asDisabled()))
                .queue();
        Event.getHook()
                .deleteOriginal()
                .queueAfter(15, TimeUnit.SECONDS);

        isLink.loadItem(Chosen.getInfo().getUri()).subscribe(new AudioLoader(Event, Manager));
    }
    public void onButtonInteraction(ButtonInteractionEvent Event) {
        if (Event.getComponentId().equals("Button_1")) {
            this.musicManagers.get(Event.getGuild().getIdLong()).scheduler.nextTrack();

            Event.deferEdit().queue();
        } else if (Event.getComponentId().equals("Button_2")) {
            this.client.getOrCreateLink(Event.getGuild().getIdLong())
                    .getPlayer()
                    .flatMap((Player) -> Player.setPaused(true))
                    .subscribe((Player) -> {
                        Event.reply("**Paused**").setEphemeral(true).queue();
                    });
        } else if (Event.getComponentId().equals("Button_3")) {
            this.client.getOrCreateLink(Event.getGuild().getIdLong())
                    .getPlayer()
                    .flatMap((Player) -> Player.setPaused(false))
                    .subscribe((Player) -> {
                        Event.reply("**Played**").setEphemeral(true).queue();
                    });
        } else if (Event.getComponentId().equals("Button_4")) {
            this.musicManagers.get(Event.getGuild().getIdLong()).scheduler.previousTrack();
            Event.deferEdit().queue();
        } else if (Event.getComponentId().equals("Button_5")) {
            this.getOrCreateMusicManager(Event.getGuild().getIdLong()).stop();
            Event.reply("**Stopped**").setEphemeral(true).queue();
        }
    }
    public GuildMusicManager getOrCreateMusicManager(long guildId) {
        synchronized(this) {
            var mng = this.musicManagers.get(guildId);

            if (mng == null) {
                mng = new GuildMusicManager(guildId, this.client);
                this.musicManagers.put(guildId, mng);
            }

            return mng;
        }
    }
    /*private void joinHelper(MessageReceivedEvent event) {
        final GuildVoiceState memberVoiceState = event.getMember().getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        }

        this.getOrCreateMusicManager(event.getMember().getGuild().getIdLong());

        event.getChannel().sendMessage("Joining your channel!").queue();
    }*/
}