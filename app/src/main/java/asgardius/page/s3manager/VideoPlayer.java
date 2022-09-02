package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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


    // url of video which we are loading.
    //String videoURL = "https://video.asgardius.company/download/videos/41780585-a935-4d53-84c8-45ce97141231-480.mp4";

    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
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
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_conn_fail), Toast.LENGTH_SHORT).show();
                }
                else {
                    // An HTTP error occurred.
                    //System.out.println("Playback error F");
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_wrong_type), Toast.LENGTH_SHORT).show();
                }
                player.release();
                finish();
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

    public void onBackPressed() {
        player.release();
        finish();
    }
}