package asgardius.page.s3manager;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Downloader extends AppCompatActivity {
    String username, password, endpoint, bucket, filename, prefix, location;
    Uri fileuri, filepath;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    ListObjectsRequest orequest;
    Intent intent;
    Button fileDownload;
    Thread downloadFile, downloadProgress;
    S3Object object;
    DocumentFile document;
    boolean started = false;
    boolean cancel = false;
    boolean style, isfolder;
    long filesize = 0;
    long objectsize;
    long transfered = 0;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        // create Wifi and wake locks
        mWifiLock = ((WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Transistor:wifi_lock");
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Transistor:wake_lock");
        filename = getIntent().getStringExtra("filename");
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        style = getIntent().getBooleanExtra("style", false);
        isfolder = getIntent().getBooleanExtra("isfolder", false);
        prefix = getIntent().getStringExtra("prefix");
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        fileDownload = (Button)findViewById(R.id.filedownload);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        s3ClientOptions.setPathStyleAccess(style);
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        performFileSearch("Select download location");
        fileDownload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                if (started) {
                    cancel = true;
                    downloadFile.interrupt();
                    //simpleProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    downloadFile();
                }
            }

        });
    }

    private void downloadFile () {
        started = true;
        simpleProgressBar.setVisibility(View.VISIBLE);
        //fileDownload.setEnabled(false);
        fileDownload.setText(getResources().getString(R.string.cancel_download));
        //Acquiring WakeLock and WifiLock if not held
        if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
            //System.out.println("WifiLock acquired");
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            //System.out.println("WakeLock acquired");
        }
        downloadFile = new Thread(new Runnable() {

            @Override
            public void run() {
                simpleProgressBar.setVisibility(View.VISIBLE);
                try  {
                    //Your code goes here
                    //s3client.createBucket(bucket, location);
                    //System.out.println(fkey);
                    if (isfolder) {
                        document = DocumentFile.fromTreeUri(getApplicationContext(), fileuri);
                        if (prefix == null) {
                            orequest = new ListObjectsRequest().withBucketName(bucket).withMaxKeys(1000);
                        } else {
                            orequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix).withMaxKeys(1000);
                        }
                        ArrayList<String> objectlist = new ArrayList<String>();
                        ObjectListing result = s3client.listObjects(orequest);
                        List<S3ObjectSummary> objects = result.getObjectSummaries();
                        for (S3ObjectSummary os : objects) {
                            objectlist.add(os.getKey());
                            objectsize = os.getSize();
                            if (objectsize%1024 == 0) {
                                filesize = filesize+(objectsize/1024);
                            } else {
                                filesize = filesize+(objectsize/1024)+1;
                            }
                        }
                        while (result.isTruncated()) {
                            result = s3client.listNextBatchOfObjects (result);
                            objects = result.getObjectSummaries();
                            for (S3ObjectSummary os : objects) {
                                objectlist.add(os.getKey());
                                objectsize = os.getSize();
                                if (objectsize%1024 == 0) {
                                    filesize = filesize+(objectsize/1024);
                                } else {
                                    filesize = filesize+(objectsize/1024)+1;
                                }
                            }

                        }
                        for (String os : objectlist) {
                            object = s3client.getObject(bucket, os);
                            filepath = document.createFile(null, os.replace(prefix, "")).getUri();
                            writeContentToFile(filepath, object);
                        }
                    } else {
                        object = s3client.getObject(bucket, prefix+filename);
                        filesize = (object.getObjectMetadata().getContentLength())/1024;
                        writeContentToFile(fileuri, object);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //simpleProgressBar.setProgress(100);
                            //Releasing WifiLock and WakeLock if held
                            if (mWifiLock.isHeld()) {
                                mWifiLock.release();
                                //System.out.println("WifiLock released");
                            }
                            if (mWakeLock.isHeld()) {
                                mWakeLock.release();
                                //System.out.println("WakeLock released");
                            }
                            simpleProgressBar.setProgress(100);
                            if (isfolder) {
                                fileDownload.setText(getResources().getString(R.string.batch_download_success));
                            } else {
                                fileDownload.setText(getResources().getString(R.string.download_success));
                            }
                            fileDownload.setEnabled(false);
                            //simpleProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    //System.out.println("tree "+treelevel);
                    //System.out.println("prefix "+prefix);

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //Releasing WifiLock and WakeLock if held
                            if (mWifiLock.isHeld()) {
                                mWifiLock.release();
                                //System.out.println("WifiLock released");
                            }
                            if (mWakeLock.isHeld()) {
                                mWakeLock.release();
                                //System.out.println("WakeLock released");
                            }
                            if (cancel) {
                                fileDownload.setText(getResources().getString(R.string.download_canceled));
                            } else {
                                fileDownload.setText(getResources().getString(R.string.download_failed));
                            }
                            fileDownload.setEnabled(false);
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    //finish();
                }
            }
        });
        downloadProgress = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    while (fileDownload.isEnabled()) {
                        try {
                            if (filesize != 0) {
                                simpleProgressBar.setProgress((int)((transfered*100)/filesize));
                            }
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    //finish();
                }
            }
        });
        downloadFile.start();
        downloadProgress.start();
    }

    private void performFileSearch(String messageTitle) {
        //uri = Uri.parse("content://com.android.externalstorage.documents/document/home");
        intent = new Intent();
        if (isfolder) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        } else {
            intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
            //intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            //intent.putExtra("android.provider.extra.INITIAL_URI", uri);
            intent.putExtra(Intent.EXTRA_TITLE, filename);
            intent.setType("*/*");
        }
        ((Activity) this).startActivityForResult(intent, 50);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code OPEN_DIRECTORY_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 50) {
            if (resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
                if (resultData != null && resultData.getData() != null) {
                    fileuri = resultData.getData();
                    System.out.println(fileuri.toString());
                    downloadFile();
                    //System.out.println("File selected successfully");
                    //System.out.println("content://com.android.externalstorage.documents"+file.getPath());
                } else {
                    Toast.makeText(Downloader.this, getResources().getString(R.string.file_path_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                //System.out.println("User cancelled file browsing {}");
                finish();
            }
        }
    }

    private void writeContentToFile(Uri uri, S3Object object) throws IOException {
        try (
                final InputStream in = object.getObjectContent();
                final OutputStream out = getContentResolver().openOutputStream(uri);
        ) {
            byte[] buffer = new byte[1024];
            for (int len; (len = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
                transfered ++;
            }
        }
    }

    private String getDisplayName(Uri uri) {
        final String[] projection = { MediaStore.Images.Media.DISPLAY_NAME };
        try (
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        ){
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        }
        // If the display name is not found for any reason, use the Uri path as a fallback.
        Log.w(TAG, "Couldnt determine DISPLAY_NAME for Uri.  Falling back to Uri path: " + uri.getPath());
        return uri.getPath();
    }
}