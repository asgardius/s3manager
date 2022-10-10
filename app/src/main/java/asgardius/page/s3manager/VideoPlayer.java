package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayer extends AppCompatActivity {

    // creating a variable for exoplayerview.
    protected StyledPlayerView playerView;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;
    private long maxCacheSize;
    LeastRecentlyUsedCacheEvictor evictor;
    StandaloneDatabaseProvider standaloneDatabaseProvider;
    SimpleCache simpleCache;
    int videocache, buffersize;
    ProgressiveMediaSource mediaSource;
    DefaultLoadControl loadControl;
    DefaultRenderersFactory renderersFactory;
    ExoPlayer player;
    long videoPosition;
    MediaSessionCompat mediaSession;
    MediaSessionConnector mediaSessionConnector;
    StyledPlayerView.ControllerVisibilityListener control;
    AppOpsManager appOpsManager;
    private PlayerNotificationManager playerNotificationManager;
    private int notificationId = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        appOpsManager = (AppOpsManager)getSystemService(Context.APP_OPS_SERVICE);
        mediaSession = new MediaSessionCompat(this, getPackageName());
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        hideSystemBars();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        // create Wifi and wake locks
        mWifiLock = ((WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "S3Manager:wifi_lock");
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S3Manager:wake_lock");
        //Get media url
        String videoURL = getIntent().getStringExtra("video_url");
        videocache = getIntent().getIntExtra("videocache", 40);
        buffersize = getIntent().getIntExtra("buffersize", 2000);
        loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(2000, buffersize, 1500, 2000).build();

        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;

        renderersFactory = new DefaultRenderersFactory(this) .setExtensionRendererMode(extensionRendererMode);
        standaloneDatabaseProvider = new StandaloneDatabaseProvider(this);
        maxCacheSize = (long)videocache * 1024 * 1024;
        playerView = findViewById(R.id.player_view);
        // creating a variable for exoplayer
        player = new ExoPlayer.Builder(this).setLoadControl(loadControl).build();
        player.setAudioAttributes(audioAttributes, true);
        mediaSessionConnector.setPlayer(player);
        mediaSession.setActive(true);
        evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
        simpleCache = new SimpleCache(
                new File(this.getCacheDir(), "media"),
                evictor,
                standaloneDatabaseProvider);
        mediaSource = new ProgressiveMediaSource.Factory(
                new CacheDataSource.Factory()
                        .setCache(simpleCache)
                        .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                .setUserAgent("ExoplayerDemo"))
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        ).createMediaSource(MediaItem.fromUri(Uri.parse(videoURL)));
        playerView.setPlayer(player);
        control = new StyledPlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if (playerView.isControllerFullyVisible()) {
                    showSystemBars();
                } else {
                    hideSystemBars();
                }
            }
        };
        playerView.setControllerVisibilityListener(control);
        //MediaItem mediaItem = MediaItem.fromUri(videoURL);

        // Set the media item to be played.
        //player.setMediaItem(mediaItem);
        // Prepare the player.
        player.setPlayWhenReady(true);
        //playerNotificationManager = new PlayerNotificationManager.Builder(this, notificationId, "playback").build();
        player.setMediaSource(mediaSource);
        player.prepare();
        // Start the playback.
        player.play();


        player.addListener(new Player.Listener() {
            @Override

            public void onPlayerError(PlaybackException error) {
                Throwable cause = error.getCause();
                if (cause instanceof HttpDataSource.HttpDataSourceException) {
                    // An HTTP error occurred.
                    //System.out.println("Playback error F");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.media_conn_fail), Toast.LENGTH_SHORT).show();
                } else {
                    // An HTTP error occurred.
                    //System.out.println("Playback error F");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.media_wrong_type), Toast.LENGTH_SHORT).show();
                }
                player.release();
                finish();
            }


        });

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int state) {
                if (state == 3) {
                    // Active playback.
                    //Acquiring WakeLock and WifiLock if not held
                    if (!mWifiLock.isHeld()) {
                        mWifiLock.acquire();
                        //System.out.println("WifiLock acquired");
                    }
                    if (!mWakeLock.isHeld()) {
                        mWakeLock.acquire();
                        //System.out.println("WakeLock acquired");
                    }
                } else if (state == 2) {
                    // Buffering.
                    //Acquiring WakeLock and WifiLock if not held
                    if (!mWifiLock.isHeld()) {
                        mWifiLock.acquire();
                        //System.out.println("WifiLock acquired");
                    }
                    if (!mWakeLock.isHeld()) {
                        mWakeLock.acquire();
                        //System.out.println("WakeLock acquired");
                    }
                } else {
                    //Player inactive
                    //Releasing WifiLock and WakeLock if held
                    if (mWifiLock.isHeld()) {
                        mWifiLock.release();
                        //System.out.println("WifiLock released");
                    }
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                        //System.out.println("WakeLock released");
                    }
                    // Not playing because playback is paused, ended, suppressed, or the player
                    // is buffering, stopped or failed. Check player.getPlayWhenReady,
                    // player.getPlaybackState, player.getPlaybackSuppressionReason and
                    // player.getPlaybackError for details.
                }
            }
        });
    }

    private void hideSystemBars() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemBars() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    protected void enterPIPMode() {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && this.getPackageManager()
                    .hasSystemFeature(
                            PackageManager.FEATURE_PICTURE_IN_PICTURE) && appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    this.getPackageManager().getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA).uid, this.getPackageName())
                    == AppOpsManager.MODE_ALLOWED) {
                videoPosition = player.getCurrentPosition();
                playerView.setUseController(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PictureInPictureParams params = new PictureInPictureParams.Builder().build();
                    this.enterPictureInPictureMode(params);
                }else {
                    this.enterPictureInPictureMode();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    /*public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);


        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }*/

    public void onDestroy() {
        mediaSessionConnector.setPlayer(null);
        mediaSession.setActive(false);
        player.release();
        playerView.setPlayer(null);
        simpleCache.release();
        super.onDestroy();
    }

    public void onStop() {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && this.getPackageManager()
                    .hasSystemFeature(
                            PackageManager.FEATURE_PICTURE_IN_PICTURE) && appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    this.getPackageManager().getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA).uid, this.getPackageName())
                    == AppOpsManager.MODE_ALLOWED) {
                mediaSessionConnector.setPlayer(null);
                mediaSession.setActive(false);
                player.release();
                playerView.setPlayer(null);
                simpleCache.release();
                finish();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    public void onResume(){
        super.onResume();
        // put your code here...
        if (!playerView.getUseController()) {
            playerView.setUseController(true);
        }
    }

    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPIPMode();
    }

    public void onBackPressed() {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && this.getPackageManager()
                    .hasSystemFeature(
                            PackageManager.FEATURE_PICTURE_IN_PICTURE) && appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    this.getPackageManager().getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA).uid, this.getPackageName())
                    == AppOpsManager.MODE_ALLOWED) {
                enterPIPMode();
            } else {
                super.onBackPressed();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            super.onBackPressed();
        }
    }
}