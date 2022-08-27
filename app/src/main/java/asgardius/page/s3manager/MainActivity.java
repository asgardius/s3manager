package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //This is to launch video playback test
        Button videotest = (Button)findViewById(R.id.vtest);
        videotest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                videoplayer();
            }
        });
    }

    private void videoplayer() {

        Intent intent = new Intent(this, VideoPlayer.class);

        startActivity(intent);

    }
}