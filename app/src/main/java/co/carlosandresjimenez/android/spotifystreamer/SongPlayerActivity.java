package co.carlosandresjimenez.android.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class SongPlayerActivity extends AppCompatActivity {

    private final String LOG_TAG = SongPlayerFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_player_activity);

        FragmentManager fm = getSupportFragmentManager();
        SongPlayerFragment songPlayerFragment = (SongPlayerFragment) fm.findFragmentByTag(Constants.FRAGMENT_ID.SONGPLAYER_TAG);

        if (songPlayerFragment == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Constants.INTENT_EXTRA_ID.TRACK_LIST)) {
                arguments.putParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST,
                        getIntent().getParcelableArrayListExtra(Constants.INTENT_EXTRA_ID.TRACK_LIST));
                arguments.putInt(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION,
                        getIntent().getIntExtra(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION, 0));
            }

            SongPlayerFragment fragment = new SongPlayerFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.song_player_container, fragment, Constants.FRAGMENT_ID.SONGPLAYER_TAG)
                    .commit();
        }
    }
}
