package asgardius.page.s3manager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AccountAdd extends AppCompatActivity {
    EditText aapick, aupick, appick, aepick, arpick, pdfpick;
    SwitchMaterial pathstyle;
    String alias, username, password, endpoint, id, location, pdfendpoint, style;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    Region region;
    boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_add);
        aapick = (EditText)findViewById(R.id.alias);
        pdfpick = (EditText)findViewById(R.id.pdfendpoint);
        aepick = (EditText)findViewById(R.id.endpoint);
        arpick = (EditText)findViewById(R.id.region);
        aupick = (EditText)findViewById(R.id.username);
        appick = (EditText)findViewById(R.id.password);
        pathstyle = (SwitchMaterial) findViewById(R.id.pathstyle);
        Button register = (Button)findViewById(R.id.addaccount);
        Button accounttest = (Button)findViewById(R.id.testaccount);
        edit = getIntent().getBooleanExtra("edit", false);
        if (edit) {
            getSupportActionBar().setTitle(getResources().getString(R.string.accountedit_button));
            register.setText(getResources().getString(R.string.accountsave_button));
            id = getIntent().getStringExtra("alias");
            endpoint = getIntent().getStringExtra("endpoint");
            username = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
            location = getIntent().getStringExtra("region");
            pdfendpoint = getIntent().getStringExtra("pdfendpoint");
            if (getIntent().getBooleanExtra("style", false)) {
                pathstyle.setChecked(true);
            } else {
                pathstyle.setChecked(false);
            }
            aapick.setText(id);
            aepick.setText(endpoint);
            //aapick.setEnabled(false);
            aupick.setText(username);
            appick.setText(password);
            arpick.setText(location);
            pdfpick.setText(pdfendpoint);
        } else{
            getSupportActionBar().setTitle(getResources().getString(R.string.accountadd_button));
        }



        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                alias = aapick.getText().toString();
                pdfendpoint = pdfpick.getText().toString();
                endpoint = aepick.getText().toString();
                location = arpick.getText().toString();
                username = aupick.getText().toString();
                password = appick.getText().toString();
                if (pathstyle.isChecked()) {
                    style = "1";
                } else {
                    style = "0";
                }
                MyDbHelper dbHelper = new MyDbHelper(AccountAdd.this);
                if (alias.equals("") && endpoint.equals("") && username.equals(getResources().getString(R.string.access_key))) {
                    endpoint = getResources().getString(R.string.endpoint_url);
                    alias = "Google Test";
                    pdfendpoint = getResources().getString(R.string.pdf_reader);
                }
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (alias.equals("") || endpoint.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                } else if (endpoint.startsWith("http://") || pdfendpoint.startsWith("http://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.nosslwarning), Toast.LENGTH_SHORT).show();
                } else if (!endpoint.startsWith("https://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
                } else if (db != null) {
                    // Database Queries
                    try {
                        if (location.equals("")) {
                            location = "us-east-1";
                        }
                        if(endpoint.endsWith("/")) {
                            endpoint = endpoint.substring(0, endpoint.length()-1);
                        }
                        if (edit) {
                            db.execSQL("UPDATE account SET id=\""+alias+"\", endpoint=\""+endpoint+"\", username=\""+username+"\", password=\""+password+"\", region=\""+location+"\", pdfendpoint=\""+pdfendpoint+"\", style=\""+style+"\" WHERE id=\""+id+"\"");
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountsave_success), Toast.LENGTH_SHORT).show();
                        } else {
                            db.execSQL("INSERT INTO account VALUES (\""+alias+"\", \""+endpoint+"\", \""+username+"\", \""+password+"\", \""+location+"\", \""+pdfendpoint+"\", \""+style+"\")");
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
                location = arpick.getText().toString();
                if (alias.equals("") && endpoint.equals("") && username.equals(getResources().getString(R.string.access_key))) {
                    endpoint = getResources().getString(R.string.endpoint_url);
                    alias = "Google Test";
                }
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
                                if (endpoint.contains(getResources().getString(R.string.aws_endpoint)) || location.equals("")) {
                                    region = Region.getRegion("us-east-1");
                                } else {
                                    region = Region.getRegion(location);
                                }
                                if(endpoint.endsWith("/")) {
                                    endpoint = endpoint.substring(0, endpoint.length()-1);
                                }
                                S3ClientOptions s3ClientOptions = S3ClientOptions.builder().build();
                                s3ClientOptions.setPathStyleAccess(pathstyle.isChecked());
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
                                        Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show();
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