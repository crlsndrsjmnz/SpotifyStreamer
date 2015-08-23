package co.carlosandresjimenez.android.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * Created by carlosjimenez on 8/22/15.
 */
public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final int PLAY_NOTIFICATION = 0;
    private static final int PAUSE_NOTIFICATION = 1;
    private static final String TEST_TRACK_NAME = "Artist - Song";
    private final String LOG_TAG = MusicPlayerService.class.getSimpleName();
    //binder
    private final IBinder musicBind = new MusicPlayerBinder();
    //String mTrackUrl;
    SongPlayerFragment fragment;
    boolean mIsPrepared = false;
    boolean mPlayRequested = false;
    Intent notificationIntent;
    PendingIntent pendingIntent;
    Intent previousIntent;
    PendingIntent pPreviousIntent;
    Intent playIntent;
    PendingIntent pPlayIntent;
    Intent pauseIntent;
    PendingIntent pPauseIntent;
    Intent nextIntent;
    PendingIntent pNextIntent;
    Bitmap icon;
    Bitmap defaultNotificationIcon;
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<ListItem> mSongList;
    //current position
    private int songListPosition;
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            try {
                setNotificationIcon(bitmap);
                if (isPlayingSong())
                    updateNotification(PAUSE_NOTIFICATION, getSongTitle());
                else
                    updateNotification(PLAY_NOTIFICATION, getSongTitle());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public void onCreate() {
        //create the service
        super.onCreate();

        Log.d(LOG_TAG, "^^^^^^^^^^^^^^ MusicPlayerService.onCreate");

        //initialize position
        songListPosition = 0;
        //create player
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();

        initNotification(getSongTitle());
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(LOG_TAG, "^^^^^^^^^^^^^^ MusicPlayerService.onBind");

        return musicBind;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent) {

        Log.d(LOG_TAG, "^^^^^^^^^^^^^^ MusicPlayerService.onUnbind");

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "^^^^^^^^^^^^^^ MusicPlayerService.onDestroy");

        player.stop();
        player.release();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LOG_TAG, "^^^^^^^^^^^^^^ MusicPlayerService.onStartCommand");

        if (intent != null && intent.getAction() != null)
            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
            } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
                Log.i(LOG_TAG, "Clicked Previous");
                previousSong();
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                Log.i(LOG_TAG, "Clicked Play");
                playSong();
            } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
                Log.i(LOG_TAG, "Clicked Pause");
                pauseSong();
            } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                Log.i(LOG_TAG, "Clicked Next");
                nextSong();
            } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
        return START_STICKY;
    }

    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    private boolean showNotifications() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String displayNotificationsKey = getApplicationContext().getString(R.string.pref_enable_notifications_key);
        return prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(getApplicationContext().getString(R.string.pref_enable_notifications_default)));
    }

    private void initNotification(String songTitle) {

        notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        previousIntent = new Intent(this, MusicPlayerService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        pPreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        playIntent = new Intent(this, MusicPlayerService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        pPlayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        pauseIntent = new Intent(this, MusicPlayerService.class);
        pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
        pPauseIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);

        nextIntent = new Intent(this, MusicPlayerService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        pNextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        defaultNotificationIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_artist);
        this.icon = defaultNotificationIcon;

        if (showNotifications()) {
            Notification notification = getPlayNotification(songTitle);

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }
    }

    private void updateNotification(int notificationType, String songTitle) {

        Notification notification;

        if (notificationType == PLAY_NOTIFICATION)
            notification = getPlayNotification(songTitle);
        else
            notification = getPauseNotification(songTitle);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }

    private Notification getPlayNotification(String songTitle) {

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notification_title))
                .setTicker(getString(R.string.notification_ticker))
                .setContentText(songTitle)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, getString(R.string.notification_previous_button),
                        pPreviousIntent)
                .addAction(android.R.drawable.ic_media_play, getString(R.string.notification_play_button),
                        pPlayIntent)
                .addAction(android.R.drawable.ic_media_next, getString(R.string.notification_next_button),
                        pNextIntent)
                .build();
    }

    private Notification getPauseNotification(String songTitle) {

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notification_title))
                .setTicker(getString(R.string.notification_ticker))
                .setContentText(songTitle)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, getString(R.string.notification_previous_button),
                        pPreviousIntent)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.notification_pause_button),
                        pPauseIntent)
                .addAction(android.R.drawable.ic_media_next, getString(R.string.notification_next_button),
                        pNextIntent)
                .build();
    }

    void attach(SongPlayerFragment fragment) {
        this.fragment = fragment;
    }

    void detach() {
        this.fragment = null;
    }

    public void nextSong() {
        songListPosition++;
        if (mSongList.size() <= songListPosition)
            songListPosition = 0;

        if (fragment != null) {
            fragment.setCurrentSong(songListPosition);
            fragment.updateUI();
        }

        prepareSong();
        playSong();
    }

    public void previousSong() {
        songListPosition--;
        if (songListPosition < 0)
            songListPosition = mSongList.size() - 1;

        if (fragment != null) {
            fragment.setCurrentSong(songListPosition);
            fragment.updateUI();
        }

        prepareSong();
        playSong();
    }

    private String getSongTitle() {
        String title = "";
        ListItem item;

        if (mSongList != null) {
            item = mSongList.get(songListPosition);
            title = item.getArtist() + " - " + item.getName();
        }

        return title;
    }

    //play a song
    public void prepareSong() {
        //play
        player.reset();
        mIsPrepared = false;
        this.icon = defaultNotificationIcon;

        try {
            Log.d(LOG_TAG, "^^^^^^^^^^^^^^ MusicPlayerService songListPosition:" + songListPosition + " - PreviewUrl: " + mSongList.get(songListPosition).getName());

            if (showNotifications())
                Picasso.with(this)
                        .load(mSongList.get(songListPosition).getImageUrl())
                        .into(target);

            player.setDataSource(mSongList.get(songListPosition).getPreviewUrl());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error setting data source", e);
        }
        player.prepareAsync();
    }

    //play song
    public void playSong() {
        if (isPrepared()) {

            if (showNotifications())
                updateNotification(PAUSE_NOTIFICATION, getSongTitle());

            player.start();

            if (fragment != null)
                fragment.updateSongTimer();
        } else {
            mPlayRequested = true;
            Log.e(LOG_TAG, "MediaPlayer not prepared");
        }
    }

    //pause song
    public void pauseSong() {
        if (showNotifications())
            updateNotification(PLAY_NOTIFICATION, getSongTitle());

        player.pause();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void songSeekTo(int milliseconds) {
        player.seekTo(milliseconds);
    }

    public boolean isPlayingSong() {
        return player.isPlaying();
    }

    public ArrayList<ListItem> getSongList() {
        return mSongList;
    }

    public void setSongList(ArrayList<ListItem> songList) {
        this.mSongList = songList;
    }

    public int getSongListPosition() {
        return songListPosition;
    }

    public void setSongListPosition(int songListPosition) {
        this.songListPosition = songListPosition;
    }

    public void setNotificationIcon(Bitmap icon) {
        this.icon = icon;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub
        if (fragment != null)
            fragment.songCompleted();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mIsPrepared = true;

        if (mPlayRequested)
            playSong();

        mPlayRequested = false;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    //binder
    public class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

}
