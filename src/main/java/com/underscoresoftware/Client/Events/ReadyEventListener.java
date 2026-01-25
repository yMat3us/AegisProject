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
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import reactor.util.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                Event.reply("**Agradeço Sua Requisição.**").setEphemeral(true).queue();
                isLink.loadItem(Identifier).subscribe(new AudioLoader(Event, Manager));
            }
            if(!Search.matches(Regex)) {
                Event.reply("**Agradeço Sua Requisição.**").setEphemeral(true).queue();
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

        if (Tracks == null || Index >= Tracks.size()) {
            Event.reply("Essa Seleção Expirou. Use /Play de Novo.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Track Chosen = Tracks.get(Index);

        TrackSelectionCache.remove(isUserId);

        Event.deferEdit().queue();

        isLink.loadItem(Chosen.getInfo().getUri()).subscribe(new AudioLoader(Event, Manager));
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