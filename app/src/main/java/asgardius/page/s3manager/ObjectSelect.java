package asgardius.page.s3manager;

import static com.amazonaws.regions.Regions.US_EAST_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ObjectSelect extends AppCompatActivity {

    ArrayList Name;
    ArrayList Img;
    //ArrayList object;
    RecyclerView recyclerView;
    String username, password, endpoint, bucket, prefix;
    int treelevel;
    String[] filename, path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        prefix = getIntent().getStringExtra("prefix");
        treelevel = getIntent().getIntExtra("treelevel", 0);
        setContentView(R.layout.activity_object_select);
        Region region = Region.getRegion(US_EAST_1);
        S3ClientOptions s3ClientOptions = S3ClientOptions.builder().build();
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        AWSCredentials myCredentials = new BasicAWSCredentials(username, password);
        AmazonS3 s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);

        recyclerView = findViewById(R.id.olist);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        Thread listobject = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    //List<Bucket> buckets = s3client.listBuckets();
                    ListObjectsRequest orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix).withMaxKeys(8000);
                    //List<S3Object> objects = (List<S3Object>) s3client.listObjects(bucket, "/");
                    ObjectListing result = s3client.listObjects(orequest);
                    //System.out.println(objects);
                    //This convert bucket list to an array list
                    Img = new ArrayList<String>();
                    LinkedHashSet<String> object = new LinkedHashSet<String>();
                    // Print bucket names
                    //System.out.println("Buckets:");
                    //int i=0;
                    List<S3ObjectSummary> objects = result.getObjectSummaries();
                    boolean nextbatch = false;
                    while (result.isTruncated() || !nextbatch) {
                        if (nextbatch) {
                            result = s3client.listNextBatchOfObjects (result);
                            objects = result.getObjectSummaries();
                        } else {
                            nextbatch = true;
                        }
                        for (S3ObjectSummary os : objects) {
                            filename = os.getKey().split("/");
                            if (filename.length == treelevel+1) {
                                object.add(filename[treelevel]);
                            }
                            else {
                                object.add(filename[treelevel]+"/");
                            }

                            //i++;
                        }

                    }

                    Name = new ArrayList<String>(object);
                    object.clear();
                    //Img.add(R.drawable.unknownfile);
                    //This set object icon based on its filetype
                    int i = 0;
                    while(i<Name.size()) {
                        //Img.add(R.drawable.unknownfile);
                        if (Name.get(i).toString().endsWith("/")) {
                            Img.add(R.drawable.folder);
                        }
                        else if (Name.get(i).toString().endsWith(".txt") || Name.get(i).toString().endsWith(".md")) {
                            Img.add(R.drawable.textfile);
                        }
                        else if (Name.get(i).toString().endsWith(".opus") || Name.get(i).toString().endsWith(".ogg")
                                || Name.get(i).toString().endsWith(".oga") || Name.get(i).toString().endsWith(".mp3")
                                || Name.get(i).toString().endsWith(".m4a") || Name.get(i).toString().endsWith(".flac")
                                || Name.get(i).toString().endsWith(".mka")) {
                            Img.add(R.drawable.audiofile);
                        }
                        else if(Name.get(i).toString().endsWith(".mp4") || Name.get(i).toString().endsWith(".mkv")
                                || Name.get(i).toString().endsWith(".webm") || Name.get(i).toString().endsWith(".m4v")) {
                            Img.add(R.drawable.videofile);
                        }
                        else if (Name.get(i).toString().endsWith(".htm") || Name.get(i).toString().endsWith(".html")) {
                            Img.add(R.drawable.webpage);
                        }
                        else {
                            Img.add(R.drawable.unknownfile);
                        }
                        i++;
                    }



                    /*for (Bucket bucket : buckets) {
                        //i++;
                        //System.out.println(bucket.getName());
                        Name.add(bucket.getName());
                        //Img.add(R.drawable.ic_launcher_foreground);
                        Img.add(R.drawable.videofile);
                    }*/
                    //System.out.println(Name);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            Adapter adapter = new Adapter(ObjectSelect.this, Img, Name);
                            simpleProgressBar.setVisibility(View.INVISIBLE);

                            // Setting Adapter to RecyclerView
                            recyclerView.setAdapter(adapter);
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
        listobject.start();
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //System.out.println("Click on "+Name.get(position).toString());
                //explorer(Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.folder)) {
                    //go to subfolder
                    explorer(Name.get(position).toString());
                } else if (Img.get(position).equals(R.drawable.textfile)) {
                    //load media file
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                    URL objectURL = s3client.generatePresignedUrl(request);
                    textviewer(objectURL.toString());
                } else if (Img.get(position).equals(R.drawable.webpage)) {
                    //load media file
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                    URL objectURL = s3client.generatePresignedUrl(request);
                    webbrowser(objectURL.toString());
                } else if (Img.get(position).equals(R.drawable.audiofile) || Img.get(position).equals(R.drawable.videofile)) {
                    //load media file
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                    URL objectURL = s3client.generatePresignedUrl(request);
                    videoplayer(objectURL.toString());
                }  else {
                    Toast.makeText(ObjectSelect.this, getResources().getString(R.string.unsupported_file), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //System.out.println("Long click on "+Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.folder)) {
                    //go to subfolder
                    //explorer(Name.get(position).toString());
                } else {
                    // Initializing the popup menu and giving the reference as current context
                    PopupMenu popupMenu = new PopupMenu(ObjectSelect.this, recyclerView);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.object_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Toast message on menu item clicked
                            //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            if (menuItem.getTitle() == getResources().getString(R.string.file_share)) {
                                share(Name.get(position).toString());
                            }
                            return true;
                        }
                    });
                    // Showing the popup menu
                    popupMenu.show();
                }
            }
        }));
    }

    private void videoplayer(String url) {

        Intent intent = new Intent(this, VideoPlayer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }
    private void textviewer(String url) {

        Intent intent = new Intent(this, TextViewer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }

    private void webbrowser (String url) {

        Intent intent = new Intent(this, WebBrowser.class);
        intent.putExtra("web_url", url);
        startActivity(intent);

    }

    private void explorer(String object) {

        Intent intent = new Intent(this, ObjectSelect.class);
        //treelevel ++;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix + object);
        intent.putExtra("treelevel", treelevel+1);
        startActivity(intent);

    }

    private void share(String object) {

        Toast.makeText(ObjectSelect.this, "This feature is not yet implemented", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, FileShare.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix + object);
        //intent.putExtra("treelevel", treelevel+1);
        startActivity(intent);

    }
}