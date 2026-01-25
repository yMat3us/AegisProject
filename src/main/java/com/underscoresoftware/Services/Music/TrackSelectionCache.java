package com.underscoresoftware.Services.Music;
import dev.arbjerg.lavalink.client.player.Track;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrackSelectionCache {

    private static final Map<Long, List<Track>> CACHE = new ConcurrentHashMap<>();

    public static void put(long userId, List<Track> tracks) {
        CACHE.put(userId, tracks);
    }

    public static List<Track> get(long userId) {
        return CACHE.get(userId);
    }

    public static void remove(long userId) {
        CACHE.remove(userId);
    }
}
