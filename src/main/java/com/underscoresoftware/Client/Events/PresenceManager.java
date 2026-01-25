package com.underscoresoftware.Client.Events;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PresenceManager {

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    /* --> Mensagens de Status do BOT <-- */
    private static final List<Activity> activities = List.of(
            Activity.playing("Defendendo Este Servidor"),
            Activity.watching("Atividades Suspeitas"),
            Activity.listening("Comandos Autorizados"),
            Activity.playing("Protocolos de Segurança"),
            Activity.watching("Cada Mensagem Enviada")
    );

    public static void Start(JDA jda) {
        scheduler.scheduleAtFixedRate(new Runnable() {
            private int index = 0;

            @Override
            public void run() {
                jda.getPresence().setActivity(activities.get(index));
                index = (index + 1) % activities.size();
            }
        }, 0, 15, TimeUnit.SECONDS); /* --> Troca a Cada 15 Segundos <-- */
    }
}
