package co.carlosandresjimenez.android.spotifystreamer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import butterknife.OnItemClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopSongsFragment extends Fragment {

    private final String LOG_TAG = TopSongsFragment.class.getSimpleName();

    @Bind(R.id.artist_top_songs_list)
    ListView listOfTopSongs;
    @Bind(R.id.songs_progress_bar)
    ProgressBar searchProgressBar;

    Activity mActivity;

    ArrayList<ListItem> mSongList;
    int songsListPosition = 0;
    private String mArtistId;
    private String mArtistName;
    private StreamerListAdapter mListAdapter;
    private boolean searchTopSongs = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(Constants.INTENT_EXTRA_ID.KEY_SONGS_VIEW_STATE)) {
                mSongList = savedInstanceState.getParcelableArrayList(Constants.INTENT_EXTRA_ID.KEY_SONGS_VIEW_STATE);
            } else {
                mSongList = new ArrayList<>();
            }

            if (savedInstanceState.containsKey(Constants.INTENT_EXTRA_ID.KEY_SONGS_LIST_POSITION)) {
                songsListPosition = savedInstanceState.getInt(Constants.INTENT_EXTRA_ID.KEY_SONGS_LIST_POSITION);
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
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(Constants.INTENT_EXTRA_ID.KEY_ARTIST_ID)) {
                mArtistId = arguments.getString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_ID);
                mArtistName = arguments.getString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME);
            } else if (arguments.containsKey(Constants.INTENT_EXTRA_ID.TRACK_LIST)) {
                mSongList = arguments.getParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST);
                mArtistName = arguments.getString(Constants.INTENT_EXTRA_ID.KEY_ARTIST_NAME);
                searchTopSongs = false;
            }
        }

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mListAdapter = new StreamerListAdapter(getActivity(), mSongList);

        if (listOfTopSongs != null) {
            listOfTopSongs.setAdapter(mListAdapter);
            listOfTopSongs.setSelection(songsListPosition);
        } else {
            Log.e(LOG_TAG, "ListOfTopSongs is NULL");
        }

        actionBarSetup();

        // If the adapter is empty, look for the tracks.
        if (mListAdapter.getCount() == 0 && searchTopSongs)
            searchTopTracks();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @OnItemClick(R.id.artist_top_songs_list)
    void onSelectTrack(int position) {
        ((Callback) mActivity).onSongSelected(mListAdapter.getAllItems(), position);
    }

    private void actionBarSetup() {
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (ab != null)
            ab.setSubtitle(mArtistName);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.INTENT_EXTRA_ID.KEY_SONGS_LIST_POSITION, listOfTopSongs.getFirstVisiblePosition());
        savedInstanceState.putParcelableArrayList(Constants.INTENT_EXTRA_ID.KEY_SONGS_VIEW_STATE, mListAdapter.getAllItems());
    }

    public void searchTopTracks() {

        if (mArtistId != null && mArtistId.compareTo("") != 0) {
            searchProgressBar.setVisibility(View.VISIBLE);

            FetchTopSongsTask artistsTask = new FetchTopSongsTask();
            artistsTask.execute(mArtistId);
        } else {
            Log.e(LOG_TAG, "ID not found");
        }
    }

    private String getCountryCode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(getActivity().getString(R.string.pref_country_key),
                getActivity().getString(R.string.pref_country_default));
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onSongSelected(ArrayList<ListItem> items, int songSelection);
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

                options.put("country", getCountryCode());
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
            String artistImageHq;

            List<Track> result = response.getResult();
            mListAdapter.clear();
            searchProgressBar.setVisibility(View.GONE);

            if (result != null && result.size() > 0) {
                for (Track track : result) {

                    albumStr = "";
                    artistImage = "";
                    artistImageHq = "";

                    if (track.album != null) {
                        albumStr = track.album.name;

                        if (track.album.images != null && track.album.images.size() > 0) {
                            Image image = track.album.images.get(track.album.images.size() - 1);
                            artistImage = image.url;
                        }

                        if (track.album.images != null && track.album.images.size() > 1) {
                            Image image = track.album.images.get(track.album.images.size() - 2);
                            artistImageHq = image.url;
                        } else {
                            artistImageHq = artistImage;
                        }
                    }

                    String artistStr = "";
                    boolean firstLoop = true;
                    for (ArtistSimple artist : track.artists) {
                        if (firstLoop) {
                            artistStr = artist.name;
                            firstLoop = false;
                        } else {
                            artistStr = artistStr + ", " + artist.name;
                        }
                    }

                    String songUrl = "";
                    if (track.external_urls.containsKey("spotify"))
                        songUrl = track.external_urls.get("spotify");

                    mListAdapter.add(new ListItem(track.id, artistImage, artistImageHq, track.name, albumStr, songUrl, artistStr, track.preview_url));
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

