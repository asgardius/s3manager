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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        String videoURL = getIntent().getStringExtra("video_url");
        ImageView iv=(ImageView) findViewById(R.id.imageViewer);
        System.out.println(videoURL);
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
                            // Sending reference and data to Adapter
                            simpleProgressBar.setVisibility(View.INVISIBLE);
                            iv.setImageDrawable(thumb_d);

                        }
                    });
                    //System.out.println("tree "+treelevel);
                    //System.out.println("prefix "+prefix);

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        imgread.start();
    }
}