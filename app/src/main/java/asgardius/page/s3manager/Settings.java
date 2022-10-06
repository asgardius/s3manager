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
    String videocache, videotime;
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
                        videocache = (cursor.getString(0));
                    }
                    query = "SELECT value FROM preferences where setting='videotime'";
                    cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        videotime = (cursor.getString(0));
                    }
                    db.close();
                    runOnUiThread(new Runnable() {

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            vcachepick.setText(videocache);
                            vtimepick.setText(videotime);
                        }
                    });
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
                try {
                    videocache = vcachepick.getText().toString();
                    videotime = vtimepick.getText().toString();
                    if (videocache.equals("") || videotime.equals("")) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                    } else {
                        db = dbHelper.getWritableDatabase();
                        db.execSQL("UPDATE preferences SET value='"+videocache+"' where setting='videocache'");
                        db.execSQL("UPDATE preferences SET value='"+videotime+"' where setting='videotime'");
                        db.close();
                        mainmenu();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
                }
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

    private void mainmenu() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);

    }
}