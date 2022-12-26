package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

public class CorsConfig extends AppCompatActivity {
    String username, password, endpoint, bucket, location, title, pdfendpoint;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    BucketCrossOriginConfiguration bucketcors;
    boolean style;
    boolean allorigins, pdfcompatible, found = false;
    TextView origins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cors_config);
        endpoint = getIntent().getStringExtra("endpoint");
        title = getIntent().getStringExtra("title");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        style = getIntent().getBooleanExtra("style", false);
        location = getIntent().getStringExtra("region");
        pdfendpoint = getIntent().getStringExtra("pdfendpoint");
        getSupportActionBar().setTitle(bucket+"/");
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        myCredentials = new BasicAWSCredentials(username, password);
        origins = (TextView) findViewById(R.id.origins);
        try {
            s3client = new AmazonS3Client(myCredentials, region);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
        }
        s3client.setEndpoint(endpoint);
        s3ClientOptions.setPathStyleAccess(style);
        s3client.setS3ClientOptions(s3ClientOptions);
        Thread getCors = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    bucketcors = s3client.getBucketCrossOriginConfiguration(bucket);
                    if (bucketcors != null) {
                        List<CORSRule> corsRules = bucketcors.getRules();
                        if (!corsRules.isEmpty()) {
                            found = true;
                            for (CORSRule rule: corsRules) {
                                System.out.println("allowOrigins: "+rule.getAllowedOrigins());
                                System.out.println("AllowedMethod: "+rule.getAllowedMethods());
                                if (rule.getAllowedOrigins().toString().equals("[*]")) {
                                    allorigins = true;
                                } else if (rule.getAllowedOrigins().toString().equals("["+pdfendpoint+"]")) {
                                    pdfcompatible = true;
                                }
                            }
                        }
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (allorigins) {
                                origins.setText(getResources().getString(R.string.cors_all));
                            } else if (pdfcompatible) {
                                origins.setText(getResources().getString(R.string.cors_pdf));
                            } else if (found) {
                                origins.setText(getResources().getString(R.string.cors_npdf));
                            } else {
                                origins.setText(getResources().getString(R.string.cors_none));
                            }
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
        getCors.start();
    }
}