package co.carlosandresjimenez.android.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import co.carlosandresjimenez.android.spotifystreamer.MusicPlayerService.MusicPlayerBinder;

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
    @Bind(R.id.player_progress_bar)
    ProgressBar playerProgressBar;

    boolean mPlaySongRequested;
    boolean mMediaPlayerReady;
    Handler mHandler;

    //service
    private MusicPlayerService musicPlayerSrv;
    private Intent playIntent;
    //binding
    private boolean musicBound = false;
    private boolean gotSongList = true;

    private ShareActionProvider mShareActionProvider;
    private String mShareDetails;

    //    private String mSongUrl;
//    private String mTrackId;
    private ArrayList<ListItem> mSongList;
    private int mCurrentSong;
    private int HANDLER_DELAY_MILLISECONDS = 100;
    private Runnable moveSeekBarThread = new Runnable() {

        public void run() {
            if (musicPlayerSrv.isPlayingSong()) {
                int newSongPosition = musicPlayerSrv.getCurrentPosition();
                int songDuration = musicPlayerSrv.getDuration();

                mSbSongProgress.setMax(songDuration);
                mSbSongProgress.setProgress(newSongPosition);

                mTvSongLength.setText(formatDuration(songDuration));
                mTvSongProgress.setText(formatDuration(newSongPosition));

                // Looping the thread after 0.1 seconds
                mHandler.postDelayed(this, HANDLER_DELAY_MILLISECONDS);
            } else {
                int songDuration = musicPlayerSrv.getDuration();

                mSbSongProgress.setMax(songDuration);
                mSbSongProgress.setProgress(0);

                mTvSongLength.setText(formatDuration(songDuration));
                mTvSongProgress.setText(formatDuration(0));
            }
        }
    };
    //connect to the service
    private ServiceConnection musicPlayerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% ServiceConnection.onServiceConnected");

            MusicPlayerBinder binder = (MusicPlayerBinder) service;
            //get service
            musicPlayerSrv = binder.getService();
            musicBound = true;

            //Log.d(LOG_TAG, "%%%%%%%%%%%% musicPlayerSrv.getSongUrl " + musicPlayerSrv.getSongUrl());
            //pass list
            //musicPlayerSrv.setList(songList);

            if (gotSongList)
                setSongList();
            else {
                gotSongList = true;
                mSongList = musicPlayerSrv.getSongList();
                mCurrentSong = musicPlayerSrv.getSongListPosition();

                if (mSongList != null)
                    getTrackDetails();
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

    public void setCurrentSong(int currentSong) {
        this.mCurrentSong = currentSong;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onCreate");

        if (playIntent == null) {
            playIntent = new Intent(getActivity().getApplicationContext(), MusicPlayerService.class);
            getActivity().getApplicationContext().bindService(playIntent, musicPlayerConnection, Context.BIND_AUTO_CREATE);
            getActivity().getApplicationContext().startService(playIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onCreateView");

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
            } else {
                Log.e(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onCreateView: Track List empty!!");

                gotSongList = false;
            }
        }

        mSbSongProgress.setOnSeekBarChangeListener(this);

        if (gotSongList)
            getTrackDetails();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onStart mTrackId: " + mSongList.get(mCurrentSong).getId());

    }

    @Override
    public void onResume() {
        super.onResume();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onResume mTrackId: " + mSongList.get(mCurrentSong).getId());
    }

    @Override
    public void onPause() {
        super.onPause();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onPause");

        mHandler.removeCallbacks(moveSeekBarThread);
    }


    @Override
    public void onStop() {
        super.onStop();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onStop");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onDestroy");

        //getActivity().getApplicationContext().unbindService(musicPlayerConnection);

        musicPlayerSrv.detach();
    }

    @Override
    public void onDetach() {
        super.onDetach();

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onDetach");

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onCreateDialog");

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onSaveInstanceState");

        savedInstanceState.putInt(Constants.INTENT_EXTRA_ID.TRACK_CURRENT_POSITION, mCurrentSong);
        savedInstanceState.putParcelableArrayList(Constants.INTENT_EXTRA_ID.TRACK_LIST, mSongList);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

//        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%% SongPlayerFragment.onDismiss");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

            if (mShareActionProvider != null) {
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
        mHandler.postDelayed(moveSeekBarThread, HANDLER_DELAY_MILLISECONDS);
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
            Log.d(LOG_TAG, "************* updateUI: " + mSongList.get(mCurrentSong).toString());

            playerProgressBar.setVisibility(View.INVISIBLE);

            mTvArtistName.setText(mSongList.get(mCurrentSong).getArtist());
            mTvAlbumTitle.setText(mSongList.get(mCurrentSong).getDescription());

            Uri imgUri = Uri.parse(mSongList.get(mCurrentSong).getImageUrlHq());

            Picasso.with(LayoutInflater.from(getActivity()).getContext())
                    .load(imgUri)
                    .placeholder(R.drawable.ic_artist)
                    .error(R.drawable.ic_artist)
                    .into(mIvAlbumCover);

            mTvSongTitle.setText(mSongList.get(mCurrentSong).getName());

            if (musicPlayerSrv != null) {
                if (musicPlayerSrv.isPlayingSong())
                    mBtPlaySong.setSelected(true);

                mHandler.postDelayed(moveSeekBarThread, HANDLER_DELAY_MILLISECONDS);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error obtaining the track", Toast.LENGTH_LONG).show();
        }
    }

    private void setSongList() {
        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%%%%% setSongList - mCurrentSong: " + mCurrentSong + " - mSongName: " + mSongList.get(mCurrentSong).getName());
        Log.d(LOG_TAG, "%%%%%%%%%%%%%%%%%%%%%%% setSongList - musicBound: " + musicBound + " - mSongUrl: " + mSongList.get(mCurrentSong).getPreviewUrl());

        if (musicBound && !mSongList.isEmpty()) {
            musicPlayerSrv.setSongList(mSongList);
            musicPlayerSrv.setSongListPosition(mCurrentSong);
            musicPlayerSrv.prepareSong();
        }
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
