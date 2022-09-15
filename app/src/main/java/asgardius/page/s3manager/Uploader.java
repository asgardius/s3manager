package asgardius.page.s3manager;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
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
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Uploader extends AppCompatActivity {
    String  username, password, endpoint, bucket, prefix, location, fkey;
    boolean isfolder;
    int progress;
    Uri fileuri, folder, uri;
    EditText fprefix;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    long filesize;
    File ufile;
    StorageManager sm;
    Intent intent;
    private static final long MAX_SINGLE_PART_UPLOAD_BYTES = 5 * 1024 * 1024;

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
                if (fileuri == null && folder == null) {
                    Toast.makeText(Uploader.this, getResources().getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(CreateBucket.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
                    //System.out.println(file.getPath());
                    simpleProgressBar.setVisibility(View.VISIBLE);
                    fileUpload.setEnabled(false);
                    fileUpload.setText(getResources().getString(R.string.wait));
                    Thread uploadFile = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            if (fprefix.getText().toString().endsWith("/") || fprefix.getText().toString().equals("")) {
                                fkey = fprefix.getText().toString()+getDisplayName(fileuri);
                            } else {
                                fkey = fprefix.getText().toString()+"/"+getDisplayName(fileuri);
                            }
                            //System.out.println(fkey);
                            progress = 0;
                            filesize = 0;
                            try  {
                                //Your code goes here
                                //s3client.createBucket(bucket, location);
                                //System.out.println(fkey);
                                ufile = readContentToFile(fileuri);
                                filesize = ufile.length();
                                //PutObjectRequest request = new PutObjectRequest(bucket, fkey, ufile);
                                //upload = s3client.putObject(request);
                                putS3Object(bucket, fkey, ufile);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        //simpleProgressBar.setProgress(100);
                                        simpleProgressBar.setVisibility(View.INVISIBLE);
                                        fileUpload.setText(getResources().getString(R.string.success));
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
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
                                        fileUpload.setEnabled(true);
                                        fileUpload.setText(getResources().getString(R.string.retry));
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                //finish();
                            }
                        }
                    });
                    //simpleProgressBar.setVisibility(View.VISIBLE);
                    uploadFile.start();
                    /*while (progress <= 99) {
                        System.out.println("Upload in progress");
                        if (upload != null) {
                            System.out.println(upload.getMetadata().getContentLength());
                            if(upload.getMetadata().getContentLength() != 0) {
                                progress = (int) (((int)filesize / (int)upload.getMetadata().getContentLength())*100);
                                simpleProgressBar.setProgress(progress);
                            }
                        }
                        System.out.println(filesize);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/
                }
            }

        });
    }

    private void performFileSearch(String messageTitle) {
        //uri = Uri.parse("content://com.android.externalstorage.documents/document/home");
        intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        //intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        intent.setType("*/*");
        ((Activity) this).startActivityForResult(intent, 100);
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
                    fileuri = resultData.getData();
                    System.out.println(fileuri.toString());
                    //System.out.println("File selected successfully");
                    //System.out.println("content://com.android.externalstorage.documents"+file.getPath());
                } else {
                    Toast.makeText(Uploader.this, getResources().getString(R.string.file_path_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                //System.out.println("User cancelled file browsing {}");
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

    public void putS3Object(String bucket, String objectKey, File file) {
        if (file.length() <= MAX_SINGLE_PART_UPLOAD_BYTES) {
            putS3ObjectSinglePart(bucket, objectKey, file);
        } else {
            putS3ObjectMultiPart(bucket, objectKey, file);
        }
    }

    private void putS3ObjectSinglePart(String bucket, String objectKey, File file) {
        PutObjectRequest request = new PutObjectRequest(bucket, objectKey, file);
        PutObjectResult result = s3client.putObject(request);
        long bytesPushed = result.getMetadata().getContentLength();
        //LOGGER.info("Pushed {} bytes to s3://{}/{}", bytesPushed, bucket, objectKey);
    }

    private void putS3ObjectMultiPart(String bucket, String objectKey, File file) {
        long contentLength = file.length();
        long partSize = MAX_SINGLE_PART_UPLOAD_BYTES;
        List<PartETag> partETags = new ArrayList<>();

        // Initiate the multipart upload.
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, objectKey);
        InitiateMultipartUploadResult initResponse = s3client.initiateMultipartUpload(initRequest);

        // Upload the file parts.
        long fileOffset = 0;
        for (int partNumber = 1; fileOffset < contentLength; ++partNumber) {
            // Because the last part could be less than 5 MB, adjust the part size as needed.
            partSize = Math.min(partSize, (contentLength - fileOffset));

            // Create the request to upload a part.
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(bucket)
                    .withKey(objectKey)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(partNumber)
                    .withFileOffset(fileOffset)
                    .withFile(file)
                    .withPartSize(partSize);

            // Upload the part and add the response's ETag to our list.
            UploadPartResult uploadResult = s3client.uploadPart(uploadRequest);
            //LOGGER.info("Uploading part {} of Object s3://{}/{}", partNumber, bucket, objectKey);
            partETags.add(uploadResult.getPartETag());

            fileOffset += partSize;
        }

        // Complete the multipart upload.
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, objectKey, initResponse.getUploadId(), partETags);
        s3client.completeMultipartUpload(compRequest);
    }

}