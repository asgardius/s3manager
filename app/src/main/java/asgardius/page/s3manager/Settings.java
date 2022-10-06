package asgardius.page.s3manager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import asgardius.page.s3manager.databinding.ActivitySettingsBinding;

public class Settings extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //This is to add new user account
        Button saveprefs = (Button)findViewById(R.id.saveprefs);
        saveprefs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                //This launch account add screen
                //addaccount(false);
            }
        });

        //This is to view app credits
        Button about = (Button)findViewById(R.id.settings_button);
        about.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                aboutPage();
            }
        });
    }

    private void aboutPage () {

        Intent intent = new Intent(this, WebBrowser.class);
        intent.putExtra("web_url", "file:///android_asset/about.htm");
        intent.putExtra("title", getResources().getString(R.string.about_button));
        startActivity(intent);

    }
}