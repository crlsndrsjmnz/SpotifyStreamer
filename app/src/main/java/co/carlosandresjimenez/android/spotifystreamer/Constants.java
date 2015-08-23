package co.carlosandresjimenez.android.spotifystreamer;

/**
 * Created by carlosjimenez on 8/22/15.
 */
public class Constants {
    public interface ACTION {
        static String MAIN_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.main";
        static String PREV_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.prev";
        static String PLAY_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.play";
        static String PAUSE_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.pause";
        static String NEXT_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.next";
        static String STARTFOREGROUND_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.startforeground";
        static String STOPFOREGROUND_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.stopforeground";
        static String LOADSONG_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.loadsong";
    }

    public interface NOTIFICATION_ID {
        static int FOREGROUND_SERVICE = 101;
    }

    public interface INTENT_EXTRA_ID {
        static final String SONG_URL = "SONG_URL";
        static final String TRACK_LIST = "TRACK_LIST";
        static final String TRACK_CURRENT_POSITION = "TRACK_CURRENT_POSITION";

    }

    public interface FRAGMENT_ID {
        static final String SONGPLAYER_TAG = "SONGPLAYER_FRAGMENT_TAG";
    }
}
