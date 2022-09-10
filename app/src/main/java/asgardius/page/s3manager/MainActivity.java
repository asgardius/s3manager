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

    static boolean DEFAULT_PATH_STYLE_ACCESS = true;
    String alias, username, password, endpoint, location;
    RecyclerView recyclerView;
    SQLiteDatabase db;
    ArrayList Name;
    ArrayList Img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.alist);

        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        MyDbHelper dbHelper = new MyDbHelper(this);
        db = dbHelper.getWritableDatabase();
        if (db != null) {
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
        } else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.broken_database), Toast.LENGTH_SHORT).show();
        }

        Thread listaccount = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
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
                        String query = "SELECT endpoint, username, password, region FROM account where id=\""+ Name.get(position).toString()+ "\"";
                        Cursor cursor = db.rawQuery(query,null);
                        if (cursor.moveToNext()){
                            endpoint = cursor.getString(0);
                            username = cursor.getString(1);
                            password = cursor.getString(2);
                            location = cursor.getString(3);
                            db.close();
                            explorer();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
                                String query = "SELECT id, endpoint, username, password, region FROM account where id=\""+ Name.get(position).toString()+ "\"";
                                System.out.println(query);
                                Cursor cursor = db.rawQuery(query,null);
                                if (cursor.moveToNext()){
                                    alias = cursor.getString(0);
                                    endpoint = cursor.getString(1);
                                    username = cursor.getString(2);
                                    password = cursor.getString(3);
                                    location = cursor.getString(4);
                                }
                                db.close();
                                addaccount(true);
                                //Toast.makeText(MainActivity.this, "This feature is not yet implemented", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if (menuItem.getTitle() == getResources().getString(R.string.accountdel_button)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle(getResources().getString(R.string.accountdel_button));
                            builder.setMessage(getResources().getString(R.string.accountdel_confirm));
                            builder.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            db = dbHelper.getWritableDatabase();
                                            if (db != null) {
                                                // Database Queries
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

        //This is to launch video playback test
        Button addaccount = (Button)findViewById(R.id.addaccount);
        addaccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                addaccount(false);
            }
        });

        //This is to launch file explorer test
        Button explorertest = (Button)findViewById(R.id.ltest);
        explorertest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
            }
        });
    }

    private void explorer() {

        Intent intent = new Intent(this, BucketSelect.class);
        intent.putExtra("endpoint", endpoint);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("region", location);
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
        }
        intent.putExtra("edit", edit);
        startActivity(intent);

    }
}