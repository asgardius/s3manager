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
    String username, password, endpoint, bucket, object, location;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    Calendar mycal;
    EditText datepick, monthpick, yearpick, hourpick, minutepick;
    int date, month, year, hour, minute;
    Button share, external;

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
        monthpick = (EditText)findViewById(R.id.Month);
        yearpick = (EditText)findViewById(R.id.Year);
        hourpick = (EditText)findViewById(R.id.Hour);
        minutepick = (EditText)findViewById(R.id.Minute);
        share = (Button)findViewById(R.id.share);
        external = (Button)findViewById(R.id.open_in);
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
        share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                try {
                    if (datepick.getText().toString().equals("") || monthpick.getText().toString().equals("") ||
                            yearpick.getText().toString().equals("") || hourpick.getText().toString().equals("") ||
                            minutepick.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.null_expiration_date), Toast.LENGTH_SHORT).show();
                    } else {
                        date = Integer.parseInt(datepick.getText().toString());
                        month = Integer.parseInt(monthpick.getText().toString());
                        year = Integer.parseInt(yearpick.getText().toString());
                        hour = Integer.parseInt(hourpick.getText().toString());
                        minute = Integer.parseInt(minutepick.getText().toString());
                        mycal.set(Calendar.YEAR, year);
                        mycal.set(Calendar.MONTH, month-1);
                        mycal.set(Calendar.DATE, date);
                        mycal.set(Calendar.HOUR, hour);
                        mycal.set(Calendar.MINUTE, minute);
                        mycal.set(Calendar.SECOND, 0);
                        Date expiration = mycal.getTime();
                        //System.out.println(expiration);
                        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, object).withExpiration(expiration);
                        URL objectURL = s3client.generatePresignedUrl(request);
                        //System.out.println(URLify(objectURL.toString()));
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, URLify(objectURL.toString()));
                        startActivity(Intent.createChooser(shareIntent, "choose one"));
                    }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_expiration_date), Toast.LENGTH_SHORT).show();
                    }
                }

        });
        external.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                try {
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, object);
                    URL objectURL = s3client.generatePresignedUrl(request);
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