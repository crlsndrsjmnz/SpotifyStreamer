package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, TopSongsFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    boolean mTwoPane;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.artist_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.artist_detail_container, new TopSongsFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onArtistSelected(ListItem item) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(TopSongsFragment.KEY_ARTIST_ID, item.getId());
            args.putString(TopSongsFragment.KEY_ARTIST_NAME, item.getName());

            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent topSongsIntent = new Intent(this, TopSongsActivity.class);
            topSongsIntent.putExtra(TopSongsFragment.KEY_ARTIST_ID, item.getId());
            topSongsIntent.putExtra(TopSongsFragment.KEY_ARTIST_NAME, item.getName());
            startActivity(topSongsIntent);
        }
    }

    @Override
    public void onSongSelected(ListItem item) {
        // The device is using a large layout, so show the media player fragment as a dialog
        Bundle arguments = new Bundle();
        arguments.putString(SongPlayerFragment.TRACK_ID, item.getId());

        SongPlayerFragment fragment = new SongPlayerFragment();
        fragment.setArguments(arguments);
        fragment.show(getSupportFragmentManager(), "dialog");
    }
}
