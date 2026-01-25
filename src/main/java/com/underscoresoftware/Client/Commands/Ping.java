package com.underscoresoftware.Client.Commands;

import com.underscoresoftware.Client.Events.ReadyEventListener;
import com.underscoresoftware.Structures.Command;
import dev.arbjerg.lavalink.client.LavalinkClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Ping extends Command {
    public Ping(JDA Client) {
        super(Client);
        this.name = "ping";
        this.category = "Bot";
        this.description = "Comando Para Ver o Ping do Bot";
        this.usage = "ping";
        this.aliases = List.of("pong");
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent Event, Object Database, LavalinkClient ClientLavalink, ReadyEventListener Listener) {
        long gatewayPing = Event.getJDA().getGatewayPing();
        Event.reply("**| Ping do Bot: `" + gatewayPing + "MS`**").setEphemeral(true).queue();
    }
}