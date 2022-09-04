package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AccountAdd extends AppCompatActivity {
    EditText aapick, aupick, appick, aepick;
    String alias, username, password, endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_add);
        aapick = (EditText)findViewById(R.id.alias);
        aepick = (EditText)findViewById(R.id.endpoint);
        aupick = (EditText)findViewById(R.id.username);
        appick = (EditText)findViewById(R.id.password);
        Button register = (Button)findViewById(R.id.addaccount);
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                alias = aapick.getText().toString();
                endpoint = aepick.getText().toString();
                username = aupick.getText().toString();
                password = appick.getText().toString();
                System.out.println("Alias " + alias);
                System.out.println("Endpoint " + endpoint);
                System.out.println("Username " + username);
                System.out.println("Password " + password);
                MyDbHelper dbHelper = new MyDbHelper(AccountAdd.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (alias.equals("") || endpoint.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                } else if (db != null) {
                    // Database Queries
                    try {
                        db.execSQL("INSERT INTO account VALUES (\""+alias+"\", \""+endpoint+"\", \""+username+"\", \""+password+"\")");
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_success), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_fail), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}