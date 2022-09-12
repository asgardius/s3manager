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
    String[] filename;

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
        fprefix = (EditText)findViewById(R.id.fprefix);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        if (!endpoint.contains(getResources().getString(R.string.aws_endpoint))) {
            s3ClientOptions.setPathStyleAccess(true);
        }
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        Button fileUpload = (Button)findViewById(R.id.fileupload);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        //Toast.makeText(Uploader.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
        performFileSearch("Select file to upload");
        fprefix.setText(prefix);
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
                    //System.out.println(file.getPath());
                    System.out.println(fprefix.getText().toString()+filename[filename.length-1]);
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

    private void performFileSearch(String messageTitle) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setType("*/*");
        /*String[] mimeTypes = new String[]{"application/x-binary,application/octet-stream"};
        if (mimeTypes.length > 0) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }*/

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, messageTitle), 100);
        } else {
            Toast.makeText(Uploader.this, getResources().getString(R.string.file_choose_fail), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code OPEN_DIRECTORY_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
                if (resultData != null && resultData.getData() != null) {
                    file = resultData.getData();
                    filename = file.getPath().split("/");
                    System.out.println("File selected successfully");
                        System.out.println("Prefix "+prefix);
                } else {
                    Toast.makeText(Uploader.this, getResources().getString(R.string.file_path_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                System.out.println("User cancelled file browsing {}");
                finish();
            }
        }
    }
}