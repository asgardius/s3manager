package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class Share extends AppCompatActivity {
    String username, password, endpoint, bucket, object, location, title;
    boolean mediafile, style;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    Calendar mycal;
    EditText datepick, hourpick, minutepick;
    int date, hour, minute;
    Button share;
    GeneratePresignedUrlRequest request;
    Date expiration;
    URL objectURL;
    int videotime;

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
        endpoint = getIntent().getStringExtra("endpoint");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        location = getIntent().getStringExtra("region");
        style = getIntent().getBooleanExtra("style", false);
        object = getIntent().getStringExtra("object");
        mediafile = getIntent().getBooleanExtra("mediafile", false);
        videotime = getIntent().getIntExtra("videotime", 1);
        title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        myCredentials = new BasicAWSCredentials(username, password);
        try {
            s3client = new AmazonS3Client(myCredentials, region);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
        }
        s3client.setEndpoint(endpoint);
        s3ClientOptions.setPathStyleAccess(style);
        s3client.setS3ClientOptions(s3ClientOptions);
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
                    System.out.println("today is " + mycal.getTime());
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
                    System.out.println("Expiration date: " + mycal.getTime());
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
    }
}