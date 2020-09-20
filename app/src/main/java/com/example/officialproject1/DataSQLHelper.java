package com.example.officialproject1;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DataSQLHelper extends SQLiteOpenHelper {
    public static String DB_NAME = "ValueData1";
    public static int DB_VERSION = 1;
    public static String tableName = "ValData";
    public DataSQLHelper(Context ctx){
        super(ctx,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE "+tableName+" (_id Integer primary key autoincrement, Speed real, GForce real);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
    public void dropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+tableName);
        onCreate(db);
    }
    public void insertData(double speed, double gforce){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Speed", speed);
        cv.put("GForce", gforce);
        db.insert(tableName,null,cv);
    }
    public Cursor getData(){
        SQLiteDatabase db = this.getReadableDatabase();
        String getQuery = "Select * from "+tableName;
        Cursor cursor = db.rawQuery(getQuery,null);
        return cursor;
    }
}
