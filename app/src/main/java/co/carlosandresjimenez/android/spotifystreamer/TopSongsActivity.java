package co.carlosandresjimenez.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TopSongsActivity extends AppCompatActivity {

    private final String LOG_TAG = TopSongsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putString(TopSongsFragment.KEY_ARTIST_ID,
                    getIntent().getStringExtra(TopSongsFragment.KEY_ARTIST_ID));
            arguments.putString(TopSongsFragment.KEY_ARTIST_NAME,
                    getIntent().getStringExtra(TopSongsFragment.KEY_ARTIST_NAME));

            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_detail_container, fragment)
                    .commit();
        }
    }


}
