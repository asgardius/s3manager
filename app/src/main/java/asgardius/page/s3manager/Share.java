package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Share extends AppCompatActivity {
    String username, password, endpoint, bucket, object, location, title, objectlist;
    boolean mediafile, style;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ListObjectsRequest orequest;
    Calendar mycal;
    EditText datepick, hourpick, minutepick;
    int date, hour, minute;
    Button share, copylinks, savelinks;
    GeneratePresignedUrlRequest request;
    Date expiration;
    URL objectURL;
    Uri fileuri;
    Intent intent;
    int videotime, playlisttime;

    public static String URLify(String str) {
        str = str.trim();
        int length = str.length();
        int trueL = length;
        if(str.contains(" ")) {
            for(int i = 0; i < length; i++) {
                if(str.charAt(i) == ' ') {
                    trueL = trueL + 2;
                }
            }
            char[] oldArr = str.toCharArray();
            char[] newArr = new char[trueL];
            int x = 0;
            for(int i = 0; i < length; i++) {
                if(oldArr[i] == ' ') {
                    newArr[x] = '%';
                    newArr[x+1] = '2';
                    newArr[x+2] = '0';
                    x += 3;
                } else {
                    newArr[x] = oldArr[i];
                    x++;
                }
            }
            str = new String(newArr, 0, trueL);
        }
        return str;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        mycal = Calendar.getInstance();
        datepick = (EditText)findViewById(R.id.Date);
        hourpick = (EditText)findViewById(R.id.Hour);
        minutepick = (EditText)findViewById(R.id.Minute);
        share = (Button)findViewById(R.id.share);
        copylinks = (Button)findViewById(R.id.copy_links);
        savelinks = (Button)findViewById(R.id.save_links);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        style = getIntent().getBooleanExtra("style", false);
        object = getIntent().getStringExtra("object");
        mediafile = getIntent().getBooleanExtra("mediafile", false);
        videotime = getIntent().getIntExtra("videotime", 1);
        playlisttime = getIntent().getIntExtra("playlisttime", 1);
        title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        myCredentials = new BasicAWSCredentials(username, password);
        try {
            s3client = new AmazonS3Client(myCredentials, region);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show();
        }
        s3client.setEndpoint(endpoint);
        s3ClientOptions.setPathStyleAccess(style);
        s3client.setS3ClientOptions(s3ClientOptions);
        if(object == null || object.endsWith("/")) {
            copylinks.setVisibility(View.VISIBLE);
            savelinks.setVisibility(View.VISIBLE);
        } else {
            share.setVisibility(View.VISIBLE);
        }
        share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                try {
                    if (datepick.getText().toString().equals("")) {
                        date = 0;
                    } else {
                        date = Integer.parseInt(datepick.getText().toString());
                    }
                    if (hourpick.getText().toString().equals("")) {
                        hour = 0;
                    } else {
                        hour = Integer.parseInt(hourpick.getText().toString());
                    }
                    if (minutepick.getText().toString().equals("")) {
                        minute = 0;
                    } else {
                        minute = Integer.parseInt(minutepick.getText().toString());
                    }
                    expiration = new Date();
                    //System.out.println("today is " + mycal.getTime());
                    mycal.setTime(expiration);
                    if (date == 0 && hour == 0 && minute == 0) {
                        if (mediafile) {
                            mycal.add(Calendar.HOUR, videotime);
                        } else {
                            mycal.add(Calendar.MINUTE, 15);
                        }
                    } else {
                        mycal.add(Calendar.DATE, date);
                        mycal.add(Calendar.HOUR, hour);
                        mycal.add(Calendar.MINUTE, minute);
                    }
                    //System.out.println("Expiration date: " + mycal.getTime());
                    expiration = mycal.getTime();
                    //System.out.println(expiration);
                    request = new GeneratePresignedUrlRequest(bucket, object).withExpiration(expiration);
                    objectURL = s3client.generatePresignedUrl(request);
                    //System.out.println(URLify(objectURL.toString()));
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, URLify(objectURL.toString()));
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_expiration_date), Toast.LENGTH_SHORT).show();
                    }
                }

        });
        copylinks.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                Thread copyLinks = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            //load media file
                            getLinks();

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // Sending reference and data to Adapter
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip;
                                    clip = ClipData.newPlainText("name", objectlist);
                                    clipboard.setPrimaryClip(clip);
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.copy_ok), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            //System.out.println("tree "+treelevel);
                            //System.out.println("prefix "+prefix);

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_expiration_date), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                copyLinks.start();
            }

        });
        savelinks.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                performFileSearch("Select download location");
            }

        });
    }

    private void performFileSearch(String messageTitle) {
        //uri = Uri.parse("content://com.android.externalstorage.documents/document/home");
        intent = new Intent();
        intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        //intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        intent.putExtra(Intent.EXTRA_TITLE, "links.txt");
        intent.setType("*/*");
        ((Activity) this).startActivityForResult(intent, 70);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code OPEN_DIRECTORY_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 70) {
            if (resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
                if (resultData != null && resultData.getData() != null) {
                    fileuri = resultData.getData();
                    System.out.println(fileuri.toString());
                    savelinks();
                    //System.out.println("File selected successfully");
                    //System.out.println("content://com.android.externalstorage.documents"+file.getPath());
                } else {
                    Toast.makeText(Share.this, getResources().getString(R.string.file_path_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                //System.out.println("User cancelled file browsing {}");
                finish();
            }
        }
    }

    private void getLinks() {
        if (datepick.getText().toString().equals("")) {
            date = 0;
        } else {
            date = Integer.parseInt(datepick.getText().toString());
        }
        if (hourpick.getText().toString().equals("")) {
            hour = 0;
        } else {
            hour = Integer.parseInt(hourpick.getText().toString());
        }
        if (minutepick.getText().toString().equals("")) {
            minute = 0;
        } else {
            minute = Integer.parseInt(minutepick.getText().toString());
        }
        expiration = new Date();
        //System.out.println("today is " + mycal.getTime());
        mycal.setTime(expiration);
        if (date == 0 && hour == 0 && minute == 0) {
            mycal.add(Calendar.HOUR, playlisttime);
        } else {
            mycal.add(Calendar.DATE, date);
            mycal.add(Calendar.HOUR, hour);
            mycal.add(Calendar.MINUTE, minute);
        }
        //System.out.println("Expiration date: " + mycal.getTime());
        expiration = mycal.getTime();
        //System.out.println(expiration);
        if (object == null) {
            orequest = new ListObjectsRequest().withBucketName(bucket).withMaxKeys(1000);
        } else {
            orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(object).withMaxKeys(1000);
        }
        ObjectListing result = s3client.listObjects(orequest);
        objectlist = "";
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            request = new GeneratePresignedUrlRequest(bucket, os.getKey()).withExpiration(expiration);
            objectlist = objectlist+s3client.generatePresignedUrl(request).toString()+"\n";
        }
        while (result.isTruncated()) {
            result = s3client.listNextBatchOfObjects (result);
            objects = result.getObjectSummaries();
            for (S3ObjectSummary os : objects) {
                request = new GeneratePresignedUrlRequest(bucket, os.getKey()).withExpiration(expiration);
                objectlist = objectlist+s3client.generatePresignedUrl(request).toString()+"\n";
            }

        }
    }

    private void savelinks() {
        Thread saveLinks = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    getLinks();
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContentResolver().openOutputStream(fileuri));
                    outputStreamWriter.write(objectlist);
                    outputStreamWriter.close();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.save_ok), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //System.out.println("tree "+treelevel);
                    //System.out.println("prefix "+prefix);

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_expiration_date), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        saveLinks.start();
    }
}