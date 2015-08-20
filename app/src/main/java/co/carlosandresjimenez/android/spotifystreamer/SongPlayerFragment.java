package co.carlosandresjimenez.android.spotifystreamer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Carlos on 8/20/2015.
 */
public class SongPlayerFragment extends DialogFragment implements MediaPlayer.OnPreparedListener {

    public static final String TRACK_ID = "SONG_ID";
    private final String LOG_TAG = SongPlayerFragment.class.getSimpleName();
    @Bind(R.id.artist_name)
    TextView mTvArtistName;
    @Bind(R.id.album_title)
    TextView mTvAlbumTitle;
    @Bind(R.id.album_cover)
    ImageView mIvAlbumCover;
    @Bind(R.id.song_title)
    TextView mTvSongTitle;
    @Bind(R.id.song_seekBar)
    SeekBar mSbSongProgress;
    @Bind(R.id.previus_song_button)
    ImageView mBtPreviusSong;
    @Bind(R.id.play_song_button)
    ImageView mBtPlaySong;
    @Bind(R.id.next_song_button)
    ImageView mBtNextSong;
    @Bind(R.id.player_progress_bar)
    ProgressBar playerProgressBar;

    MediaPlayer mMediaPlayer;
    boolean mPlayingSong;

    private String mTrackId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.song_player, container, false);
        ButterKnife.bind(this, rootView);

        mPlayingSong = false;
        mTrackId = "";

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackId = arguments.getString(TRACK_ID);
        }

        mMediaPlayer = new MediaPlayer();

        getTrackDetails();

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        Log.d(LOG_TAG, "***** SongPlayerFragment.onDismiss");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(LOG_TAG, "***** SongPlayerFragment.onDestroyView");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LOG_TAG, "***** SongPlayerFragment.onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(LOG_TAG, "***** SongPlayerFragment.onDetach");
    }

    public void getTrackDetails() {

        if (mTrackId != null && !mTrackId.equals("")) {
            playerProgressBar.setVisibility(View.VISIBLE);

            FetchTrackTask trackTask = new FetchTrackTask();
            trackTask.execute(mTrackId);
        } else {
            Log.e(LOG_TAG, "ID not found");
        }
    }

    @OnItemClick(R.id.previus_song_button)
    void onPlaySong() {
        mPlayingSong = true;
        mMediaPlayer.start();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    public class FetchTrackTask extends AsyncTask<String, Void, AsyncTaskResult<Track>> {

        private final String LOG_TAG = FetchTrackTask.class.getSimpleName();

        @Override
        protected AsyncTaskResult<Track> doInBackground(String... params) {

            Track result;

            // If there's no search string, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            try {
                SpotifyService spotify = new SpotifyApi().getService();
                result = spotify.getTrack(params[0]);

            } catch (Exception e) {
                e.printStackTrace();
                return new AsyncTaskResult<>(e);
            }

            return new AsyncTaskResult<>(result);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Track> response) {
            String albumStr;
            String artistImage;

            Track track = response.getResult();
            playerProgressBar.setVisibility(View.GONE);

            if (track != null) {
                albumStr = "";
                artistImage = "";

                if (track.album != null) {
                    albumStr = track.album.name;

                    if (track.album.images != null)
                        for (Image image : track.album.images) {
                            if (image.height <= 300) {
                                artistImage = image.url;
                                break;
                            }
                        }
//                        Image image = track.album.images.get(track.album.images.size() - 1);
//                        artistImage = image.url;
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

                updateUI(artistStr, albumStr, track.name, artistImage, track.preview_url);

                //mListAdapter.add(new ListItem(track.id, artistImage, track.name, albumStr));
            } else {
                if (response.getError() != null) {

                    Toast.makeText(getActivity(), response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Top tracks not found", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void updateUI(String artistName,
                             String albumName,
                             String songName,
                             String albumCoverUri,
                             String previewTrackUrl) {

            try {
                mTvArtistName.setText(artistName);
                mTvAlbumTitle.setText(albumName);

                Uri imgUri = Uri.parse(albumCoverUri);

                Picasso.with(LayoutInflater.from(getActivity()).getContext())
                        .load(imgUri)
                        .placeholder(R.drawable.ic_artist)
                        .error(R.drawable.ic_artist)
                        .into(mIvAlbumCover);

                mTvSongTitle.setText(songName);

                Uri previewTrackUri = Uri.parse(previewTrackUrl);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(getActivity(), previewTrackUri);
                mMediaPlayer.prepareAsync();

                //mSbSongProgress.set

//            mSbSongProgress;
//            mBtPreviusSong;
//            mBtPlaySong;
//            mBtNextSong;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error obtaining the track", Toast.LENGTH_LONG).show();
            }
        }

    }
}
