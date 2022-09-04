package asgardius.page.s3manager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static boolean DEFAULT_PATH_STYLE_ACCESS = true;
    String username, password, endpoint;
    RecyclerView recyclerView;
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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db != null) {
            // Database Queries
            System.out.println("Database OK");
            Name = new ArrayList<String>();
            Img = new ArrayList<String>();
            String query = "SELECT id FROM account";
            Cursor cursor = db.rawQuery(query,null);
            while (cursor.moveToNext()){
                Name.add(cursor.getString(0));
                Img.add(R.drawable.account);
            }
        } else {
            System.out.println("Database Missing");
        }

        Thread listaccount = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here

                    System.out.println(Name);

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
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.media_list_fail), Toast.LENGTH_SHORT).show();
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
                if (db != null) {
                    // Database Queries
                    System.out.println("Database OK");
                    try {
                        String query = "SELECT endpoint, username, password FROM account where id=\""+ Name.get(position).toString()+ "\"";
                        System.out.println(query);
                        Cursor cursor = db.rawQuery(query,null);
                        if (cursor.moveToNext()){
                            endpoint = cursor.getString(0);
                            username = cursor.getString(1);
                            password = cursor.getString(2);
                            explorer();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onLongClick(View view, int position) {
                System.out.println("Long click on "+Name.get(position).toString());
            }
        }));

        //This is to launch video playback test
        Button addaccount = (Button)findViewById(R.id.addaccount);
        addaccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                addaccount();
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
        startActivity(intent);

    }

    private void addaccount() {

        Intent intent = new Intent(this, AccountAdd.class);
        startActivity(intent);

    }

}