package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, TopSongsFragment.Callback {

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
                        .replace(R.id.artist_detail_container, new TopSongsFragment(), Constants.FRAGMENT_ID.DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        if (getIntent() != null && getIntent().getAction().equals(Constants.NOTIFICATION_ACTION.OPEN_PLAYER_ACTION))
            openPlayer();

    }

    @Override
    public void onArtistSelected(ListItem item) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_ID, item.getId());
            args.putString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME, item.getName());

            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment, Constants.FRAGMENT_ID.DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent topSongsIntent = new Intent(this, TopSongsActivity.class);
            topSongsIntent.putExtra(Constants.INTENT_EXTRA_ID.KEY_ARTIST_ID, item.getId());
            topSongsIntent.putExtra(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME, item.getName());
            startActivity(topSongsIntent);
        }
    }

    @Override
    public void onSongSelected(ArrayList<ListItem> items, int songSelection) {
        // The device is using a large layout, so show the media player fragment as a dialog
        Bundle arguments = new Bundle();

        if (items != null) {
            arguments.putParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST, items);
            arguments.putInt(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION, songSelection);
        }

        SongPlayerFragment fragment = new SongPlayerFragment();
        fragment.setArguments(arguments);
        fragment.show(getSupportFragmentManager(), Constants.FRAGMENT_ID.SONGPLAYER_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_player) {
            openPlayer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openPlayer() {
        if (mTwoPane) {
            onSongSelected(null, 0);
        } else
            startActivity(new Intent(this, SongPlayerActivity.class));
    }
}
