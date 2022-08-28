package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayer extends AppCompatActivity {

    // creating a variable for exoplayerview.
    protected StyledPlayerView playerView;


    // url of video which we are loading.
    String videoURL = "https://video.asgardius.company/download/videos/41780585-a935-4d53-84c8-45ce97141231-480.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        playerView = findViewById(R.id.player_view);
        try {
            // creating a variable for exoplayer
            ExoPlayer player = new ExoPlayer.Builder(this).build();
            // Attach player to the view.
            playerView.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(videoURL);

            // Set the media item to be played.
            player.setMediaItem(mediaItem);
            // Prepare the player.
            player.prepare();
            // Start the playback.
            player.play();

        } catch (Exception e) {
            // below line is used for
            // handling our errors.
            System.out.println("Error : " + e.toString());
        }
    }
}