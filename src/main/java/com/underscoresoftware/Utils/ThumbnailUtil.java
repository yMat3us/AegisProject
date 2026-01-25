package com.underscoresoftware.Utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ThumbnailUtil {
    private static final OkHttpClient client = new OkHttpClient();

    public static String fetchBestYouTubeThumbnail(String videoId) {
        String[] Qualities = {
                "maxresdefault.jpg",
                "sddefault.jpg",
                "hqdefault.jpg",
                "mqdefault.jpg",
                "default.jpg"
        };

        for (String Q : Qualities) {
            String Url = "https://img.youtube.com/vi/" + videoId + "/" + Q;
            Request Req = new Request.Builder().url(Url).head().build();

            try (Response res = client.newCall(Req).execute()) {
                if (res.isSuccessful()) {
                    return Url;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
