package asgardius.page.s3manager;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    boolean style;
    String alias, username, password, endpoint, location, pdfendpoint;
    RecyclerView recyclerView;
    SQLiteDatabase db;
    ArrayList Name;
    ArrayList Img;
    MyDbHelper dbHelper;
    int videocache, videotime, buffersize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.alist);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        dbHelper = new MyDbHelper(this);
        Thread getprefs = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    // Database Queries
                    try {
                        db = dbHelper.getWritableDatabase();
                        String query = "SELECT value FROM preferences where setting='videocache'";
                        Cursor cursor = db.rawQuery(query,null);
                        while (cursor.moveToNext()){
                            videocache = (Integer.parseInt(cursor.getString(0)));
                        }
                        db.close();
                        if (videocache == 0) {
                            try {
                                db = dbHelper.getWritableDatabase();
                                db.execSQL("INSERT INTO preferences VALUES ('videocache', '300')");
                                videocache = 300;
                                db.close();
                            } catch (Exception e) {
                                db = dbHelper.getWritableDatabase();
                                db.execSQL("CREATE TABLE IF NOT EXISTS preferences(setting text UNIQUE, value text)");
                                db.execSQL("INSERT INTO preferences VALUES ('videocache', '300')");
                                db.execSQL("INSERT INTO preferences VALUES ('videotime', '3')");
                                db.execSQL("INSERT INTO preferences VALUES ('buffersize', '12000')");
                                videocache = 300;
                                //videotime = 3;
                                db.close();
                            }
                        }
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
                    try {
                        db = dbHelper.getWritableDatabase();
                        String query = "SELECT value FROM preferences where setting='videotime'";
                        Cursor cursor = db.rawQuery(query,null);
                        while (cursor.moveToNext()){
                            videotime = (Integer.parseInt(cursor.getString(0)));
                        }
                        db.close();
                        if (videotime == 0) {
                            try {
                                db = dbHelper.getWritableDatabase();
                                db.execSQL("INSERT INTO preferences VALUES ('videotime', '3')");
                                videotime = 3;
                                db.close();
                            } catch (Exception e) {
                                db = dbHelper.getWritableDatabase();
                                db.execSQL("CREATE TABLE IF NOT EXISTS preferences(setting text UNIQUE, value text)");
                                db.close();
                            }
                        }
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
                    try {
                        db = dbHelper.getWritableDatabase();
                        String query = "SELECT value FROM preferences where setting='buffersize'";
                        Cursor cursor = db.rawQuery(query,null);
                        while (cursor.moveToNext()){
                            buffersize = (Integer.parseInt(cursor.getString(0)));
                        }
                        db.close();
                        if (buffersize == 0) {
                            try {
                                db = dbHelper.getWritableDatabase();
                                db.execSQL("INSERT INTO preferences VALUES ('buffersize', '12000')");
                                buffersize = 12000;
                                db.close();
                            } catch (Exception e) {
                                db = dbHelper.getWritableDatabase();
                                db.execSQL("CREATE TABLE IF NOT EXISTS preferences(setting text UNIQUE, value text)");
                                db.close();
                            }
                        }
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
        Thread listaccount = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    db = dbHelper.getWritableDatabase();
                    // Database Queries
                    Name = new ArrayList<String>();
                    Img = new ArrayList<String>();
                    String query = "SELECT id FROM account";
                    Cursor cursor = db.rawQuery(query,null);
                    while (cursor.moveToNext()){
                        Name.add(cursor.getString(0));
                        Img.add(R.drawable.account);
                    }
                    db.close();
                    getprefs.start();
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
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
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
                //System.out.println("Click on "+Name.get(position).toString());
                db = dbHelper.getWritableDatabase();
                if (db != null) {
                    // Database Queries
                    try {
                        //This retrieves credentials from selected account
                        String query = "SELECT endpoint, username, password, region, pdfendpoint, style FROM account where id=\""+ Name.get(position).toString()+ "\"";
                        Cursor cursor = db.rawQuery(query,null);
                        if (cursor.moveToNext()){
                            endpoint = cursor.getString(0);
                            username = cursor.getString(1);
                            password = cursor.getString(2);
                            location = cursor.getString(3);
                            pdfendpoint = cursor.getString(4);
                            try {
                                style = cursor.getString(5).equals("1");
                            } catch (Exception e) {
                                e.printStackTrace();
                                style = false;
                            }

                            db.close();
                            //This launch file explorer using selected account
                            explorer();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            db = dbHelper.getWritableDatabase();
                            db.execSQL("alter table account add column style text");
                            db.close();
                        } catch (Exception f) {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //System.out.println("Long click on "+Name.get(position).toString());
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(recyclerView.getContext(), view);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.account_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Toast message on menu item clicked
                        //Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        if (menuItem.getTitle() == getResources().getString(R.string.accountedit_button)) {
                            try {
                                db = dbHelper.getWritableDatabase();
                                //This retrieves credentials from selected account
                                String query = "SELECT id, endpoint, username, password, region, pdfendpoint, style FROM account where id=\""+ Name.get(position).toString()+ "\"";
                                System.out.println(query);
                                Cursor cursor = db.rawQuery(query,null);
                                if (cursor.moveToNext()){
                                    alias = cursor.getString(0);
                                    endpoint = cursor.getString(1);
                                    username = cursor.getString(2);
                                    password = cursor.getString(3);
                                    location = cursor.getString(4);
                                    pdfendpoint = cursor.getString(5);
                                    try {
                                        style = cursor.getString(6).equals("1");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        style = false;
                                    }
                                }
                                db.close();
                                //This launch account editor
                                addaccount(true);
                                //Toast.makeText(MainActivity.this, "This feature is not yet implemented", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    db = dbHelper.getWritableDatabase();
                                    db.execSQL("alter table account add column style text");
                                    db.close();
                                } catch (Exception f) {
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }
                        else if (menuItem.getTitle() == getResources().getString(R.string.accountdel_button)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle(Name.get(position).toString());
                            builder.setMessage(getResources().getString(R.string.accountdel_confirm));
                            builder.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            db = dbHelper.getWritableDatabase();
                                            if (db != null) {
                                                // This remove selected user account from local database
                                                try {
                                                    db.execSQL("DELETE FROM account where id=\""+ Name.get(position).toString()+ "\"");
                                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountdel_success), Toast.LENGTH_SHORT).show();
                                                    db.close();
                                                    recreate();
                                                } catch (Exception e) {
                                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            /**/
                            //Toast.makeText(MainActivity.this, "Delete Account", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        }));

        //This is to add new user account
        Button addaccount = (Button)findViewById(R.id.addaccount);
        addaccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                //This launch account add screen
                addaccount(false);
            }
        });

        //This is to view app credits
        Button settings = (Button)findViewById(R.id.settings_button);
        settings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                settingsPage();
            }
        });
    }

    private void explorer() {

        Intent intent = new Intent(this, BucketSelect.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("region", location);
        intent.putExtra("pdfendpoint", pdfendpoint);
        intent.putExtra("style", style);
        intent.putExtra("videocache", videocache);
        intent.putExtra("videotime", videotime);
        intent.putExtra("buffersize", buffersize);
        startActivity(intent);

    }

    private void addaccount(boolean edit) {

        Intent intent = new Intent(this, AccountAdd.class);
        if (edit) {
            intent.putExtra("alias", alias);
            intent.putExtra("endpoint", endpoint);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            intent.putExtra("region", location);
            intent.putExtra("pdfendpoint", pdfendpoint);
            intent.putExtra("style", style);
        }
        intent.putExtra("edit", edit);
        startActivity(intent);

    }

    private void settingsPage() {

        Intent intent = new Intent(this, Settings.class);
        //intent.putExtra("web_url", "file:///android_asset/about.htm");
        //intent.putExtra("title", getResources().getString(R.string.about_button));
        startActivity(intent);

    }
}