package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.List;

public class CreateBucket extends AppCompatActivity {
    String username, password, endpoint, bucket, location;
    EditText bname;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bucket);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        location = getIntent().getStringExtra("region");
        region = Region.getRegion("us-east-1");
        s3ClientOptions = S3ClientOptions.builder().build();
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        bname  = (EditText)findViewById(R.id.bname);
        Button cbucket = (Button)findViewById(R.id.cbucket);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        cbucket.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                bucket = bname.getText().toString();
                if (bucket.equals("")) {
                    Toast.makeText(CreateBucket.this, getResources().getString(R.string.bucket_name_empty), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(CreateBucket.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                    Thread newBucket = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                //Your code goes here
                                s3client.createBucket(bucket, location);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Sending reference and data to Adapter
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.create_bucket_success), Toast.LENGTH_SHORT).show();
                                        mainmenu();

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
                    simpleProgressBar.setVisibility(View.VISIBLE);
                    newBucket.start();
                }
            }

        });

    }

    private void mainmenu() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);

    }
}