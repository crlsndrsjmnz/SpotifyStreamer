package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Fragment containing the artist search functionality and result list.
 */
public class MainActivityFragment extends Fragment {


    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private final String KEY_ARTISTS_VIEW_STATE = "KEY_ARTISTS_VIEW_STATE";
    private final String KEY_ARTISTS_LIST_POSITION = "KEY_ARTISTS_LIST_POSITION";

    @Bind(R.id.artist_resut_list)
    ListView listOfArtists;
    @Bind(R.id.artist_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.search_box)
    SearchView artistSearchBar;

    ArrayList<ListItem> mArtistList;
    int artistListPosition = 0;

    private StreamerListAdapter mListAdapter;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (!TextUtils.isEmpty(query)) {
                searchArtist(query);
                return true;
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    public MainActivityFragment() {
    }

    @OnItemClick(R.id.artist_resut_list)
    void onSelectArtist(int position) {
        getTopTracks(mListAdapter.getItem(position));
    }

    public void searchArtist(String query) {
        artistSearchBar.clearFocus();
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(artistSearchBar.getWindowToken(), 0);

        progressBar.setVisibility(View.VISIBLE);

        FetchArtistsTask artistsTask = new FetchArtistsTask();
        artistsTask.execute(query);
    }

    public void getTopTracks(ListItem item) {

        if (item != null)
            ((Callback) getActivity()).onItemSelected(item);

//        Intent topSongsIntent = new Intent(getActivity(), TopSongsActivity.class);
//        topSongsIntent.putExtra(KEY_ARTIST_ID, item.getId());
//        topSongsIntent.putExtra(KEY_ARTIST_NAME, item.getName());
//        startActivity(topSongsIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_ARTISTS_VIEW_STATE)) {
                mArtistList = savedInstanceState.getParcelableArrayList(KEY_ARTISTS_VIEW_STATE);
            } else {
                mArtistList = new ArrayList<>();
            }

            if (savedInstanceState.containsKey(KEY_ARTISTS_LIST_POSITION)) {
                artistListPosition = savedInstanceState.getInt(KEY_ARTISTS_LIST_POSITION);
            }
        } else {
            mArtistList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        artistSearchBar.setOnQueryTextListener(onQueryTextListener);

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mListAdapter = new StreamerListAdapter(getActivity(), mArtistList);

        if (listOfArtists != null) {
            listOfArtists.setAdapter(mListAdapter);
            listOfArtists.setSelection(artistListPosition);
        } else {
            Log.e(LOG_TAG, "ListOfArtists is NULL");
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(KEY_ARTISTS_LIST_POSITION, listOfArtists.getFirstVisiblePosition());
        savedInstanceState.putParcelableArrayList(KEY_ARTISTS_VIEW_STATE, mListAdapter.getAllItems());
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
        void onItemSelected(ListItem item);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, AsyncTaskResult<List<Artist>>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected AsyncTaskResult<List<Artist>> doInBackground(String... params) {

            ArtistsPager results;

            // If there's no search string, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            try {
                SpotifyService spotify = new SpotifyApi().getService();
                results = spotify.searchArtists(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new AsyncTaskResult<>(e);
            }

            return results.artists != null ? new AsyncTaskResult<>(results.artists.items) : null;
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Artist>> response) {
            String artistImage;
            List<Artist> result = response.getResult();

            mListAdapter.clear();

            progressBar.setVisibility(View.GONE);

            if (result != null && result.size() > 0) {
                for (Artist artist : result) {

                    artistImage = "";

                    if (artist.images.size() > 0) {
                        Image image = artist.images.get(artist.images.size() - 1);
                        artistImage = image.url;
                    }

                    mListAdapter.add(new ListItem(artist.id, artistImage, artist.name, ""));
                }
            } else {
                if (response.getError() != null) {
                    Toast.makeText(getActivity(), response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Artist not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

