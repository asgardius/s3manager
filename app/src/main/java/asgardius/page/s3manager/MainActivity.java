package asgardius.page.s3manager;

import static com.amazonaws.regions.RegionUtils.getRegion;
import static com.amazonaws.regions.Regions.US_EAST_1;
import static com.amazonaws.services.s3.S3ClientOptions.DEFAULT_PATH_STYLE_ACCESS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static boolean DEFAULT_PATH_STYLE_ACCESS = true;
    String username, password, endpoint;
    RecyclerView recyclerView;
    ArrayList Name;
    ArrayList Img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = getString(R.string.access_key);
        password = getResources().getString(R.string.secret_key);
        endpoint = getResources().getString(R.string.endpoint_url);

        recyclerView = findViewById(R.id.alist);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        MyDbHelper dbHelper = new MyDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db != null) {
            // Database Queries
            System.out.println("Database OK");
            Name = new ArrayList<String>();
            Img = new ArrayList<String>();
            String query = "SELECT id FROM account";
            Cursor cursor = db.rawQuery(query,null);
            while (cursor.moveToNext()){
                Name.add(cursor.getString(0));
                Img.add(R.drawable.account);
            }
        } else {
            System.out.println("Database Missing");
        }

        Thread listaccount = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    // Print bucket names
                    //System.out.println("Buckets:");

                    System.out.println(Name);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Sending reference and data to Adapter
                            Adapter adapter = new Adapter(MainActivity.this, Img, Name);

                            // Setting Adapter to RecyclerView
                            recyclerView.setAdapter(adapter);
                        }
                    });

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

        listaccount.start();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                System.out.println("Click on "+Name.get(position).toString());
                explorer();
            }

            @Override
            public void onLongClick(View view, int position) {
                System.out.println("Long click on "+Name.get(position).toString());
            }
        }));

        //This is to launch video playback test
        Button addaccount = (Button)findViewById(R.id.addaccount);
        addaccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                addaccount();
            }
        });

        //This is to launch file explorer test
        Button explorertest = (Button)findViewById(R.id.ltest);
        explorertest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                //explorer();
                testaccount();
                //videoplayer("https://video.asgardius.company/download/videos/41780585-a935-4d53-84c8-45ce97141231-480.mp4");
            }
        });
    }

    private void explorer() {

        Intent intent = new Intent(this, BucketSelect.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);

    }

    private void addaccount() {

        Intent intent = new Intent(this, AccountAdd.class);
        startActivity(intent);

    }

    private void testaccount() {
        MyDbHelper dbHelper = new MyDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db != null) {
            // Database Queries
            try {
                db.execSQL("INSERT INTO account VALUES (\"test account\", \""+endpoint+"\", \""+username+"\", \""+password+"\")");
                System.out.println("Insert OK");
            } catch (Exception e) {
                System.out.println("Insert error");
            }
        }
    }

}