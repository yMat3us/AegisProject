package com.underscoresoftware.Structures;
import com.underscoresoftware.Client.Events.ReadyEventListener;
import dev.arbjerg.lavalink.client.LavalinkClient;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.JDA;
import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    protected JDA Client;
    public String name = "Nome";
    public String category = "Categoria";
    public String description = "Descrição";
    public String usage = "Como Usar";
    public List<String> aliases = new ArrayList<>();
    public boolean enabled = true;
    public boolean guildOnly = true;

    public Command(JDA Client) {
        this.Client = Client;
    }

    public abstract void runSlash(SlashCommandInteractionEvent Event, Object Database, LavalinkClient ClientLavalink, ReadyEventListener Listener);
}