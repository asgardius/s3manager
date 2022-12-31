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
    String videoURL, title;
    ImageView iv;
    boolean controls = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        videoURL = getIntent().getStringExtra("video_url");
        title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        iv = (ImageView) findViewById(R.id.imageViewer);
        //System.out.println(videoURL);
        Thread imgread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    URL thumb_u = new URL(videoURL);
                    Drawable thumb_d = Drawable.createFromStream(thumb_u.openStream(), "src");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            simpleProgressBar.setVisibility(View.INVISIBLE);
                            iv.setImageDrawable(thumb_d);

                        }
                    });

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.picture_too_big), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    finish();
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
        iv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                if(controls) {
                    controls = false;
                    hideSystemBars();
                }
                else {
                    controls = true;
                    showSystemBars();
                }
            }

        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        controls = false;
        hideSystemBars();

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
}