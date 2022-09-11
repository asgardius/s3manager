package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Uploader extends AppCompatActivity {
    String file, username, password, endpoint, bucket, prefix, location;

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
        Toast.makeText(Uploader.this, getResources().getString(R.string.pending_feature), Toast.LENGTH_SHORT).show();
        file = upload().getData() != null ? upload().getData().toString() : null;
    }

    private Intent upload() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, 100);
        return intent;
    }
}