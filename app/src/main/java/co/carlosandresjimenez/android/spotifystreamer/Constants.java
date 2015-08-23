package co.carlosandresjimenez.android.spotifystreamer;

public class Constants {

    public interface NOTIFICATION_ACTION {
        String MAIN_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.main";
        String PREV_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.prev";
        String PLAY_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.play";
        String PAUSE_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.pause";
        String NEXT_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.next";
        String OPEN_PLAYER_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.open_player";
        String STARTFOREGROUND_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.startforeground";
        String STOPFOREGROUND_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.stopforeground";
        String LOADSONG_ACTION = "co.carlosandresjimenez.android.spotifystreamer.action.loadsong";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
        int PLAY_NOTIFICATION = 0;
        int PAUSE_NOTIFICATION = 1;

    }

    public interface INTENT_EXTRA_ID {
        String SONG_URL = "SONG_URL";
        String TRACK_LIST = "TRACK_LIST";
        String TRACK_CURRENT_POSITION = "TRACK_CURRENT_POSITION";
        String KEY_ARTIST_ID = "KEY_ARTIST_ID";
        String KEY_ARTIST_NAME = "KEY_ARTIST_NAME";
        String KEY_SONGS_VIEW_STATE = "KEY_SONGS_VIEW_STATE";
        String KEY_SONGS_LIST_POSITION = "KEY_SONGS_LIST_POSITION";
    }

    public interface FRAGMENT_ID {
        String SONGPLAYER_TAG = "SONGPLAYER_FRAGMENT_TAG";
        String DETAILFRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    }

    public interface STATE_ID {
        String KEY_ARTISTS_VIEW_STATE = "KEY_ARTISTS_VIEW_STATE";
        String KEY_ARTISTS_LIST_POSITION = "KEY_ARTISTS_LIST_POSITION";
    }

    public interface PLAYER_SERVICE {
        int SEEK_BAR_REFRESH_RATE = 100; // MILLISECONDS - 0.1 SECONDS
    }
}
