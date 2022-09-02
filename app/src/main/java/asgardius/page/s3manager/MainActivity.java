package asgardius.page.s3manager;

import static com.amazonaws.regions.RegionUtils.getRegion;
import static com.amazonaws.regions.Regions.US_EAST_1;
import static com.amazonaws.services.s3.S3ClientOptions.DEFAULT_PATH_STYLE_ACCESS;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    static boolean DEFAULT_PATH_STYLE_ACCESS = true;
    String username, password, endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = getString(R.string.access_key);
        password = getResources().getString(R.string.secret_key);
        endpoint = getResources().getString(R.string.endpoint_url);
        //This is to launch video playback test
        Button videotest = (Button)findViewById(R.id.vtest);
        videotest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                s3test();
                //videoplayer("https://video.asgardius.company/download/videos/41780585-a935-4d53-84c8-45ce97141231-480.mp4");
            }
        });

        //This is to launch file explorer test
        Button explorertest = (Button)findViewById(R.id.ltest);
        explorertest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                explorer();
                //videoplayer("https://video.asgardius.company/download/videos/41780585-a935-4d53-84c8-45ce97141231-480.mp4");
            }
        });
    }

    private void s3test() {

        Region region = Region.getRegion(US_EAST_1);
        S3ClientOptions s3ClientOptions = S3ClientOptions.builder().build();
        s3ClientOptions.setPathStyleAccess(true);
        AWSCredentials myCredentials = new BasicAWSCredentials(username, password);
        AmazonS3 s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        //s3client.setRegion(getRegion("asteroid"));
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(getResources().getString(R.string.bucketname), getResources().getString(R.string.objectname));
        URL objectURL = s3client.generatePresignedUrl(request);
        videoplayer(objectURL.toString());

    }

    private void videoplayer(String url) {

        Intent intent = new Intent(this, VideoPlayer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }
    private void explorer() {

        Intent intent = new Intent(this, BucketSelect.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);

    }

}