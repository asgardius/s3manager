package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class TextViewer extends AppCompatActivity {
    EditText filecontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        filecontent = (EditText)findViewById(R.id.textShow);

        try {
            // Create a URL for the desired page
            URL url = new URL("yoursite.com/thefile.txt");

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = in.readLine();
            in.close();
            filecontent.setText(str);
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_conn_fail), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_conn_fail), Toast.LENGTH_SHORT).show();
        }
    }
}