package asgardius.page.s3manager;

import static com.amazonaws.regions.Regions.US_EAST_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;

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

    AmazonS3 s3client;
    ArrayList Name;
    ArrayList Img;
    RecyclerView recyclerView;

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

        recyclerView = findViewById(R.id.recyclerview);

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
                    int i=0;
                    for (Bucket bucket : buckets) {
                        //i++;
                        //System.out.println(bucket.getName());
                        Name.add(bucket.getName());
                        //Img.add(R.drawable.ic_launcher_foreground);
                        Img.add(R.drawable.bucket);
                    }
                    System.out.println(Name);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            Adapter adapter = new Adapter(BucketSelect.this, Img, Name);

                            // Setting Adapter to RecyclerView
                            recyclerView.setAdapter(adapter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        listbucket.start();
        //listbucket list = new listbucket();
        //list.execute("test");
    }
}