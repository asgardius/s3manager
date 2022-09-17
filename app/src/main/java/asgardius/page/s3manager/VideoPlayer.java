package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.HttpDataSource;

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

    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        // create Wifi and wake locks
        mWifiLock = ((WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Transistor:wifi_lock");
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Transistor:wake_lock");
        //Get media url
        String videoURL = getIntent().getStringExtra("video_url");
        playerView = findViewById(R.id.player_view);
        // creating a variable for exoplayer
        player = new ExoPlayer.Builder(this).build();
        // Attach player to the view.
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(videoURL);

        // Set the media item to be played.
        player.setMediaItem(mediaItem);
        // Prepare the player.
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);


        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    public void onDestroy() {
        player.release();
        super.onDestroy();

    }

    public void onBackPressed() {
        player.release();
        finish();
    }
}