package asgardius.page.s3manager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import asgardius.page.s3manager.databinding.ActivitySettingsBinding;

public class Settings extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    MyDbHelper dbHelper;
    SQLiteDatabase db;
    int videocache, videotime;
    EditText vcachepick, vtimepick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vcachepick = (EditText)findViewById(R.id.videocache);
        vtimepick = (EditText)findViewById(R.id.videotime);
        dbHelper = new MyDbHelper(this);
        Thread getprefs = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    // Database Queries
                    db = dbHelper.getWritableDatabase();
                    String query = "SELECT value FROM preferences where setting='videocache'";
                    Cursor cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        videocache = (Integer.parseInt(cursor.getString(0)));
                    }
                    query = "SELECT value FROM preferences where setting='videotime'";
                    cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        videotime = (Integer.parseInt(cursor.getString(0)));
                    }
                    db.close();
                    runOnUiThread(new Runnable() {

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            vcachepick.setText(Integer.toString(videocache));
                            vtimepick.setText(Integer.toString(videotime));
                        }
                    });
                    System.out.println("videocache " + videocache);
                    System.out.println("videotime " + videotime);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        getprefs.start();

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