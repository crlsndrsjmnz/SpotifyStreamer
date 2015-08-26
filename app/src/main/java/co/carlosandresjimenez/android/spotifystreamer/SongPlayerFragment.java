package co.carlosandresjimenez.android.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SongPlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private final String LOG_TAG = SongPlayerFragment.class.getSimpleName();

    @Bind(R.id.artist_name)
    TextView mTvArtistName;
    @Bind(R.id.album_title)
    TextView mTvAlbumTitle;
    @Bind(R.id.album_cover)
    ImageView mIvAlbumCover;
    @Bind(R.id.song_title)
    TextView mTvSongTitle;
    @Bind(R.id.song_progress)
    TextView mTvSongProgress;
    @Bind(R.id.song_length)
    TextView mTvSongLength;
    @Bind(R.id.song_seekBar)
    SeekBar mSbSongProgress;
    @Bind(R.id.previous_song_button)
    ImageView mBtPreviusSong;
    @Bind(R.id.play_song_button)
    ImageView mBtPlaySong;
    @Bind(R.id.next_song_button)
    ImageView mBtNextSong;
    @Bind(R.id.stop_song_button)
    ImageView mBtStopSong;
    @Bind(R.id.share_song_button)
    ImageView mBtShareSong;
    @Bind(R.id.player_progress_bar)
    ProgressBar playerProgressBar;

    Handler mHandler;

    private MusicPlayerService musicPlayerSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean gotSongList = true;
    private boolean isDialog = false;
    private boolean mAutoPlaySong = false;

    private ShareActionProvider mShareActionProvider;
    private String mShareDetails;

    private ArrayList<ListItem> mSongList;
    private int mCurrentSong;

    private Runnable moveSeekBarThread = new Runnable() {

        public void run() {
            if (musicPlayerSrv.isPrepared() && musicPlayerSrv.isPlayingSong()) {
                int newSongPosition = musicPlayerSrv.getCurrentPosition();
                int songDuration = musicPlayerSrv.getDuration();

                mSbSongProgress.setMax(songDuration);
                mSbSongProgress.setProgress(newSongPosition);

                mTvSongLength.setText(formatDuration(songDuration));
                mTvSongProgress.setText(formatDuration(newSongPosition));

                // Looping the thread after 0.1 seconds
                mHandler.postDelayed(this, Constants.PLAYER_SERVICE.SEEK_BAR_REFRESH_RATE);
            } else {
                mSbSongProgress.setProgress(0);

                mTvSongLength.setText("");
                mTvSongProgress.setText(formatDuration(0));
            }
        }
    };

    private ServiceConnection musicPlayerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;

            musicPlayerSrv = binder.getService();
            musicBound = true;

            if (gotSongList)
                setSongList();
            else {
                gotSongList = true;
                mSongList = musicPlayerSrv.getSongList();
                mCurrentSong = musicPlayerSrv.getSongListPosition();

                if (mSongList != null)
                    getTrackDetails();
            }

            if (mAutoPlaySong) {
                mBtPlaySong.setSelected(false);
                onPlayOrPauseSong();
                mAutoPlaySong = false;
            }

            musicPlayerSrv.attach(SongPlayerFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public SongPlayerFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (musicBound && musicPlayerSrv.isPrepared() && musicPlayerSrv.isPlayingSong())
            updateSongTimer();

        // safety check
        if (getDialog() == null) {
            return;
        }

        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);

        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (playIntent == null) {
            playIntent = new Intent(getActivity().getApplicationContext(), MusicPlayerService.class);
            getActivity().getApplicationContext().bindService(playIntent, musicPlayerConnection, Context.BIND_AUTO_CREATE);
            getActivity().getApplicationContext().startService(playIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.song_player, container, false);
        ButterKnife.bind(this, rootView);
        mHandler = new Handler();

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.INTENT_EXTRA_ID.TRACK_LIST)) {
            mSongList = savedInstanceState.getParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST);
            mCurrentSong = savedInstanceState.getInt(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null && arguments.containsKey(Constants.INTENT_EXTRA_ID.TRACK_LIST)) {
                mSongList = arguments.getParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST);
                mCurrentSong = arguments.getInt(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION);
                mAutoPlaySong = true;
            } else {
                gotSongList = false;
            }
        }

        mSbSongProgress.setOnSeekBarChangeListener(this);

        if (gotSongList)
            getTrackDetails();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeCallbacks(moveSeekBarThread);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        musicPlayerSrv.detach();
    }

    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        isDialog = true;

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION, mCurrentSong);
        savedInstanceState.putParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST, mSongList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isDialog) {

            mBtShareSong.setVisibility(View.GONE);

            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.song_player, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if (mShareDetails != null)
                mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareDetails);
        return shareIntent;
    }

    public void getTrackDetails() {

        if (mSongList.get(mCurrentSong).getId() != null && !mSongList.get(mCurrentSong).getId().equals("")) {
            playerProgressBar.setVisibility(View.VISIBLE);

            mShareDetails = "#SpotifyStreamer " +
                    mSongList.get(mCurrentSong).getArtist() + " - " +
                    mSongList.get(mCurrentSong).getName() + " - " +
                    mSongList.get(mCurrentSong).getSongUrl();

            if (isDialog)
                mBtShareSong.setVisibility(View.VISIBLE);

            if (!isDialog && mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }

            updateUI();
        } else {
            Log.e(LOG_TAG, "ID not found");
        }
    }

    @OnClick(R.id.play_song_button)
    void onPlayOrPauseSong() {

        if (musicBound) {
            if (mBtPlaySong.isSelected()) {
                pauseSong();
                mBtPlaySong.setSelected(false);
            } else {
                playSong();
                mBtPlaySong.setSelected(true);
            }
        }
    }

    @OnClick(R.id.next_song_button)
    void onPlayNextSong() {

        if (musicBound) {
            musicPlayerSrv.nextSong();
        }
    }

    @OnClick(R.id.previous_song_button)
    void onPlayPreviousSong() {

        if (musicBound) {
            musicPlayerSrv.previousSong();
        }
    }

    @OnClick(R.id.stop_song_button)
    void onStopSong() {

        if (musicBound) {
            musicPlayerSrv.stopPlayer();
            mBtPlaySong.setSelected(false);
            updateSongTimer();
        }
    }

    @OnClick(R.id.share_song_button)
    void onShareSong() {
        Intent intent = createShareForecastIntent();
        startActivity(intent);
    }

    public void playSong() {
        musicPlayerSrv.playSong();
    }

    public void pauseSong() {
        mHandler.removeCallbacks(moveSeekBarThread);
        musicPlayerSrv.pauseSong();
    }

    public void songCompleted() {
        mHandler.removeCallbacks(moveSeekBarThread);
        mTvSongProgress.setText(formatDuration(0));
        mSbSongProgress.setProgress(0);
        mBtPlaySong.setSelected(false);
    }

    public void updateSongTimer() {
        mHandler.removeCallbacks(moveSeekBarThread);
        mHandler.postDelayed(moveSeekBarThread, Constants.PLAYER_SERVICE.SEEK_BAR_REFRESH_RATE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            musicPlayerSrv.songSeekTo(progress);
            mSbSongProgress.setProgress(progress);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void updateUI() {

        try {
            playerProgressBar.setVisibility(View.INVISIBLE);

            String artistName = mSongList.get(mCurrentSong).getArtist();
            if (artistName.length() > 50)
                artistName = artistName.substring(0, 50) + "...";
            mTvArtistName.setText(artistName);

            String albumTitle = mSongList.get(mCurrentSong).getDescription();
            if (albumTitle.length() > 50)
                albumTitle = albumTitle.substring(0, 50) + "...";
            mTvAlbumTitle.setText(albumTitle);

            Uri imgUri = Uri.parse(mSongList.get(mCurrentSong).getImageUrlHq());

            Picasso.with(LayoutInflater.from(getActivity()).getContext())
                    .load(imgUri)
                    .placeholder(R.drawable.ic_artist)
                    .error(R.drawable.ic_artist)
                    .into(mIvAlbumCover);

            String songTitle = mSongList.get(mCurrentSong).getName();
            if (songTitle.length() > 50)
                songTitle = songTitle.substring(0, 50) + "...";
            mTvSongTitle.setText(songTitle);

            if (musicPlayerSrv != null) {
                if (musicPlayerSrv.isPlayingSong())
                    mBtPlaySong.setSelected(true);

                updateSongTimer();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error obtaining the track", Toast.LENGTH_LONG).show();
        }
    }

    private void setSongList() {
        if (musicBound && !mSongList.isEmpty()) {
            musicPlayerSrv.setSongList(mSongList);
            musicPlayerSrv.setSongListPosition(mCurrentSong);
            musicPlayerSrv.prepareSong();
        }
    }

    public void setCurrentSong(int currentSong) {
        this.mCurrentSong = currentSong;
    }

    private String formatDuration(long duration) {
        String durationStr;

        int milliseconds = (int) duration % 60;
        int seconds = (int) (duration / 1000) % 60;
        int minutes = (int) ((duration / (1000 * 60)) % 60);
        int hours = (int) ((duration / (1000 * 60 * 60)) % 24);

        durationStr = String.format(getActivity().getString(R.string.format_duration), minutes, seconds);

        return durationStr;
    }
}
