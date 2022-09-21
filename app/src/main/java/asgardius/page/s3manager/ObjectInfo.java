package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.List;

public class ObjectInfo extends AppCompatActivity {
    String username, password, endpoint, bucket, object, location;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    TextView filesize, filesizeinfo;
    boolean isobject, isfolder;
    long totalSize = 0;
    int totalItems = 0;
    ListObjectsRequest orequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_info);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        filesize = (TextView) findViewById(R.id.size);
        filesizeinfo = (TextView) findViewById(R.id.size_info);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        object = getIntent().getStringExtra("object");
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        myCredentials = new BasicAWSCredentials(username, password);
        try {
            s3client = new AmazonS3Client(myCredentials, region);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
        }
        s3client.setEndpoint(endpoint);
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        s3client.setS3ClientOptions(s3ClientOptions);
        Thread getInfo = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    if (object == null) {
                        isobject = false;
                        orequest = new ListObjectsRequest().withBucketName(bucket).withMaxKeys(8000);
                    } else {
                        isobject = true;
                        if (object.endsWith("/")) {
                            isfolder = true;
                        } else {
                            isfolder = false;
                        }
                        orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(object).withMaxKeys(8000);
                    }
                    ObjectListing result = s3client.listObjects(orequest);
                    do {
                        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                            totalSize += objectSummary.getSize();
                            totalItems++;
                        }
                        result = s3client.listNextBatchOfObjects (result);
                    } while (result.isTruncated());


                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (isobject) {
                                if (isfolder) {
                                    filesizeinfo.setText(getResources().getString(R.string.folder_size));
                                } else {
                                    filesizeinfo.setText(getResources().getString(R.string.file_size));
                                }
                            } else {
                                filesizeinfo.setText(getResources().getString(R.string.bucket_size));
                            }
                            filesize.setText(Long.toString(totalSize));
                            simpleProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

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
        getInfo.start();
    }
}