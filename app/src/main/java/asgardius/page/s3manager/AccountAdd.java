package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AccountAdd extends AppCompatActivity {
    EditText aapick, aupick, appick, aepick;
    String alias, username, password, endpoint, id;
    boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_add);
        aapick = (EditText)findViewById(R.id.alias);
        aepick = (EditText)findViewById(R.id.endpoint);
        aupick = (EditText)findViewById(R.id.username);
        appick = (EditText)findViewById(R.id.password);
        Button register = (Button)findViewById(R.id.addaccount);
        edit = getIntent().getBooleanExtra("edit", false);
        if (edit) {
            register.setText(getResources().getString(R.string.accountsave_button));
            id = getIntent().getStringExtra("alias");
            endpoint = getIntent().getStringExtra("endpoint");
            username = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
            aapick.setText(id);
            //aapick.setEnabled(false);
            aepick.setText(endpoint);
            aupick.setText(username);
            appick.setText(password);
        }
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //buttonaction
                alias = aapick.getText().toString();
                endpoint = aepick.getText().toString();
                username = aupick.getText().toString();
                password = appick.getText().toString();
                MyDbHelper dbHelper = new MyDbHelper(AccountAdd.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (alias.equals("") || endpoint.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_null), Toast.LENGTH_SHORT).show();
                } else if (endpoint.startsWith("http://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.nosslwarning), Toast.LENGTH_SHORT).show();
                } else if (!endpoint.startsWith("https://")) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
                } else if (db != null) {
                    // Database Queries
                    try {
                        if (edit) {
                            db.execSQL("UPDATE account SET id=\""+id+"\", endpoint=\""+endpoint+"\", username=\""+username+"\", password=\""+password+"\" WHERE id=\""+id+"\"");
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountsave_success), Toast.LENGTH_SHORT).show();
                        } else {
                            db.execSQL("INSERT INTO account VALUES (\""+alias+"\", \""+endpoint+"\", \""+username+"\", \""+password+"\")");
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_success), Toast.LENGTH_SHORT).show();
                        }
                        mainmenu();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.accountadd_fail), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    private void mainmenu() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);

    }

}