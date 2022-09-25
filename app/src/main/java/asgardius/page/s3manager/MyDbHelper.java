package asgardius.page.s3manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String atcreate = "CREATE TABLE IF NOT EXISTS account(id text UNIQUE, endpoint text, username text, password text, region text, pdfendpoint text)";
    //private static final String upgrade = "ALTER TABLE account add column pdfendpoint text";
    private static final int DATABASE_VERSION = 1;
    private static final String dbname = "accounts.sqlite3";
    private static final int dbversion = 3;
    public MyDbHelper(Context context) {
        super(context, dbname, null, dbversion);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(atcreate);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(upgrade);
    }
}
