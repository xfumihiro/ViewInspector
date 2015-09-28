package view_inspector.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class DbOpenHelper extends SQLiteOpenHelper {
  public static final String DB_NAME = "viewInspector.db";
  private static final int VERSION = 1;

  private static final String CREATE_VIEW_PROFILE =
      "" + "CREATE TABLE " + ViewProfile.TABLE + "(" + ViewProfile.ID
          + " INTEGER NOT NULL PRIMARY KEY," + ViewProfile.VIEW_HASHCODE + " TEXT NOT NULL,"
          + ViewProfile.MEASURE_DURATION + " INTEGER NOT NULL DEFAULT -1,"
          + ViewProfile.LAYOUT_DURATION + " INTEGER NOT NULL DEFAULT -1,"
          + ViewProfile.DRAW_DURATION + " INTEGER NOT NULL DEFAULT -1" + ")";

  public DbOpenHelper(Context context) {
    super(context, DB_NAME, null, VERSION);
    // drop database if exists
    context.deleteDatabase(DbOpenHelper.DB_NAME);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_VIEW_PROFILE);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
}
