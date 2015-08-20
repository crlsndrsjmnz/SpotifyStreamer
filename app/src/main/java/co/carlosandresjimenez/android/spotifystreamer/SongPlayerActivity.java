package co.carlosandresjimenez.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Carlos on 8/20/2015.
 */
public class SongPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_player_activity);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putString(SongPlayerFragment.TRACK_ID,
                    getIntent().getStringExtra(SongPlayerFragment.TRACK_ID));

            SongPlayerFragment fragment = new SongPlayerFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.song_player_container, fragment)
                    .commit();
        }
    }
}
