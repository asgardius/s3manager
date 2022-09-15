package asgardius.page.s3manager;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.amazonaws.services.s3.model.S3Object;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Downloader extends AppCompatActivity {
    String username, password, endpoint, bucket, filekey, filename, prefix, location, fkey;
    boolean isfolder;
    int progress;
    Uri fileuri, folder, uri;
    EditText fprefix;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    File dfile;
    Intent intent;
    Button fileDownload;
    Thread downloadFile;
    S3Object object;
    InputStream in;
    OutputStream out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        filename = getIntent().getStringExtra("filename");
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        prefix = getIntent().getStringExtra("prefix");
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        fileDownload = (Button)findViewById(R.id.filedownload);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        performFileSearch("Select download location");
        fileDownload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                simpleProgressBar.setVisibility(View.VISIBLE);
                fileDownload.setEnabled(false);
                fileDownload.setText(getResources().getString(R.string.download_in_progress));
                downloadFile = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        simpleProgressBar.setVisibility(View.VISIBLE);
                        try  {
                            //Your code goes here
                            //s3client.createBucket(bucket, location);
                            //System.out.println(fkey);
                            object = s3client.getObject(bucket, prefix+filename);
                            writeContentToFile(fileuri);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //simpleProgressBar.setProgress(100);
                                    simpleProgressBar.setVisibility(View.INVISIBLE);
                                    fileDownload.setText(getResources().getString(R.string.download_success));
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.download_success), Toast.LENGTH_SHORT).show();
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
                                    simpleProgressBar.setVisibility(View.INVISIBLE);
                                    fileDownload.setText(getResources().getString(R.string.download_failed));
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                }
                            });
                            //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                            //finish();
                        }
                    }
                });
                downloadFile.start();
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
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        intent.setType("*/*");
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

    private void writeContentToFile(Uri uri) throws IOException {
        try (
                final InputStream in = object.getObjectContent();
                final OutputStream out = getContentResolver().openOutputStream(uri);
        ) {
            byte[] buffer = new byte[1024];
            for (int len; (len = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
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