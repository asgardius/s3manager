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

import com.google.android.material.switchmaterial.SwitchMaterial;

import asgardius.page.s3manager.databinding.ActivitySettingsBinding;

public class Settings extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    MyDbHelper dbHelper;
    SQLiteDatabase db;
    String videocache, videotime, buffersize, playlisttime;
    EditText vcachepick, vtimepick, bsizepick, ptimepick;
    Button saveprefs, about;
    SwitchMaterial isplaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vcachepick = (EditText)findViewById(R.id.videocache);
        vtimepick = (EditText)findViewById(R.id.videotime);
        bsizepick = (EditText)findViewById(R.id.buffersize);
        ptimepick = (EditText)findViewById(R.id.playlisttime);
        dbHelper = new MyDbHelper(this);
        isplaylist = (SwitchMaterial) findViewById(R.id.isplaylist);
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
                    query = "SELECT value FROM preferences where setting='buffersize'";
                    cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        buffersize = (cursor.getString(0));
                    }
                    query = "SELECT value FROM preferences where setting='isplaylist'";
                    cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        isplaylist.setChecked(cursor.getString(0).equals("1"));
                    }
                    query = "SELECT value FROM preferences where setting='playlisttime'";
                    cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        playlisttime = (cursor.getString(0));
                    }
                    db.close();
                    runOnUiThread(new Runnable() {

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            vcachepick.setText(videocache);
                            vtimepick.setText(videotime);
                            bsizepick.setText(buffersize);
                            ptimepick.setText(playlisttime);
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
        saveprefs = (Button)findViewById(R.id.saveprefs);
        saveprefs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                try {
                    videocache = vcachepick.getText().toString();
                    videotime = vtimepick.getText().toString();
                    buffersize = bsizepick.getText().toString();
                    playlisttime = ptimepick.getText().toString();
                    if (videocache.equals("") || videotime.equals("") || buffersize.equals("")) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                    } else if (videocache.equals("0") || videotime.equals("0") || playlisttime.equals("0")) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.not_zero), Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(buffersize) <= 2000) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.buffersize_error), Toast.LENGTH_SHORT).show();
                    }  else if (Integer.parseInt(videotime) > 168) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_expiration_date), Toast.LENGTH_SHORT).show();
                    } else {
                        db = dbHelper.getWritableDatabase();
                        db.execSQL("UPDATE preferences SET value='" + videocache + "' where setting='videocache'");
                        db.execSQL("UPDATE preferences SET value='" + videotime + "' where setting='videotime'");
                        db.execSQL("UPDATE preferences SET value='" + buffersize + "' where setting='buffersize'");
                        db.execSQL("UPDATE preferences SET value='" + playlisttime + "' where setting='playlisttime'");
                        if(isplaylist.isChecked()) {
                            db.execSQL("UPDATE preferences SET value='1' where setting='isplaylist'");
                        } else {
                            db.execSQL("UPDATE preferences SET value='0' where setting='isplaylist'");
                        }
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
        about = (Button)findViewById(R.id.settings_button);
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