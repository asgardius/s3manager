package asgardius.page.s3manager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.List;

public class BucketSelect extends AppCompatActivity {

    ArrayList Name;
    ArrayList Img;
    RecyclerView recyclerView;
    String username, password, endpoint, prefix, location, pdfendpoint;
    boolean style, isplaylist;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    int videocache, videotime, buffersize, treelevel, playlisttime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        location = getIntent().getStringExtra("region");
        pdfendpoint = getIntent().getStringExtra("pdfendpoint");
        style = getIntent().getBooleanExtra("style", false);
        videocache = getIntent().getIntExtra("videocache", 40);
        videotime = getIntent().getIntExtra("videotime", 1);
        playlisttime = getIntent().getIntExtra("playlisttime", 1);
        buffersize = getIntent().getIntExtra("buffersize", 2000);
        isplaylist = getIntent().getBooleanExtra("isplaylist", false);
        prefix = "";
        setContentView(R.layout.activity_bucket_select);
        if (endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            region = Region.getRegion("us-east-1");
        } else {
            region = Region.getRegion(location);
        }
        s3ClientOptions = S3ClientOptions.builder().build();
        s3ClientOptions.setPathStyleAccess(style);
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);

        recyclerView = findViewById(R.id.blist);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

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
                    if (Name.size() == 0) {
                        Name.add(getResources().getString(R.string.create_bucket));
                        //Img.add(R.drawable.ic_launcher_foreground);
                        Img.add(R.drawable.create_new);
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
                            Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        listbucket.start();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //System.out.println("Click on "+Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.bucket)) {
                    //go to bucket content
                    explorer(Name.get(position).toString());
                } else {
                    newBucket();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //System.out.println("Long click on "+Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.bucket)) {
                    PopupMenu popupMenu = new PopupMenu(recyclerView.getContext(), view);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.bucket_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Toast message on menu item clicked
                            //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            if (menuItem.getTitle() == getResources().getString(R.string.upload_file_tobucket)) {
                                //Toast.makeText(BucketSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                upload(Name.get(position).toString(), false);

                            } else if (menuItem.getTitle() == getResources().getString(R.string.upload_folder_tobucket)) {
                                //Toast.makeText(BucketSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                upload(Name.get(position).toString(), true);

                            } else if (menuItem.getTitle() == getResources().getString(R.string.create_link)) {
                                share(Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.download_bucket)) {
                                //Toast.makeText(BucketSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                download(Name.get(position).toString());

                            } else if (menuItem.getTitle() == getResources().getString(R.string.create_bucket)) {
                                //upload();
                                newBucket();
                                //System.out.println(file);
                                //Toast.makeText(BucketSelect.this, intent.getData().toString(), Toast.LENGTH_SHORT).show();

                            } else if (menuItem.getTitle() == getResources().getString(R.string.object_info)) {
                                objectInfo(Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.copy_name)) {
                                copyName(Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.cors_config)) {
                                corsConfig(Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                //Toast.makeText(BucketSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                delete(prefix + Name.get(position).toString());
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
        intent.putExtra("pdfendpoint", pdfendpoint);
        intent.putExtra("style", style);
        intent.putExtra("videocache", videocache);
        intent.putExtra("videotime", videotime);
        intent.putExtra("buffersize", buffersize);
        intent.putExtra("playlisttime", playlisttime);
        intent.putExtra("isplaylist", isplaylist);
        startActivity(intent);

    }

    private void share(String bucket) {

        Intent intent = new Intent(this, Share.class);
        //treelevel ++;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("title", bucket);
        intent.putExtra("region", location);
        intent.putExtra("videotime", videotime);
        intent.putExtra("playlisttime", playlisttime);
        intent.putExtra("style", style);
        startActivity(intent);

    }

    private void delete(String bucket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BucketSelect.this);
        builder.setCancelable(true);
        builder.setTitle(bucket);
        builder.setMessage(getResources().getString(R.string.bucket_del_confirm));
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                        Thread deleteBucket = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    //Your code goes here
                                    ListObjectsRequest orequest = new ListObjectsRequest().withBucketName(bucket).withMaxKeys(1000);
                                    //List<S3Object> objects = (List<S3Object>) s3client.listObjects(bucket, "/");
                                    ObjectListing result = s3client.listObjects(orequest);
                                    ArrayList<String> objectl = new ArrayList<String>();
                                    List<S3ObjectSummary> objects = result.getObjectSummaries();
                                    for (S3ObjectSummary os : objects) {
                                        objectl.add(os.getKey());
                                    }
                                    if (objectl.size() >= 1) {
                                        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(objectl.toArray(new String[0]));
                                        s3client.deleteObjects(deleteObjectsRequest);
                                    }
                                    while (result.isTruncated()) {
                                        objectl = new ArrayList<String>();
                                        result = s3client.listNextBatchOfObjects (result);
                                        objects = result.getObjectSummaries();
                                        for (S3ObjectSummary os : objects) {
                                            objectl.add(os.getKey());
                                            //i++;
                                        }
                                        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(objectl.toArray(new String[0]));
                                        s3client.deleteObjects(deleteObjectsRequest);

                                    }
                                    //System.out.println("bucket items: " + objectl.size());
                                    s3client.deleteBucket(bucket);
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            // Sending reference and data to Adapter
                                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.bucket_del_success), Toast.LENGTH_SHORT).show();
                                            recreate();

                                        }
                                    });
                                    //System.out.println("tree "+treelevel);
                                    //System.out.println("prefix "+prefix);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                        simpleProgressBar.setVisibility(View.VISIBLE);
                        deleteBucket.start();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void upload(String bucket, boolean isfolder) {
        Intent intent = new Intent(this, Uploader.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix);
        intent.putExtra("region", location);
        intent.putExtra("style", style);
        intent.putExtra("isfolder", isfolder);
        startActivity(intent);
    }

    private void objectInfo(String bucket) {

        Intent intent = new Intent(this, ObjectInfo.class);
        //treelevel ++;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("region", location);
        intent.putExtra("style", style);
        startActivity(intent);

    }

    private void newBucket() {
        Intent intent = new Intent(this, CreateBucket.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("region", location);
        intent.putExtra("style", style);
        //startActivity(intent);
        ((Activity) this).startActivityForResult(intent, 25);
    }

    private void download(String bucket) {

        Intent intent = new Intent(this, Downloader.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("prefix", prefix);
        intent.putExtra("region", location);
        intent.putExtra("style", style);
        intent.putExtra("bucket", bucket);
        intent.putExtra("isfolder", true);
        startActivity(intent);
    }

    private void corsConfig(String bucket) {
        Intent intent = new Intent(this, CorsConfig.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("region", location);
        intent.putExtra("style", style);
        intent.putExtra("pdfendpoint", pdfendpoint);
        startActivity(intent);
    }

    public void copyName (String name) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("name", name);
        clipboard.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.copy_ok), Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code OPEN_DIRECTORY_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.
        super.onActivityResult(requestCode, resultCode, resultData);
        if(requestCode == 25 && resultCode == 25)  {
            recreate();
        }
    }

}