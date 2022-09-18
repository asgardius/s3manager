package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class Share extends AppCompatActivity {
    String username, password, endpoint, bucket, object, location;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    Calendar mycal;

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
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        object = getIntent().getStringExtra("object");
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
        mycal.set(Calendar.YEAR, 2022);
        mycal.set(Calendar.MONTH, 8);
        mycal.set(Calendar.DATE, 18);
        mycal.set(Calendar.HOUR, 14);
        mycal.set(Calendar.MINUTE, 15);
        mycal.set(Calendar.SECOND, 28);
        Date expiration = mycal.getTime();
        System.out.println(expiration);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, object).withExpiration(expiration);
        URL objectURL = s3client.generatePresignedUrl(request);
        System.out.println(URLify(objectURL.toString()));
    }
}