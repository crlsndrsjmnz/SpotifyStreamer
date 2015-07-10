package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
 * Created by carlosjimenez on 7/7/15.
 */
public class TopSongsActivity extends ActionBarActivity {

    private final String LOG_TAG = TopSongsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    public static class TopSongsFragment extends Fragment {

        private final String LOG_TAG = TopSongsFragment.class.getSimpleName();

        private final String KEY_SONGS_VIEW_STATE = "KEY_SONGS_VIEW_STATE";
        private final String KEY_SONGS_LIST_POSITION = "KEY_SONGS_LIST_POSITION";

        @Bind(R.id.artist_top_songs_list)
        ListView listOfTopSongs;
        private String mArtistIdStr;
        private StreamerListAdapter mListAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.artist_detail, container, false);
            ButterKnife.bind(this, rootView);

            ArrayList<ListItem> mSongList;
            int songsListPosition = 0;

            if (savedInstanceState != null) {

                if (savedInstanceState.containsKey(KEY_SONGS_VIEW_STATE)) {
                    mSongList = savedInstanceState.getParcelableArrayList(KEY_SONGS_VIEW_STATE);
                } else {
                    mSongList = new ArrayList<ListItem>();
                }

                if (savedInstanceState.containsKey(KEY_SONGS_LIST_POSITION)) {
                    songsListPosition = savedInstanceState.getInt(KEY_SONGS_LIST_POSITION);
                }
            } else {
                mSongList = new ArrayList<ListItem>();
            }

            // The ArrayAdapter will take data from a source and
            // use it to populate the ListView it's attached to.
            mListAdapter = new StreamerListAdapter(mSongList, getActivity());

            if (listOfTopSongs != null) {
                listOfTopSongs.setAdapter(mListAdapter);
                listOfTopSongs.setSelection(songsListPosition);
            } else {
                Log.e(LOG_TAG, "ListOfTopSongs is NULL");
            }

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mArtistIdStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            if (mListAdapter.getCount() == 0)
                searchTopTracks();

            return rootView;
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);

            savedInstanceState.putInt(KEY_SONGS_LIST_POSITION, listOfTopSongs.getFirstVisiblePosition());
            savedInstanceState.putParcelableArrayList(KEY_SONGS_VIEW_STATE, mListAdapter.getAllItems());
        }

        public void searchTopTracks() {

            if (mArtistIdStr != null && mArtistIdStr.compareTo("") != 0) {
                FetchTopSongsTask artistsTask = new FetchTopSongsTask();
                artistsTask.execute(mArtistIdStr);
            } else {
                Log.e(LOG_TAG, "ID not found");
            }
        }

        public class FetchTopSongsTask extends AsyncTask<String, Void, List<Track>> {

            private final String LOG_TAG = FetchTopSongsTask.class.getSimpleName();

            @Override
            protected List<Track> doInBackground(String... params) {

                // If there's no search string, there's nothing to look up.  Verify size of params.
                if (params.length == 0) {
                    return null;
                }

                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                Map<String, Object> options = new HashMap<String, Object>();
                options.put("country", "US");
                Tracks results = spotify.getArtistTopTrack(params[0], options);
                return results != null ? results.tracks : null;

            }

            @Override
            protected void onPostExecute(List<Track> result) {
                String artistImage = "";

                mListAdapter.clear();

                if (result != null) {
                    for (Track track : result) {

                        String albumStr = "";
                        String albumImg = null;

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
                    Toast.makeText(getActivity(), "Top tracks not found", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

}
