package asgardius.page.s3manager;

import static com.amazonaws.regions.Regions.US_EAST_1;

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
import com.amazonaws.services.s3.model.Bucket;

import java.util.ArrayList;
import java.util.List;

public class AccountAdd extends AppCompatActivity {
    EditText aapick, aupick, appick, aepick;
    String alias, username, password, endpoint, id;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_add);
        aapick = (EditText)findViewById(R.id.alias);
        aepick = (EditText)findViewById(R.id.endpoint);
        aupick = (EditText)findViewById(R.id.username);
        appick = (EditText)findViewById(R.id.password);
        Button register = (Button)findViewById(R.id.addaccount);
        Button accounttest = (Button)findViewById(R.id.testaccount);
        edit = getIntent().getBooleanExtra("edit", false);
        Region region = Region.getRegion(US_EAST_1);
        S3ClientOptions s3ClientOptions = S3ClientOptions.builder().build();
        s3ClientOptions.setPathStyleAccess(true);
        if (edit) {
            register.setText(getResources().getString(R.string.accountsave_button));
            id = getIntent().getStringExtra("alias");
            endpoint = getIntent().getStringExtra("endpoint");
            username = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
            aapick.setText(id);
            //aapick.setEnabled(false);
            aepick.setText(endpoint);
            aupick.setText(username);
            appick.setText(password);
        }



        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                alias = aapick.getText().toString();
                endpoint = aepick.getText().toString();
                username = aupick.getText().toString();
                password = appick.getText().toString();
                MyDbHelper dbHelper = new MyDbHelper(AccountAdd.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (alias.equals("") || endpoint.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                } else if (endpoint.startsWith("http://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.nosslwarning), Toast.LENGTH_SHORT).show();
                } else if (!endpoint.startsWith("https://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
                } else if (db != null) {
                    // Database Queries
                    try {
                        if (edit) {
                            db.execSQL("UPDATE account SET id=\""+id+"\", endpoint=\""+endpoint+"\", username=\""+username+"\", password=\""+password+"\" WHERE id=\""+id+"\"");
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountsave_success), Toast.LENGTH_SHORT).show();
                        } else {
                            db.execSQL("INSERT INTO account VALUES (\""+alias+"\", \""+endpoint+"\", \""+username+"\", \""+password+"\")");
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_success), Toast.LENGTH_SHORT).show();
                        }
                        mainmenu();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_fail), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        accounttest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                alias = aapick.getText().toString();
                endpoint = aepick.getText().toString();
                username = aupick.getText().toString();
                password = appick.getText().toString();
                if (alias.equals("") || endpoint.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                } else if (endpoint.startsWith("http://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.nosslwarning), Toast.LENGTH_SHORT).show();
                } else if (!endpoint.startsWith("https://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
                } else {
                    Thread servertest = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                //Your code goes here
                                myCredentials = new BasicAWSCredentials(username, password);
                                s3client = new AmazonS3Client(myCredentials, region);
                                s3client.setEndpoint(endpoint);
                                s3client.setS3ClientOptions(s3ClientOptions);
                                List<Bucket> buckets = s3client.listBuckets();
                                //System.out.println(Name);

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Sending reference and data to Adapter
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accounttest_success), Toast.LENGTH_SHORT).show();
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
                            }
                        }
                    });
                    servertest.start();
                }
            }
        });
    }

    private void mainmenu() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);

    }

}