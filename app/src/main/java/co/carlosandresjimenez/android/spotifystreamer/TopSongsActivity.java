package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class TopSongsActivity extends AppCompatActivity implements TopSongsFragment.Callback {

    private final String LOG_TAG = TopSongsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getIntent() != null && getIntent().getExtras() != null &&
                getIntent().getExtras().containsKey(Constants.INTENT_EXTRA_ID.TRACK_LIST)) {

            Bundle arguments = new Bundle();
            arguments.putParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST,
                    getIntent().getParcelableArrayListExtra(Constants.INTENT_EXTRA_ID.TRACK_LIST));
            arguments.putString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME,
                    getIntent().getStringExtra(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME));

            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_detail_container, fragment)
                    .commit();
        } else if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_ID,
                    getIntent().getStringExtra(Constants.INTENT_EXTRA_ID.KEY_ARTIST_ID));
            arguments.putString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME,
                    getIntent().getStringExtra(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME));

            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_detail_container, fragment)
                    .commit();
        }
    }

    public void onSongSelected(ArrayList<ListItem> items, int songSelection) {
        // The device is using a small layout, so show the media player fragment as full screen
        Intent intent = new Intent(this, SongPlayerActivity.class);
        intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_ID.TRACK_LIST, items);
        intent.putExtra(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION, songSelection);
        startActivity(intent);
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
            startActivity(new Intent(this, SongPlayerActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
