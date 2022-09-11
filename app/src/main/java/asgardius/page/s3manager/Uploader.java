package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
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

import java.net.URL;

public class Uploader extends AppCompatActivity {
    String  username, password, endpoint, bucket, prefix, location;
    boolean isfolder;
    Uri file, folder;
    EditText fprefix;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploader);
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        prefix = getIntent().getStringExtra("prefix");
        isfolder = getIntent().getBooleanExtra("isfolder", false);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        fprefix = (EditText)findViewById(R.id.fprefix);
        Button fileUpload = (Button)findViewById(R.id.fileupload);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        Toast.makeText(Uploader.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
        /*if (isfolder) {
            folder = uploadFolder();
        } else {
            file = uploadFile();
        }*/
        fileUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                if (file == null && folder == null) {
                    Toast.makeText(Uploader.this, getResources().getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(CreateBucket.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                    System.out.println(file.getPath());
                    Thread uploadFile = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                //Your code goes here
                                //s3client.createBucket(bucket, location);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Sending reference and data to Adapter
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                                        simpleProgressBar.setVisibility(View.INVISIBLE);
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
                    //simpleProgressBar.setVisibility(View.VISIBLE);
                    //uploadFile.start();
                }
            }

        });
    }
/*
    private Uri uploadFolder() {
        StorageManager sm = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);

        Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        String startDir = "Documents";

        Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");

        String scheme = uri.toString();

        scheme = scheme.replace("/root/", "/document/");

        scheme += "%3A" + startDir;

        uri = Uri.parse(scheme);

        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        ((Activity) this).startActivityForResult(intent,100);
        return uri;
    }

    private Uri uploadFile() {
        StorageManager sm = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);

        Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        String startDir = "Documents";

        Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");

        String scheme = uri.toString();

        scheme = scheme.replace("/root/", "/document/");

        scheme += "%3A" + startDir;

        uri = Uri.parse(scheme);

        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        ((Activity) this).startActivityForResult(intent,100);
        return uri;
    }*/
}