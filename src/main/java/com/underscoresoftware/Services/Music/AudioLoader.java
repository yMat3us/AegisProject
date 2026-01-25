package com.underscoresoftware.Services.Music;
import com.underscoresoftware.Services.MyUserData;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private final IReplyCallback event;
    private final GuildMusicManager mngr;

    public AudioLoader(IReplyCallback event, GuildMusicManager mngr) {
        this.event = event;
        this.mngr = mngr;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();

        var isUserData = new MyUserData(event.getUser().getIdLong(), event.getGuild().getIdLong(), event.getChannel().getIdLong());

        track.setUserData(isUserData);

        this.mngr.scheduler.enqueue(track);

        final var trackTitle = track.getInfo().getTitle();

        event.reply("**Adicionado a Fila:** **" + trackTitle + "**\n**Pedida Por:** <@" + isUserData.isRequesterUser() + ">").queue();
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
        result.getTracks().forEach(isTrack -> {
            var isUserData = new MyUserData(event.getUser().getIdLong(), event.getGuild().getIdLong(), event.getChannel().getIdLong());

            isTrack.setUserData(isUserData);
        });
        event.reply("**Adicionadas** `" + trackCount + "` **Músicas à Fila de:" + result.getInfo().getName() + "!**").queue();

        this.mngr.scheduler.enqueuePlaylist(result.getTracks());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        final List<Track> tracks = result.getTracks()
                .stream()
                .limit(10)
                .toList();

        if (tracks.isEmpty()) {
            event.reply("No tracks found!").setEphemeral(true).queue();
            return;
        }

        StringSelectMenu.Builder Menu = StringSelectMenu.create("SelectSearch:" + event.getUser().getId());

        int index = 0;
        for (Track track : tracks) {
            String title = track.getInfo().getTitle();
            String author = track.getInfo().getAuthor();

            Menu.addOption(
                    title.length() > 100 ? title.substring(0, 97) + "..." : title,
                    String.valueOf(index),
                    author
            );

            index++;
        }

        TrackSelectionCache.put(event.getUser().getIdLong(), tracks);

        event.reply("🎵 Escolha a Música:")
                //.addComponents(ActionRow.of(Menu.build()))
                .queue();

        /*final Track firstTrack = tracks.get(0);

        var isUserData = new MyUserData(event.getUser().getIdLong(), event.getGuild().getIdLong(), event.getChannel().getIdLong());

        firstTrack.setUserData(isUserData);

        event.getChannel().sendMessage("**Adicionado a Fila:** **" + firstTrack.getInfo().getTitle() + "**\n**Pedida Por:** <@" + isUserData.isRequesterUser() + ">").queue();

        this.mngr.scheduler.enqueue(firstTrack);*/
    }

    @Override
    public void noMatches() {
        event.reply("No matches found for your input!").setEphemeral(true).queue();
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        event.reply("Failed to load track! " + result.getException().getMessage()).setEphemeral(true).queue();
    }
}
