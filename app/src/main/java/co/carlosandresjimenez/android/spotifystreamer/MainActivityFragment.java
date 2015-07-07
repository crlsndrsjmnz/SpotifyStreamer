package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
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

    @Bind(R.id.artist_resut_list)
    ListView listOfArtists;
    @Bind(R.id.artist_search_bar)
    TextView artistSearchBar;
    @Bind(R.id.search_button)
    ImageView artistSearchButton;

    private StreamerListAdapter mListAdapter;

    public MainActivityFragment() {
    }

    @OnItemClick(R.id.artist_resut_list)
    void onSelectArtist(int position) {
        getTopTracks(mListAdapter.getItem(position).getId());
    }

    @OnClick(R.id.search_button)
    void onClickSearchArtist() {
        searchArtist();
    }

    @OnEditorAction(R.id.artist_search_bar)
    boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchArtist();
            return true;
        }
        return false;
    }

    @OnFocusChange(R.id.artist_search_bar)
    void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            searchArtist();
        }
    }

    public void searchArtist() {

        Log.d(LOG_TAG, "searchArtist " + artistSearchBar.getText().toString());

        FetchArtistsTask artistsTask = new FetchArtistsTask();
        artistsTask.execute(artistSearchBar.getText().toString());
    }

    public void getTopTracks(String id) {

        Log.d(LOG_TAG, "getTopTracks " + id);

        Intent topSongsIntent = new Intent(getActivity(), TopSongsActivity.class);
        topSongsIntent.putExtra(Intent.EXTRA_TEXT, id);
        startActivity(topSongsIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mListAdapter = new StreamerListAdapter(getActivity());

        if (listOfArtists != null)
            listOfArtists.setAdapter(mListAdapter);
        else
            Log.e(LOG_TAG, "ListOfArtists is NULL");

        return rootView;
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected List<Artist> doInBackground(String... params) {

            // If there's no search string, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);

            return results.artists != null ? results.artists.items : null;
        }

        @Override
        protected void onPostExecute(List<Artist> result) {
            String artistImage = "";

            mListAdapter.clear();

            if (result != null && result.size() > 0) {
                for (Artist artist : result) {
                    if (artist.images.size() > 0) {
                        Image image = artist.images.get(artist.images.size() - 1);
                        artistImage = image.url;
                    } else {
                        artistImage = null;
                    }

                    mListAdapter.add(new ListItem(artist.id, artistImage, artist.name, ""));
                }
            } else {
                Toast.makeText(getActivity(), "Artist not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

