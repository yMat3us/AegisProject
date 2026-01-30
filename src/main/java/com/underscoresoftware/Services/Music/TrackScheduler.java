package com.underscoresoftware.Services.Music;
import com.underscoresoftware.Services.MyUserData;
import com.underscoresoftware.Structures.BotClient;
import com.underscoresoftware.Utils.ThumbnailUtil;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class TrackScheduler {
    private final GuildMusicManager guildMusicManager;
    public final Deque<Track> queue = new LinkedList<>();
    private final Deque<Track> history = new LinkedList<>();
    private Track current;
    private net.dv8tion.jda.api.entities.Message nowPlayingMessage;

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

        if(JavaDiscordAPI.getGuildById(GuildId).getTextChannelById(ChannelId) == null) return;
        if(nowPlayingMessage == null) {
            JavaDiscordAPI.getGuildById(GuildId)
                    .getTextChannelById(ChannelId)
                    .sendMessageEmbeds(Embed.build())
                    .addComponents(
                            ActionRow.of(
                                    Button.success("Button_1", Emoji.fromFormatted("<:Next:1465383700115230762>")),
                                    Button.success("Button_2", Emoji.fromFormatted("<:Pause:1465383864750047285>")),
                                    Button.success("Button_3", Emoji.fromFormatted("<:Play:1465383633195106509>")),
                                    Button.success("Button_4", Emoji.fromFormatted("<:Previous:1465383575267446857>")),
                                    Button.success("Button_5", Emoji.fromFormatted("<:Stop:1465384033579171870>"))
                            )
                    )
                    .queue(Message -> nowPlayingMessage = Message);
        } else {
            nowPlayingMessage.editMessageEmbeds(Embed.build()).queue();
        }
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (!endReason.getMayStartNext()) {
            try {
                clearNowPlayingMessage();
            } catch(ExceptionInInitializerError E) {
                System.out.println(E);
            }
            return;
        }

        Track Next = queue.poll();
        if (Next != null) {
            this.nextTrack();
        } else {
            clearNowPlayingMessage();
            current = null;
        }
    }

    public void nextTrack() {

        if (current != null) {
            history.push(current);
        }

        this.guildMusicManager.getPlayer().ifPresent(Player -> {
            MyUserData isUserData = Player.getTrack().getUserData(MyUserData.class);
            long GuildId = isUserData.isRequesterGuild();
            long ChannelId = isUserData.isRequesterChannel();
            JDA JavaDiscordAPI = BotClient.GetJDA();

            Track Next = this.queue.poll();

            if (Next != null) {
                this.startTrack(Next);
            } else {
                JavaDiscordAPI.getGuildById(GuildId).getTextChannelById(ChannelId).sendMessage("**Sem Músicas na Fila**").queue(Message -> Message.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
            }
        });
    }

    public void startTrack(Track track) {
        this.guildMusicManager.getLink().ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(100)
                        .setPosition(0L)
                        .subscribe()
        );
        current = track;
    }
    public void previousTrack() {

        if (current != null) {
            queue.offerFirst(current);
        }

        this.guildMusicManager.getPlayer().ifPresent(Player -> {
            MyUserData isUserData = Player.getTrack().getUserData(MyUserData.class);
            long GuildId = isUserData.isRequesterGuild();
            long ChannelId = isUserData.isRequesterChannel();
            JDA JavaDiscordAPI = BotClient.GetJDA();

            if (!history.isEmpty()) {
                Track Previous = history.pop();
                this.startTrack(Previous);
            } else {
                JavaDiscordAPI.getGuildById(GuildId).getTextChannelById(ChannelId).sendMessage("**Sem Músicas Anteriores**").queue(Message -> Message.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
            }
        });
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

    public void clearNowPlayingMessage() {
        if (nowPlayingMessage != null) {
            nowPlayingMessage.delete().queue(
                    success -> {},
                    error -> {}
            );
            nowPlayingMessage = null;
        }
    }
}
