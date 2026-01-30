package com.underscoresoftware.Client.Commands;
import com.underscoresoftware.Client.Events.ReadyEventListener;
import com.underscoresoftware.Structures.Command;
import dev.arbjerg.lavalink.client.LavalinkClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

import java.util.List;

public class Play extends Command {

    public Play(JDA Client) {
        super(Client);

        this.name = "play";
        this.category = "Música";
        this.description = "Comando Para Tocar Música";
        this.usage = "play <url|nome>";
        this.aliases = List.of("p");
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent Event, Object Database, LavalinkClient ClientLavalink, ReadyEventListener Listener) {
        Member isMember  = Event.getMember();

        if (isMember == null) {
            Event.reply("Esse Comando só Pode ser Usado em Servidores.").setEphemeral(true).queue();
            return;
        }

        GuildVoiceState isMemberVoiceState = isMember.getVoiceState();

        if(isMemberVoiceState == null || isMemberVoiceState.getChannel() == null || !isMemberVoiceState.inAudioChannel()) {
            Event.reply("Entre em um Canal de Voz.").setEphemeral(true).queue();
            return;
        }

        Member isSelf = Event.getGuild().getSelfMember();
        GuildVoiceState isSelfVoiceState = isSelf.getVoiceState();

        if(isSelfVoiceState == null || !isSelfVoiceState.inAudioChannel()) {
            Event.getGuild().getAudioManager().openAudioConnection(isMemberVoiceState.getChannel());
        } else {
            if(isSelfVoiceState.getChannel() != isMemberVoiceState.getChannel()) {
                Event.reply("Ambos Precisam Estar no Mesmo Canal.").setEphemeral(true).queue();
                return;
            }
        }

        if(Event.getName().equals("Play".toLowerCase())) {
            TextInput Search = TextInput.create("Search", TextInputStyle.SHORT)
                    .setPlaceholder("Digite o Nome ou URL da Música")
                    .setMinLength(1)
                    .setMaxLength(140)
                    .build();

            Modal ModalSearch = Modal.create("ModalSearch", "Tocar Música")
                    .addComponents(Label.of("Search/URL", Search))
                    .build();

            Event.replyModal(ModalSearch).queue();
        }
    }
}