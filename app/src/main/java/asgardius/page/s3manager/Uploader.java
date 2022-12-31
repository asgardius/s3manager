package asgardius.page.s3manager;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.annotation.SuppressLint;
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

public class Uploader extends AppCompatActivity {
    String  username, password, endpoint, bucket, prefix, location;
    Uri fileuri, folder;
    EditText fprefix;
    TextView fprefixlabel;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    ProgressBar simpleProgressBar;
    long filesize;
    File ufile;
    DocumentFile document;
    Intent intent;
    Button fileUpload;
    Thread uploadFile, uploadProgress, calculateProgress;
    boolean style, isfolder;
    boolean started = false;
    boolean cancel = false;
    long transfered = 0;
    private static final long MAX_SINGLE_PART_UPLOAD_BYTES = 5 * 1024 * 1024;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploader);
        // create Wifi and wake locks
        mWifiLock = ((WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Transistor:wifi_lock");
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Transistor:wake_lock");
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        style = getIntent().getBooleanExtra("style", false);
        isfolder = getIntent().getBooleanExtra("isfolder", false);
        prefix = getIntent().getStringExtra("prefix");
        fprefix = (EditText)findViewById(R.id.fprefix);
        fprefixlabel = (TextView) findViewById(R.id.fprefixlabel);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        s3ClientOptions.setPathStyleAccess(style);
        myCredentials = new BasicAWSCredentials(username, password);
        s3client = new AmazonS3Client(myCredentials, region);
        s3client.setEndpoint(endpoint);
        s3client.setS3ClientOptions(s3ClientOptions);
        fileUpload = (Button)findViewById(R.id.fileupload);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        //Toast.makeText(Uploader.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
        performFileSearch("Select file to upload");
        //fprefix.setText(prefix);
        fileUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                if (started) {
                    started = false;
                    cancel = true;
                    uploadFile.interrupt();
                    //simpleProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    started = true;
                    transfered = 0;
                    //simpleProgressBar.setProgress(0);
                    simpleProgressBar.setVisibility(View.VISIBLE);
                    if (fileuri == null && folder == null) {
                        Toast.makeText(Uploader.this, getResources().getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
                    } else {
                        //Acquiring WakeLock and WifiLock if not held
                        if (!mWifiLock.isHeld()) {
                            mWifiLock.acquire();
                            //System.out.println("WifiLock acquired");
                        }
                        if (!mWakeLock.isHeld()) {
                            mWakeLock.acquire();
                            //System.out.println("WakeLock acquired");
                        }
                        //eUpload.setEnabled(false);
                        fileUpload.setText(getResources().getString(R.string.cancel_upload));
                        fprefix.setEnabled(false);
                        uploadFile = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                //System.out.println(fkey);
                                //filesize = 0;
                                try  {
                                    //Your code goes here
                                    //s3client.createBucket(bucket, location);
                                    //System.out.println(fkey);
                                    if (isfolder) {
                                        if(prefix.endsWith("/")) {
                                            prefix = fprefix.getText().toString();
                                        } else {
                                            prefix = fprefix.getText().toString().concat("/");
                                        }
                                        document = DocumentFile.fromTreeUri(getApplicationContext(), fileuri);
                                        DocumentFile[] filelist = document.listFiles();
                                        ArrayList<String> filepath = new ArrayList<String>();
                                        int treelevel = 0;
                                        ArrayList<Integer> fileindex = new ArrayList<Integer>();
                                        fileindex.add(0);
                                        for (int i = 0; i < filelist.length; i++) {
                                            filepath.add(filelist[i].getName());
                                            if(filelist[i].isDirectory()) {
                                                treelevel++;
                                                fileindex.add(0);
                                                document = filelist[i];
                                                filelist = document.listFiles();
                                                while (treelevel >= 1 && fileindex.get(treelevel) < filelist.length+1) {
                                                    if(fileindex.get(treelevel) == filelist.length) {
                                                        fileindex.remove(treelevel);
                                                        document = document.getParentFile();
                                                        filelist = document.listFiles();
                                                        treelevel--;
                                                        filepath.remove(treelevel);
                                                        fileindex.set(treelevel, fileindex.get(treelevel)+1);
                                                    } else {
                                                        filepath.add(filelist[fileindex.get(treelevel)].getName());
                                                        if (filelist[fileindex.get(treelevel)].isDirectory()) {
                                                            document = filelist[fileindex.get(treelevel)];
                                                            filelist = document.listFiles();
                                                            treelevel++;
                                                            fileindex.add(0);
                                                        } else {
                                                            ufile = readContentToFile(filelist[fileindex.get(treelevel)].getUri());
                                                            putS3Object(bucket, prefix+String.join("/", filepath), ufile);
                                                            filepath.remove(treelevel);
                                                            fileindex.set(treelevel, fileindex.get(treelevel)+1);
                                                        }
                                                    }
                                                }
                                            } else {
                                                ufile = readContentToFile(filelist[i].getUri());
                                                putS3Object(bucket, prefix+String.join("/", filepath), ufile);
                                            }
                                            filepath.clear();
                                            fileindex.clear();
                                            fileindex.add(0);
                                        }
                                    } else {
                                        ufile = readContentToFile(fileuri);
                                        if(ufile.length()%MAX_SINGLE_PART_UPLOAD_BYTES == 0) {
                                            filesize = ufile.length()/MAX_SINGLE_PART_UPLOAD_BYTES;
                                        } else {
                                            filesize = (ufile.length()/MAX_SINGLE_PART_UPLOAD_BYTES)+1;
                                        }
                                        //PutObjectRequest request = new PutObjectRequest(bucket, fkey, ufile);
                                        //upload = s3client.putObject(request);
                                        putS3Object(bucket, fprefix.getText().toString(), ufile);
                                    }
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
                                            simpleProgressBar.setProgress(100);
                                            //simpleProgressBar.setVisibility(View.INVISIBLE);
                                            if (isfolder) {
                                                fileUpload.setText(getResources().getString(R.string.batch_upload_success));
                                            } else {
                                                fileUpload.setText(getResources().getString(R.string.upload_success));
                                            }
                                            started = false;
                                            fileUpload.setEnabled(false);
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
                                            //Releasing WifiLock and WakeLock if held
                                            if (mWifiLock.isHeld()) {
                                                mWifiLock.release();
                                                //System.out.println("WifiLock released");
                                            }
                                            if (mWakeLock.isHeld()) {
                                                mWakeLock.release();
                                                //System.out.println("WakeLock released");
                                            }
                                            started = false;
                                            //simpleProgressBar.setVisibility(View.INVISIBLE);
                                            //fileUpload.setEnabled(true);
                                            if (cancel) {
                                                fileUpload.setText(getResources().getString(R.string.upload_canceled));
                                            } else {
                                                fileUpload.setText(getResources().getString(R.string.upload_failed));
                                            }
                                            fileUpload.setEnabled(false);
                                            //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    //finish();
                                }
                            }
                        });
                        uploadProgress = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    //Your code goes here
                                    while (started) {
                                        try {
                                            if (filesize != 0) {
                                                //simpleProgressBar.setProgress((int)((transfered*100)/filesize));
                                                simpleProgressBar.setProgress((int)((transfered*100)/filesize));
                                            }
                                            Thread.sleep(500);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //simpleProgressBar.setProgress(100);
                                        }
                                    });
                                    //System.out.println("tree "+treelevel);
                                    //System.out.println("prefix "+prefix);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                                    //finish();
                                }
                            }
                        });
                        //simpleProgressBar.setVisibility(View.VISIBLE);
                        uploadFile.start();
                        uploadProgress.start();
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
            }

        });
    }

    private void performFileSearch(String messageTitle) {
        //uri = Uri.parse("content://com.android.externalstorage.documents/document/home");
        intent = new Intent();
        if (isfolder) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            //intent.putExtra("android.provider.extra.INITIAL_URI", uri);
            intent.setType("*/*");
        }
        ((Activity) this).startActivityForResult(intent, 100);
    }

    @SuppressLint("SetTextI18n")
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
                    if (isfolder) {
                        fprefix.setText(prefix);
                        fprefix.setHint(getResources().getString(R.string.upload_prefix));
                        fprefixlabel.setText(getResources().getString(R.string.upload_prefix));
                        fileUpload.setText(getResources().getString(R.string.upload_calculate));
                        fileUpload.setEnabled(false);
                        calculateSize();
                    } else {
                        fprefix.setText(prefix+getDisplayName(fileuri));
                    }
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
        transfered++;
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
            transfered++;
        }

        // Complete the multipart upload.
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, objectKey, initResponse.getUploadId(), partETags);
        s3client.completeMultipartUpload(compRequest);
    }

    private void calculateSize() {
        calculateProgress = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    document = DocumentFile.fromTreeUri(getApplicationContext(), fileuri);
                    DocumentFile[] filelist = document.listFiles();
                    ArrayList<String> filepath = new ArrayList<String>();
                    int treelevel = 0;
                    ArrayList<Integer> fileindex = new ArrayList<Integer>();
                    fileindex.add(0);
                    for (int i = 0; i < filelist.length; i++) {
                        filepath.add(filelist[i].getName());
                        if(filelist[i].isDirectory()) {
                            treelevel++;
                            fileindex.add(0);
                            document = filelist[i];
                            filelist = document.listFiles();
                            while (treelevel >= 1 && fileindex.get(treelevel) < filelist.length+1) {
                                if(fileindex.get(treelevel) == filelist.length) {
                                    fileindex.remove(treelevel);
                                    document = document.getParentFile();
                                    filelist = document.listFiles();
                                    treelevel--;
                                    filepath.remove(treelevel);
                                    fileindex.set(treelevel, fileindex.get(treelevel)+1);
                                } else {
                                    filepath.add(filelist[fileindex.get(treelevel)].getName());
                                    if (filelist[fileindex.get(treelevel)].isDirectory()) {
                                        document = filelist[fileindex.get(treelevel)];
                                        filelist = document.listFiles();
                                        treelevel++;
                                        fileindex.add(0);
                                    } else {
                                        filepath.remove(treelevel);
                                        if(filelist[fileindex.get(treelevel)].length()%MAX_SINGLE_PART_UPLOAD_BYTES == 0) {
                                            filesize = filesize+(filelist[fileindex.get(treelevel)].length()/MAX_SINGLE_PART_UPLOAD_BYTES);
                                        } else {
                                            filesize = filesize+((filelist[fileindex.get(treelevel)].length()/MAX_SINGLE_PART_UPLOAD_BYTES)+1);
                                        }
                                        fileindex.set(treelevel, fileindex.get(treelevel)+1);
                                    }
                                }
                            }
                            //document = document.getParentFile();
                            //filelist = document.listFiles();
                            //treelevel--;
                        } else {
                            if(filelist[i].length()%MAX_SINGLE_PART_UPLOAD_BYTES == 0) {
                                filesize = filesize+(filelist[i].length()/MAX_SINGLE_PART_UPLOAD_BYTES);
                            } else {
                                filesize = filesize+((filelist[i].length()/MAX_SINGLE_PART_UPLOAD_BYTES)+1);
                            }
                        }
                        filepath.clear();
                        fileindex.clear();
                        fileindex.add(0);
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            fileUpload.setText(getResources().getString(R.string.batch_upload_button));
                            fileUpload.setEnabled(true);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    //finish();
                }
            }
        });
        calculateProgress.start();
    }

}