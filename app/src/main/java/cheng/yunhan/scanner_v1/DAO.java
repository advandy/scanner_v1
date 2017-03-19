package cheng.yunhan.scanner_v1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rabbitsong on 19/03/17.
 */

public class DAO {
    private static DAO instance;
    private Context context;
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public static synchronized DAO getInstance(Context context) {
        if (instance == null) {
            instance = new DAO(context.getApplicationContext());
        }
        return instance;
    }

    public DAO(Context context) {
        this.context = context;
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public long addItem(String shop, String article, String category, Double sum, int day, int month, int year) {
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_NAME_SHOP, shop);
        values.put(ItemEntry.COLUMN_NAME_ARTICLE, article);
        values.put(ItemEntry.COLUMN_NAME_CATEGORY, category);
        values.put(ItemEntry.COLUMN_NAME_SUM, sum);
        values.put(ItemEntry.COLUMN_NAME_DAY, day);
        values.put(ItemEntry.COLUMN_NAME_MONTH, month);
        values.put(ItemEntry.COLUMN_NAME_YEAR, year);
        return db.insert(ItemEntry.TABLE_NAME, null, values);
    }

    public void queryItemsByMonth(int month, int year) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_NAME_ARTICLE,
                ItemEntry.COLUMN_NAME_CATEGORY,
                ItemEntry.COLUMN_NAME_SUM,
                ItemEntry.COLUMN_NAME_SHOP,
                ItemEntry.COLUMN_NAME_DAY,
                ItemEntry.COLUMN_NAME_MONTH,
                ItemEntry.COLUMN_NAME_YEAR,
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = ItemEntry.COLUMN_NAME_MONTH + " = ? AND " +
                           ItemEntry.COLUMN_NAME_YEAR + " = ?";
        String[] selectionArgs = {String.valueOf(month), String.valueOf(year)};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                ItemEntry.COLUMN_NAME_DAY + " DESC";

       /* Cursor cursor = db.query(
                ItemEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                ItemEntry.COLUMN_NAME_DAY,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );*/

        Cursor cursor = db.rawQuery("Select day, shop,sum(sum) as sum from item where month=3 AND year=2017 group by day, shop", null);
        //cursor = db.rawQuery("Select day from item", null);
                //Cursor cursor = db.rawQuery("Select * from " + ItemEntry.TABLE_NAME, null);

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            //long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(ItemEntry._ID));
            //String article = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_NAME_ARTICLE));
            //String category = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_NAME_CATEGORY));
            Double sum = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_NAME_SUM));
            Log.e("sum", String.valueOf(sum));
            //itemIds.add(itemId);
        }
        cursor.close();

    }

    public static class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COLUMN_NAME_SHOP = "shop";
        public static final String COLUMN_NAME_ARTICLE = "article";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_SUM = "sum";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_YEAR = "year";

    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                    ItemEntry._ID + " INTEGER PRIMARY KEY," +
                    ItemEntry.COLUMN_NAME_SHOP + " TEXT," +
                    ItemEntry.COLUMN_NAME_CATEGORY + " TEXT," +
                    ItemEntry.COLUMN_NAME_ARTICLE + " TEXT," +
                    ItemEntry.COLUMN_NAME_DAY + " NUMBER," +
                    ItemEntry.COLUMN_NAME_MONTH + " NUMBER," +
                    ItemEntry.COLUMN_NAME_YEAR + " NUMBER," +
                    ItemEntry.COLUMN_NAME_SUM + " DOUBLE)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;

    public class DBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Butler.db";

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
    }
}
