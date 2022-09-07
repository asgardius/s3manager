package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TextViewer extends AppCompatActivity {
    EditText filecontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        filecontent = (EditText)findViewById(R.id.textShow);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        String videoURL = getIntent().getStringExtra("video_url");
        Thread textread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    URL fileurl = new URL(videoURL);

                    // Read all the text returned by the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(fileurl.openStream()));
                    String str = in.lines().collect(Collectors.joining());
                    in.close();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            simpleProgressBar.setVisibility(View.INVISIBLE);
                            filecontent.setText(str);

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
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        textread.start();

        /*try {
            // Create a URL for the desired page
            URL fileurl = new URL(videoURL);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(fileurl.openStream()));
            String str = in.readLine();
            in.close();
            filecontent.setText(str);
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_conn_fail), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_conn_fail), Toast.LENGTH_SHORT).show();
        }*/
    }
}