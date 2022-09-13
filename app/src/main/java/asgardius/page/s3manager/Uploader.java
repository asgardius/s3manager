package asgardius.page.s3manager;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
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
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Uploader extends AppCompatActivity {
    String  username, password, endpoint, bucket, prefix, location, fkey;
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
                    if (fprefix.getText().toString().endsWith("/") || fprefix.getText().toString().equals("")) {
                        fkey = fprefix.getText().toString()+filename[filename.length-1];
                    } else {
                        fkey = fprefix.getText().toString()+"/"+filename[filename.length-1];
                    }
                    System.out.println(fkey);
                    Thread uploadFile = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                //Your code goes here
                                //s3client.createBucket(bucket, location);
                                System.out.println(fkey);
                                ContentResolver cr = getContentResolver();
                                InputStream is = cr.openInputStream(file);
                                File ufile = readContentToFile(file);
                                PutObjectResult upload = s3client.putObject(bucket, fkey, ufile);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Sending reference and data to Adapter
                                        //Toast.makeText(getApplicationContext(),getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                    //simpleProgressBar.setVisibility(View.VISIBLE);
                    uploadFile.start();
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
            Toast.makeText(Uploader.this, getResources().getString(R.string.feature_not_supported), Toast.LENGTH_SHORT).show();
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
                        System.out.println("content://com.android.externalstorage.documents"+file.getPath());
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

    private File readContentToFile(Uri uri) throws IOException {
        final File file = new File(getCacheDir(), getDisplayName(uri));
        try (
                final InputStream in = getContentResolver().openInputStream(uri);
                final OutputStream out = new FileOutputStream(file, false);
        ) {
            byte[] buffer = new byte[1024];
            for (int len; (len = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
            }
            return file;
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