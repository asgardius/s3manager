package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CorsConfig extends AppCompatActivity {
    String username, password, endpoint, bucket, location, title;
    URI pdfendpoint;
    Region region;
    S3ClientOptions s3ClientOptions;
    AWSCredentials myCredentials;
    AmazonS3 s3client;
    BucketCrossOriginConfiguration bucketcors;
    boolean style;
    boolean allorigins, pdfcompatible, found = false;
    TextView origins;
    Button allowall, allowpdf, deletecors;
    BucketCrossOriginConfiguration corsconfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cors_config);
        endpoint = getIntent().getStringExtra("endpoint");
        title = getIntent().getStringExtra("title");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bucket = getIntent().getStringExtra("bucket");
        style = getIntent().getBooleanExtra("style", false);
        location = getIntent().getStringExtra("region");
        allowall = (Button)findViewById(R.id.allow_all);
        allowpdf = (Button)findViewById(R.id.allow_pdf);
        deletecors = (Button)findViewById(R.id.delete_cors);
        try {
            pdfendpoint = new URI(getIntent().getStringExtra("pdfendpoint"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setTitle(bucket+"/");
        region = Region.getRegion(location);
        s3ClientOptions = S3ClientOptions.builder().build();
        myCredentials = new BasicAWSCredentials(username, password);
        origins = (TextView) findViewById(R.id.origins);
        try {
            s3client = new AmazonS3Client(myCredentials, region);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
        }
        s3client.setEndpoint(endpoint);
        s3ClientOptions.setPathStyleAccess(style);
        s3client.setS3ClientOptions(s3ClientOptions);
        Thread getCors = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    bucketcors = s3client.getBucketCrossOriginConfiguration(bucket);
                    if (bucketcors != null) {
                        List<CORSRule> corsRules = bucketcors.getRules();
                        if (!corsRules.isEmpty()) {
                            found = true;
                            for (CORSRule rule: corsRules) {
                                //System.out.println("allowOrigins: "+rule.getAllowedOrigins());
                                //System.out.println("AllowedMethod: "+rule.getAllowedMethods());
                                if (rule.getAllowedOrigins().toString().equals("[*]")) {
                                    allorigins = true;
                                } else if (rule.getAllowedOrigins().toString().contains("https://"+pdfendpoint.getHost())) {
                                    pdfcompatible = true;
                                }
                            }
                        }
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (allorigins) {
                                origins.setText(getResources().getString(R.string.cors_all));
                                deletecors.setVisibility(View.VISIBLE);
                            } else if (pdfcompatible) {
                                origins.setText(getResources().getString(R.string.cors_pdf));
                                deletecors.setVisibility(View.VISIBLE);
                                allowall.setVisibility(View.VISIBLE);
                            } else if (found) {
                                origins.setText(getResources().getString(R.string.cors_npdf));
                                deletecors.setVisibility(View.VISIBLE);
                                allowpdf.setVisibility(View.VISIBLE);
                                allowall.setVisibility(View.VISIBLE);
                            } else {
                                origins.setText(getResources().getString(R.string.cors_none));
                                allowall.setVisibility(View.VISIBLE);
                                allowpdf.setVisibility(View.VISIBLE);
                            }
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
        getCors.start();
        allowall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                setAllowall();
            }
        });
        allowpdf.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                setAllowpdf();
            }
        });
        deletecors.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                setDeletecors();
            }
        });
    }

    public void setAllowall() {
        Thread addAll = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    List<CORSRule.AllowedMethods> allowMethods = new ArrayList<>();
                    allowMethods.add(CORSRule.AllowedMethods.GET);
                    List<String> allowOrigins = new ArrayList<>();
                    allowOrigins.add("*");
                    CORSRule corsRules = new CORSRule().withAllowedMethods(allowMethods).withAllowedOrigins(allowOrigins);
                    corsconfig = new BucketCrossOriginConfiguration().withRules(corsRules);
                    s3client.setBucketCrossOriginConfiguration(bucket, corsconfig);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.cors_ok), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.cors_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
        addAll.start();
    }

    public void setAllowpdf() {
        Thread addPdf = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    List<CORSRule.AllowedMethods> allowMethods = new ArrayList<>();
                    allowMethods.add(CORSRule.AllowedMethods.GET);
                    List<String> allowOrigins = new ArrayList<>();
                    allowOrigins.add("https://"+pdfendpoint.getHost());
                    CORSRule corsRules = new CORSRule().withAllowedMethods(allowMethods).withAllowedOrigins(allowOrigins);
                    corsconfig = new BucketCrossOriginConfiguration().withRules(corsRules);
                    s3client.setBucketCrossOriginConfiguration(bucket, corsconfig);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.cors_ok), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.cors_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
        addPdf.start();
    }

    public void setDeletecors() {
        Thread delCors = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    s3client.deleteBucketCrossOriginConfiguration(bucket);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.cors_ok), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.cors_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
        delCors.start();
    }
}