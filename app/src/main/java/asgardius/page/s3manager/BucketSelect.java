package asgardius.page.s3manager;

import static com.amazonaws.regions.Regions.US_EAST_1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

public class BucketSelect extends AppCompatActivity {

    AmazonS3 s3client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_select);
        Region region = Region.getRegion(US_EAST_1);
        S3ClientOptions s3ClientOptions = S3ClientOptions.builder().build();
        s3ClientOptions.setPathStyleAccess(true);
        AWSCredentials myCredentials = new BasicAWSCredentials(getResources().getString(R.string.access_key), getResources().getString(R.string.secret_key));
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(getResources().getString(R.string.endpoint_url));
        s3client.setS3ClientOptions(s3ClientOptions);
        listbucket list = new listbucket();
        list.execute("test");
    }

    private class listbucket extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            // do above Server call here
            System.out.println(s3client.listBuckets());
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }
}