package asgardius.page.s3manager;

import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Rational;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

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
    ArrayList<String> queue, names;
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
    boolean isplaylist;
    boolean success = false;
    String videoURL, title;
    Rational ratio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){

            NotificationChannel channel= new NotificationChannel("playback","Video Playback", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager =getSystemService(NotificationManager.class);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }
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
        videoURL = getIntent().getStringExtra("video_url");
        title = getIntent().getStringExtra("title");
        videocache = getIntent().getIntExtra("videocache", 40);
        buffersize = getIntent().getIntExtra("buffersize", 2000);
        isplaylist = getIntent().getBooleanExtra("isplaylist", false);
        queue = getIntent().getStringArrayListExtra("queue");
        names = getIntent().getStringArrayListExtra("names");
        getSupportActionBar().setTitle(title);
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
        playerNotificationManager = new PlayerNotificationManager.Builder(this, notificationId, "playback").build();
        playerNotificationManager.setUseNextActionInCompactView(true);
        playerNotificationManager.setUsePreviousActionInCompactView(true);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        playerNotificationManager.setPlayer(player);
        if (isplaylist) {
            for (int i = 0; i < queue.size(); i++) {
                if (names.get(i).endsWith(".m3u8")) {
                    MediaItem mediaItem = MediaItem.fromUri(Share.URLify(queue.get(i)));
                    player.addMediaItem(mediaItem);
                } else {
                    mediaSource = new ProgressiveMediaSource.Factory(
                            new CacheDataSource.Factory()
                                    .setCache(simpleCache)
                                    .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                            .setUserAgent("S3 Manager"))
                                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    ).createMediaSource(MediaItem.fromUri(Share.URLify(queue.get(i))));
                    player.addMediaSource(mediaSource);
                }
            }
            player.prepare();
            player.seekTo(names.indexOf(title), 0);
        } else {
            if (title.endsWith(".m3u8")) {
                MediaItem mediaItem = MediaItem.fromUri(Share.URLify(videoURL));
                player.setMediaItem(mediaItem);
            } else {
                mediaSource = new ProgressiveMediaSource.Factory(
                        new CacheDataSource.Factory()
                                .setCache(simpleCache)
                                .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                        .setUserAgent("S3 Manager"))
                                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                ).createMediaSource(MediaItem.fromUri(Share.URLify(videoURL)));
                player.setMediaSource(mediaSource);
            }
            player.prepare();
        }
        // Start the playback.
        player.play();

        player.addListener(new Player.Listener() {
            @Override

            public void onPlayerError(PlaybackException error) {
                Throwable cause = error.getCause();
                if(success) {
                    player.pause();
                } else {
                    // An HTTP error occurred.
                    //System.out.println("Playback error F");
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(error.getCause()).toString(), Toast.LENGTH_SHORT).show();
                    player.release();
                    finish();
                }
            }


        });

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int state) {
                if (state == 3) {
                    // Active playback.
                    success = true;
                    //Acquiring WakeLock and WifiLock if not held
                    if (!mWifiLock.isHeld()) {
                        mWifiLock.acquire();
                        //System.out.println("WifiLock acquired");
                    }
                    if (!mWakeLock.isHeld()) {
                        mWakeLock.acquire();
                        //System.out.println("WakeLock acquired");
                    }
                    if(isplaylist) {
                        getSupportActionBar().setTitle(names.get(player.getCurrentMediaItemIndex()));
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
                    if(isplaylist) {
                        getSupportActionBar().setTitle(names.get(player.getCurrentMediaItemIndex()));
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
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
                    if(player.getVideoFormat() != null) {
                        ratio = new Rational(Math.round(player.getVideoFormat().width*20*player.getVideoSize().pixelWidthHeightRatio), Math.round(player.getVideoFormat().height*20));
                    } else if(player.getAudioFormat() != null) {
                        ratio = new Rational(player.getAudioFormat().width, player.getAudioFormat().height);
                    } else {
                        ratio = new Rational(1, 1);
                    }
                    PictureInPictureParams params = new PictureInPictureParams.Builder().setAspectRatio(ratio).build();
                    this.enterPictureInPictureMode(params);
                }else {
                    this.enterPictureInPictureMode();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isScreenOn(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        boolean screenOn = false;
        for (Display display : dm.getDisplays()) {
            if (display.getState() == Display.STATE_ON) {
                screenOn = true;
            }
        }
        return screenOn;
    }

    @Override

    public void onDestroy() {
        if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
            //System.out.println("WifiLock acquired");
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            //System.out.println("WakeLock acquired");
        }
        mediaSessionConnector.setPlayer(null);
        deleteCache(this, standaloneDatabaseProvider);
        mediaSession.setActive(false);
        playerNotificationManager.setPlayer(null);
        player.release();
        playerView.setPlayer(null);
        simpleCache.release();
        standaloneDatabaseProvider.close();
        super.onDestroy();
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

    protected void onNewIntent(Intent intent) {
        videoURL = intent.getStringExtra("video_url");
        title = intent.getStringExtra("title");
        videocache = intent.getIntExtra("videocache", 40);
        buffersize = intent.getIntExtra("buffersize", 2000);
        isplaylist = intent.getBooleanExtra("isplaylist", false);
        queue = intent.getStringArrayListExtra("queue");
        names = intent.getStringArrayListExtra("names");
        getSupportActionBar().setTitle(title);
        if (isplaylist) {
            player.clearMediaItems();
            for (int i = 0; i < queue.size(); i++) {
                if (names.get(i).endsWith(".m3u8")) {
                    MediaItem mediaItem = MediaItem.fromUri(queue.get(i));
                    player.addMediaItem(mediaItem);
                } else {
                    mediaSource = new ProgressiveMediaSource.Factory(
                            new CacheDataSource.Factory()
                                    .setCache(simpleCache)
                                    .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                            .setUserAgent("S3 Manager"))
                                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    ).createMediaSource(MediaItem.fromUri(Uri.parse(queue.get(i))));
                    player.addMediaSource(mediaSource);
                }
            }
            player.prepare();
            player.seekTo(names.indexOf(title), 0);
        } else {
            if (title.endsWith(".m3u8")) {
                MediaItem mediaItem = MediaItem.fromUri(videoURL);
                player.setMediaItem(mediaItem);
            } else {
                mediaSource = new ProgressiveMediaSource.Factory(
                        new CacheDataSource.Factory()
                                .setCache(simpleCache)
                                .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                        .setUserAgent("S3 Manager"))
                                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                ).createMediaSource(MediaItem.fromUri(Uri.parse(videoURL)));
                player.setMediaSource(mediaSource);
            }
            player.prepare();
        }
        // Start the playback.
        player.play();
        super.onNewIntent(intent);
    }

    public void onStop() {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && this.getPackageManager()
                    .hasSystemFeature(
                            PackageManager.FEATURE_PICTURE_IN_PICTURE) && appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    this.getPackageManager().getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA).uid, this.getPackageName())
                    == AppOpsManager.MODE_ALLOWED && isScreenOn(this) && this.isInPictureInPictureMode()) {
                finish();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    static void deleteCache(Context context, StandaloneDatabaseProvider database) {
        SimpleCache.delete(new File(context.getCacheDir(), "media"), database);
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