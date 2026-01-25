package com.underscoresoftware.Structures;

import net.dv8tion.jda.api.JDA;

public class BotClient {
    private static JDA JavaDiscordAPI;

    public static void SetJDA(JDA JDAInstance) { JavaDiscordAPI = JDAInstance; }

    public static JDA GetJDA() {
        if(JavaDiscordAPI == null) { throw new IllegalStateException("JDA Não Inicializado!"); }

        return JavaDiscordAPI;
    }
}
