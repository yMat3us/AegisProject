package com.underscoresoftware.Services.Music;
import com.underscoresoftware.Client.Events.BotInitializer;
import com.underscoresoftware.Services.MyUserData;
import com.underscoresoftware.Structures.BotClient;
import com.underscoresoftware.Utils.ThumbnailUtil;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler {
    private final GuildMusicManager guildMusicManager;
    public final Queue<Track> queue = new LinkedList<>();

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
    }

    public void enqueue(Track track) {
        this.guildMusicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) {
                        this.startTrack(track);
                    } else {
                        this.queue.offer(track);
                    }
                },
                () -> {
                    this.startTrack(track);
                }
        );
    }

    public void enqueuePlaylist(List<Track> tracks) {
        this.queue.addAll(tracks);

        this.guildMusicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) {
                        this.startTrack(this.queue.poll());
                    }
                },
                () -> {
                    this.startTrack(this.queue.poll());
                }
        );
    }

    public void onTrackStart(Track track) {
        MyUserData isUserData = track.getUserData(MyUserData.class);
        long UserId = isUserData.isRequesterUser();
        long GuildId = isUserData.isRequesterGuild();
        long ChannelId = isUserData.isRequesterChannel();
        JDA JavaDiscordAPI = BotClient.GetJDA();
        String Artwork = track.getInfo().getArtworkUrl();


        if((Artwork == null || Artwork.isBlank()) && "youtube".equalsIgnoreCase(track.getInfo().getSourceName())) {
            Artwork = ThumbnailUtil.fetchBestYouTubeThumbnail(track.getInfo().getIdentifier());
        }

        EmbedBuilder Embed = new EmbedBuilder()
                .setDescription(
                            String.format("<:Seta_Esquerda:1464083592001880221> **| Nome da Musica: [%s](%s)**\n\n<:Seta_Esquerda:1464083592001880221> **| Pedido Por: `%s#%s`**\n<:Seta_Esquerda:1464083592001880221> **| Duração: `%s`**\n<:Seta_Esquerda:1464083592001880221> **| Canal: `%s`**", track.getInfo().getTitle(), track.getInfo().getUri(), JavaDiscordAPI.getUserById(UserId).getEffectiveName(), JavaDiscordAPI.getUserById(UserId).getDiscriminator(), formatTime(track.getInfo().getLength()), track.getInfo().getAuthor())
                )
                .setFooter(String.format("Autor: %s#%s", JavaDiscordAPI.getUserById(UserId).getEffectiveName(), JavaDiscordAPI.getUserById(UserId).getDiscriminator()), JavaDiscordAPI.getUserById(UserId).getAvatarUrl())
                .setImage(Artwork)
                .setAuthor("Tocando Agora...", null, JavaDiscordAPI.getGuildById(GuildId).getIconUrl())
                .setColor(new Color(0x006AFF));

        if(JavaDiscordAPI.getGuildById(GuildId).getTextChannelById(ChannelId) != null) {
            JavaDiscordAPI.getGuildById(GuildId).getTextChannelById(ChannelId).sendMessageEmbeds(Embed.build()).queue();
        }
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            final var nextTrack = this.queue.poll();

            System.out.println(nextTrack);

            if (nextTrack != null) {
                this.startTrack(nextTrack);
            }
        }
    }

    private void startTrack(Track track) {
        this.guildMusicManager.getLink().ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(50)
                        .subscribe()
        );
    }

    public static String formatTime(long isLavaMs) {
        long Seconds = (isLavaMs / 1000) % 60;
        long Minutes = (isLavaMs / (1000 * 60)) % 60;
        long Hours   = (isLavaMs / (1000 * 60 * 60)) % 24;

        String H = (Hours   < 10 ? "0" : "") + Hours;
        String M = (Minutes < 10 ? "0" : "") + Minutes;
        String S = (Seconds < 10 ? "0" : "") + Seconds;

        return H + ":" + M + ":" + S;
    }
}
