package asgardius.page.s3manager;

import static com.amazonaws.regions.Regions.US_EAST_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;

import java.util.ArrayList;
import java.util.List;

public class BucketSelect extends AppCompatActivity {

    ArrayList Name;
    ArrayList Img;
    RecyclerView recyclerView;
    String username, password, endpoint, prefix, location;
    int treelevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        location = getIntent().getStringExtra("region");
        prefix = "";
        setContentView(R.layout.activity_bucket_select);
        Region region = Region.getRegion(location);
        S3ClientOptions s3ClientOptions = S3ClientOptions.builder().build();
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        AWSCredentials myCredentials = new BasicAWSCredentials(username, password);
        AmazonS3 s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);

        recyclerView = findViewById(R.id.blist);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        Thread listbucket = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    List<Bucket> buckets = s3client.listBuckets();
                    //This convert bucket list to an array list
                    Name = new ArrayList<String>();
                    Img = new ArrayList<String>();
                    // Print bucket names
                    //System.out.println("Buckets:");
                    for (Bucket bucket : buckets) {
                        //i++;
                        //System.out.println(bucket.getName());
                        Name.add(bucket.getName());
                        //Img.add(R.drawable.ic_launcher_foreground);
                        Img.add(R.drawable.bucket);
                    }
                    //System.out.println(Name);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            Adapter adapter = new Adapter(BucketSelect.this, Img, Name);
                            simpleProgressBar.setVisibility(View.INVISIBLE);

                            // Setting Adapter to RecyclerView
                            recyclerView.setAdapter(adapter);
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

        listbucket.start();
        //listbucket list = new listbucket();
        //list.execute("test");
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                System.out.println("Click on "+Name.get(position).toString());
                explorer(Name.get(position).toString());
            }

            @Override
            public void onLongClick(View view, int position) {
                System.out.println("Long click on "+Name.get(position).toString());
            }
        }));
    }

    private void explorer(String bucket) {

        Intent intent = new Intent(this, ObjectSelect.class);
        treelevel = 0;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix);
        intent.putExtra("treelevel", treelevel);
        intent.putExtra("region", location);
        startActivity(intent);

    }
}