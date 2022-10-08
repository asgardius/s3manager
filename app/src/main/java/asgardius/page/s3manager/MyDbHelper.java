package asgardius.page.s3manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String usertable = "CREATE TABLE IF NOT EXISTS account(id text UNIQUE, endpoint text, username text, password text, region text, pdfendpoint text)";
    private static final String preftable = "CREATE TABLE IF NOT EXISTS preferences(setting text UNIQUE, value text)";
    private static final String setvideocache = "INSERT INTO preferences VALUES ('videocache', '300')";
    private static final String setvideotime = "INSERT INTO preferences VALUES ('videotime', '3')";
    private static final String setbuffersize = "INSERT INTO preferences VALUES ('buffersize', '12000')";
    //private static final String upgrade = "ALTER TABLE account add column pdfendpoint text";
    private static final int DATABASE_VERSION = 1;
    private static final String dbname = "accounts.sqlite3";
    private static final int dbversion = 3;
    public MyDbHelper(Context context) {
        super(context, dbname, null, dbversion);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(usertable);
        db.execSQL(preftable);
        db.execSQL(setvideocache);
        db.execSQL(setvideotime);
        db.execSQL(setbuffersize);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(upgrade);
    }
}
