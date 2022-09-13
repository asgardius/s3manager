package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class ImageViewer extends AppCompatActivity {
    String videoURL;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        iv = (ImageView) findViewById(R.id.imageViewer);
        //System.out.println(videoURL);
        Thread imgread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    videoURL = getIntent().getStringExtra("video_url");
                    URL thumb_u = new URL(videoURL);
                    Drawable thumb_d = Drawable.createFromStream(thumb_u.openStream(), "src");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            simpleProgressBar.setVisibility(View.INVISIBLE);
                            iv.setImageDrawable(thumb_d);

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    finish();
                }
            }
        });
        imgread.start();
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
}