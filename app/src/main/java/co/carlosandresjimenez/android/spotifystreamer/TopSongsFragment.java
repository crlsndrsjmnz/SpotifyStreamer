package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by carlosjimenez on 7/11/15.
 */
public class TopSongsFragment extends Fragment {

    private final String LOG_TAG = TopSongsFragment.class.getSimpleName();

    private final String KEY_SONGS_VIEW_STATE = "KEY_SONGS_VIEW_STATE";
    private final String KEY_SONGS_LIST_POSITION = "KEY_SONGS_LIST_POSITION";

    @Bind(R.id.artist_top_songs_list)
    ListView listOfTopSongs;
    @Bind(R.id.songs_progress_bar)
    ProgressBar songsProgressBar;

    ArrayList<ListItem> mSongList;
    int songsListPosition = 0;
    private String mArtistId;
    private String mArtistName;
    private StreamerListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(KEY_SONGS_VIEW_STATE)) {
                mSongList = savedInstanceState.getParcelableArrayList(KEY_SONGS_VIEW_STATE);
            } else {
                mSongList = new ArrayList<>();
            }

            if (savedInstanceState.containsKey(KEY_SONGS_LIST_POSITION)) {
                songsListPosition = savedInstanceState.getInt(KEY_SONGS_LIST_POSITION);
            }
        } else {
            mSongList = new ArrayList<>();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.artist_detail, container, false);
        ButterKnife.bind(this, rootView);

        mArtistId = "";
        mArtistName = "";

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mListAdapter = new StreamerListAdapter(getActivity(), mSongList);

        if (listOfTopSongs != null) {
            listOfTopSongs.setAdapter(mListAdapter);
            listOfTopSongs.setSelection(songsListPosition);
        } else {
            Log.e(LOG_TAG, "ListOfTopSongs is NULL");
        }

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(MainActivityFragment.KEY_ARTIST_ID))
                mArtistId = intent.getStringExtra(MainActivityFragment.KEY_ARTIST_ID);

            if (intent.hasExtra(MainActivityFragment.KEY_ARTIST_NAME))
                mArtistName = intent.getStringExtra(MainActivityFragment.KEY_ARTIST_NAME);
        }

        actionBarSetup();

        // If the adapter is empty, look for the tracks.
        if (mListAdapter.getCount() == 0)
            searchTopTracks();

        return rootView;
    }

    private void actionBarSetup() {
        ActionBar ab = ((ActionBarActivity) getActivity()).getSupportActionBar();
        ab.setSubtitle(mArtistName);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(KEY_SONGS_LIST_POSITION, listOfTopSongs.getFirstVisiblePosition());
        savedInstanceState.putParcelableArrayList(KEY_SONGS_VIEW_STATE, mListAdapter.getAllItems());
    }

    public void searchTopTracks() {

        if (mArtistId != null && mArtistId.compareTo("") != 0) {
            songsProgressBar.setVisibility(View.VISIBLE);

            FetchTopSongsTask artistsTask = new FetchTopSongsTask();
            artistsTask.execute(mArtistId);
        } else {
            Log.e(LOG_TAG, "ID not found");
        }
    }

    public class FetchTopSongsTask extends AsyncTask<String, Void, AsyncTaskResult<List<Track>>> {

        private final String LOG_TAG = FetchTopSongsTask.class.getSimpleName();

        @Override
        protected AsyncTaskResult<List<Track>> doInBackground(String... params) {

            Tracks results;

            // If there's no search string, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            try {
                SpotifyService spotify = new SpotifyApi().getService();

                Map<String, Object> options = new HashMap<>();
                options.put("country", "US");
                results = spotify.getArtistTopTrack(params[0], options);

            } catch (Exception e) {
                e.printStackTrace();
                return new AsyncTaskResult<>(e);
            }

            return results != null ? new AsyncTaskResult<>(results.tracks) : null;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Track>> response) {
            String albumStr;
            String artistImage;

            List<Track> result = response.getResult();

            mListAdapter.clear();

            songsProgressBar.setVisibility(View.GONE);

            if (result != null && result.size() > 0) {
                for (Track track : result) {

                    albumStr = "";
                    artistImage = "";

                    if (track.album != null) {
                        albumStr = track.album.name;

                        if (track.album.images != null && track.album.images.size() > 0) {
                            Image image = track.album.images.get(track.album.images.size() - 1);
                            artistImage = image.url;
                        }
                    }

                    mListAdapter.add(new ListItem(track.id, artistImage, track.name, albumStr));
                }
            } else {
                if (response.getError() != null) {

                    Toast.makeText(getActivity(), response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Top tracks not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

