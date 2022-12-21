package asgardius.page.s3manager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class ObjectSelect extends AppCompatActivity {

    ArrayList Name;
    ArrayList Img;
    //ArrayList object;
    RecyclerView recyclerView;
    String username, password, endpoint, bucket, prefix, location, pdfendpoint, query;
    int treelevel;
    String[] filename;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    int videocache, videotime, buffersize;
    AppOpsManager appOpsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        pdfendpoint = getIntent().getStringExtra("pdfendpoint");
        prefix = getIntent().getStringExtra("prefix");
        treelevel = getIntent().getIntExtra("treelevel", 0);
        videocache = getIntent().getIntExtra("videocache", 40);
        videotime = getIntent().getIntExtra("videotime", 1);
        buffersize = getIntent().getIntExtra("buffersize", 2000);
        appOpsManager = (AppOpsManager)getSystemService(Context.APP_OPS_SERVICE);
        setContentView(R.layout.activity_object_select);
        getSupportActionBar().setTitle(bucket+"/"+prefix);
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


        recyclerView = findViewById(R.id.olist);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        Thread listobject = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    //List<Bucket> buckets = s3client.listBuckets();
                    ListObjectsRequest orequest;
                    if (treelevel == 0) {
                        orequest = new ListObjectsRequest().withBucketName(bucket).withMaxKeys(1000).withDelimiter("/");
                    } else {
                        orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix).withMaxKeys(1000).withDelimiter("/");
                    }
                    //List<S3Object> objects = (List<S3Object>) s3client.listObjects(bucket, "/");
                    ObjectListing result = s3client.listObjects(orequest);
                    //System.out.println(objects);
                    //This convert bucket list to an array list
                    Img = new ArrayList<String>();
                    ArrayList<String> object = new ArrayList<String>();
                    // Print bucket names
                    //System.out.println("Buckets:");
                    //int i=0;
                    //This get folder list
                    List<String> od = result.getCommonPrefixes();
                    for (String os : od) {
                        filename = os.split("/");
                        if (filename.length == treelevel+1) {
                            object.add(filename[treelevel]+"/");
                        }
                        else {

                        }

                        //i++;
                    }
                    while (result.isTruncated()) {
                        result = s3client.listNextBatchOfObjects (result);
                        od = result.getCommonPrefixes();
                        for (String os : od) {
                            filename = os.split("/");
                            if (filename.length == treelevel+1) {
                                object.add(filename[treelevel]+"/");
                            }
                            else {

                            }

                            //i++;
                        }

                    }
                    //This get file list
                    List<S3ObjectSummary> ob = result.getObjectSummaries();
                    for (S3ObjectSummary os : ob) {
                        filename = os.getKey().split("/");
                        if (filename.length == treelevel+1) {
                            object.add(filename[treelevel]);
                        }
                        else {
                            object.add(filename[treelevel]+"/");
                        }

                        //i++;
                    }
                    while (result.isTruncated()) {
                        result = s3client.listNextBatchOfObjects (result);
                        ob = result.getObjectSummaries();
                        for (S3ObjectSummary os : ob) {
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
                        else if (Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".txt") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".md")) {
                            Img.add(R.drawable.ptextfile);
                        }
                        else if (Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                            Img.add(R.drawable.pdffile);
                        }
                        else if (Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".jpg") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".jpeg") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".png") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".gif")) {
                            Img.add(R.drawable.imagefile);
                        }
                        else if (Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".opus") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".ogg")
                                || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".oga") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".mp3")
                                || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".m4a") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".flac")
                                || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".mka")) {
                            Img.add(R.drawable.audiofile);
                        }
                        else if(Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".mp4") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".mkv")
                                || Name.get(i).toString().endsWith(".webm") || Name.get(i).toString().endsWith(".m4v") || Name.get(i).toString().endsWith(".m3u8")) {
                            Img.add(R.drawable.videofile);
                        }
                        else if (Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".htm") || Name.get(i).toString().toLowerCase(Locale.ROOT).endsWith(".html")) {
                            Img.add(R.drawable.webpage);
                        }
                        else {
                            Img.add(R.drawable.unknownfile);
                        }
                        i++;
                    }

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
                } else if (Img.get(position).equals(R.drawable.imagefile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        imageViewer(objectURL.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                } /*else if (Img.get(position).equals(R.drawable.textfile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        textViewer(objectURL.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                }*/ else if (Img.get(position).equals(R.drawable.webpage) || Img.get(position).equals(R.drawable.ptextfile)) {
                    //load media file
                    try {
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                        URL objectURL = s3client.generatePresignedUrl(request);
                        webBrowser(objectURL.toString(), Name.get(position).toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    }
                } else if (Img.get(position).equals(R.drawable.pdffile)) {
                    //load media file
                    Thread pdfread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                //Your code goes here
                                GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString());
                                URL objectURL = s3client.generatePresignedUrl(request);
                                //System.out.println(getResources().getString(R.string.pdf_reader)+ URLEncoder.encode(objectURL.toString(), "UTF-8" ));
                                if (pdfendpoint.endsWith("/")) {
                                    query = pdfendpoint + "web/viewer.html?file=" + URLEncoder.encode(objectURL.toString(), "UTF-8" );
                                } else {
                                    query = pdfendpoint + "/web/viewer.html?file=" + URLEncoder.encode(objectURL.toString(), "UTF-8" );
                                }

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Sending reference and data to Adapter
                                        webBrowser(query, Name.get(position).toString());
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
                    if (pdfendpoint == null || pdfendpoint.equals("")) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.pdf_reader_notready), Toast.LENGTH_SHORT).show();
                    } else {
                        pdfread.start();
                    }
                } else if (Img.get(position).equals(R.drawable.audiofile) || Img.get(position).equals(R.drawable.videofile)) {
                    Thread mediaread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                //load media file
                                Date expiration = new Date();
                                Calendar mycal = Calendar.getInstance();
                                mycal.setTime(expiration);
                                //System.out.println("today is " + mycal.getTime());
                                mycal.add(Calendar.HOUR, videotime);
                                //System.out.println("Expiration date: " + mycal.getTime());
                                expiration = mycal.getTime();
                                GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, prefix + Name.get(position).toString()).withExpiration(expiration);;
                                URL objectURL = s3client.generatePresignedUrl(request);

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Sending reference and data to Adapter
                                        videoPlayer(objectURL.toString(), Name.get(position).toString().endsWith(".m3u8"));
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
                    mediaread.start();
                }  else {
                    Toast.makeText(ObjectSelect.this, getResources().getString(R.string.unsupported_file), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //System.out.println("Long click on "+Name.get(position).toString());
                if (Img.get(position).equals(R.drawable.folder)) {
                    //go to subfolder
                    /// Initializing the popup menu and giving the reference as current context
                    PopupMenu popupMenu = new PopupMenu(recyclerView.getContext(), view);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.folder_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Toast message on menu item clicked
                            //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            if (menuItem.getTitle() == getResources().getString(R.string.upload_file_here)) {
                                //Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                upload();
                            } else if (menuItem.getTitle() == getResources().getString(R.string.object_info)) {
                                objectInfo(prefix + Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                if (Name.size() == 1 && treelevel >= 1) {
                                    Toast.makeText(ObjectSelect.this, getResources().getString(R.string.only_item_onlist), Toast.LENGTH_SHORT).show();
                                } else {
                                    delete(prefix + Name.get(position).toString(), true);
                                }
                            }
                            return true;
                        }
                    });
                    // Showing the popup menu
                    popupMenu.show();
                } else {
                    // Initializing the popup menu and giving the reference as current context
                    PopupMenu popupMenu = new PopupMenu(recyclerView.getContext(), view);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.object_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Toast message on menu item clicked
                            //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            if (menuItem.getTitle() == getResources().getString(R.string.download_file)) {
                                //Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                download(Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.upload_file_here)) {
                                //Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                                upload();
                            } else if (menuItem.getTitle() == getResources().getString(R.string.create_link)) {
                                if (Img.get(position).equals(R.drawable.audiofile) || Img.get(position).equals(R.drawable.videofile)) {
                                    share(prefix + Name.get(position).toString(), true);
                                } else {
                                    share(prefix + Name.get(position).toString(), false);
                                }
                            } else if (menuItem.getTitle() == getResources().getString(R.string.object_info)) {
                                objectInfo(prefix + Name.get(position).toString());
                            } else if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                if (menuItem.getTitle() == getResources().getString(R.string.file_del)) {
                                    if (Name.size() == 1 && treelevel >= 1) {
                                        Toast.makeText(ObjectSelect.this, getResources().getString(R.string.only_item_onlist), Toast.LENGTH_SHORT).show();
                                    } else {
                                        delete(prefix + Name.get(position).toString(), false);
                                    }
                                }
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

    private void videoPlayer(String url, boolean hls) {

        Intent intent = new Intent(this, VideoPlayer.class);
        intent.putExtra("video_url", url);
        intent.putExtra("videocache", videocache);
        intent.putExtra("buffersize", buffersize);
        intent.putExtra("hls", hls);
        startActivity(intent);

    }
    private void textViewer(String url) {

        Intent intent = new Intent(this, TextViewer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }

    private void imageViewer(String url) {

        Intent intent = new Intent(this, ImageViewer.class);
        intent.putExtra("video_url", url);
        startActivity(intent);

    }

    private void webBrowser(String url, String pagetitle) {

        Intent intent = new Intent(this, WebBrowser.class);
        intent.putExtra("web_url", url);
        intent.putExtra("title", pagetitle);
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
        intent.putExtra("region", location);
        intent.putExtra("pdfendpoint", pdfendpoint);
        intent.putExtra("videocache", videocache);
        intent.putExtra("videotime", videotime);
        intent.putExtra("buffersize", buffersize);
        startActivity(intent);

    }

    private void share(String object, boolean mediafile) {

        Intent intent = new Intent(this, Share.class);
        //treelevel ++;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("object", object);
        intent.putExtra("region", location);
        intent.putExtra("mediafile", mediafile);
        intent.putExtra("videotime", videotime);
        startActivity(intent);

    }

    private void objectInfo(String object) {

        Intent intent = new Intent(this, ObjectInfo.class);
        //treelevel ++;
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("object", object);
        intent.putExtra("region", location);
        startActivity(intent);

    }

    private void delete(String object, boolean folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ObjectSelect.this);
        builder.setCancelable(true);
        builder.setTitle(object);
        if (folder) {
            builder.setMessage(getResources().getString(R.string.folder_del_confirm));
        } else {
            builder.setMessage(getResources().getString(R.string.file_del_confirm));
        }
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(ObjectSelect.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                        Thread deleteObject = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    //Your code goes here
                                    //List<Bucket> buckets = s3client.listBuckets();
                                    if (folder) {
                                        ListObjectsRequest orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(object).withMaxKeys(1000);
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

                                    } else {
                                        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, object);
                                        s3client.deleteObject(deleteObjectRequest);
                                    }

                                                                        //System.out.println(Name);

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            // Sending reference and data to Adapter
                                            if (folder) {
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.folder_del_success), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.file_del_success), Toast.LENGTH_SHORT).show();
                                            }
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
                                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                        simpleProgressBar.setVisibility(View.VISIBLE);
                        deleteObject.start();
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

    private void upload() {
        Intent intent = new Intent(this, Uploader.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("bucket", bucket);
        intent.putExtra("prefix", prefix);
        intent.putExtra("region", location);
        startActivity(intent);
    }

    private void download(String filename) {

        Intent intent = new Intent(this, Downloader.class);
        intent.putExtra("filename", filename);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("prefix", prefix);
        intent.putExtra("region", location);
        intent.putExtra("bucket", bucket);
        startActivity(intent);
    }
}