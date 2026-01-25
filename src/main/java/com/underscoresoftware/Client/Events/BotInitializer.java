package com.underscoresoftware.Client.Events;
import com.underscoresoftware.Structures.BotConfig;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;
import java.util.Optional;

public class BotInitializer {
    public static JDA JavaDiscordAPI;
    private static ReadyEventListener listener;
    private static final int SESSION_INVALID = 4006;

    public static void Start() {

        try {

            final LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(BotConfig.TOKEN));
            client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

            registerLavalinkListeners(client);
            registerLavalinkNodes(client);

            listener = new ReadyEventListener(client);

            /* --> Forma a Estrutura do BOT <-- */
            final var JavaDiscordAPI = JDABuilder.createLight(BotConfig.TOKEN)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_VOICE_STATES
                    )
                    .addEventListeners(listener)
                    .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(client))
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)
                    .enableCache(CacheFlag.VOICE_STATE)
                    .build()
                            .awaitReady();

            client.on(WebSocketClosedEvent.class).subscribe((event) -> {
                if (event.getCode() == SESSION_INVALID) {
                    final var guildId = event.getGuildId();
                    final var guild = JavaDiscordAPI.getGuildById(guildId);

                    if (guild == null) {
                        return;
                    }

                    final var connectedChannel = guild.getSelfMember().getVoiceState().getChannel();

                    if (connectedChannel == null) {
                        return;
                    }

                    JavaDiscordAPI.getDirectAudioController().reconnect(connectedChannel);
                }
            });

            System.out.println("O Bot Está Inicializando...");
        } catch(Exception Error) {
            Error.printStackTrace();
        }
    }
    private static void registerLavalinkNodes(LavalinkClient client) {
        List.of(
                client.addNode(
                        new NodeOptions.Builder()
                                .setName(BotConfig.lavalinkName)
                                .setServerUri("ws://" + BotConfig.lavalinkHost + ":" + BotConfig.lavalinkPort)
                                .setPassword(BotConfig.lavalinkPassword)
                                .build()
                )
        ).forEach((node) -> {
            node.on(TrackStartEvent.class).subscribe((event) -> {
                final LavalinkNode node1 = event.getNode();

                System.out.printf(
                        "%s: track started: %s %n",
                        node1.getName(),
                        event.getTrack().getInfo()
                );
            });
        });
    }
    private static void registerLavalinkListeners(LavalinkClient client) {
        client.on(ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            System.out.printf(
                    "Node '%s' is ready, session id is '%s'! %n",
                    node.getName(),
                    event.getSessionId()
            );
        });

        client.on(StatsEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            System.out.printf(
                    "Node '%s' has stats, current players: %s/%s (link count %s) %n",
                    node.getName(),
                    event.getPlayingPlayers(),
                    event.getPlayers(),
                    client.getLinks().size()
            );
        });

        client.on(TrackStartEvent.class).subscribe((event) -> {
            Optional.ofNullable(listener.musicManagers.get(event.getGuildId())).ifPresent(
                    (mng) -> mng.scheduler.onTrackStart(event.getTrack())
            );
        });

        client.on(TrackEndEvent.class).subscribe((event) -> {
            Optional.ofNullable(listener.musicManagers.get(event.getGuildId())).ifPresent(
                    (mng) -> mng.scheduler.onTrackEnd(event.getTrack(), event.getEndReason())
            );
        });

        client.on(EmittedEvent.class).subscribe((event) -> {
            if (event instanceof TrackStartEvent) {
                System.out.println("Is a track start event!");
            }

            if (event instanceof TrackEndEvent) {
                System.out.println("Is a track end event!");
            }

            final var node = event.getNode();

            System.out.printf(
                    "Node '%s' emitted event: %s %n",
                    node.getName(),
                    event
            );
        });
    }
}
