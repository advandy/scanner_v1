package cheng.yunhan.butler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

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


    public double queryMonthlyIncomeSum(int month, int year) {
        Cursor cursor= db.rawQuery("Select sum(income_sum) as income_sum from item where month=" + month + " AND year=" + year, null);
        Double reVal = 0.00;
        if (cursor.moveToFirst()) {
            reVal = cursor.getDouble(0);
        }
        cursor.close();
        return reVal;
    }

    public double queryMonthlyExpenseSum(int month, int year) {
        Cursor cursor= db.rawQuery("Select sum(sum) as sum from item where month=" + month + " AND year=" + year, null);
        Double reVal = 0.00;
        if (cursor.moveToFirst()) {
            reVal = cursor.getDouble(0);
        }
        cursor.close();
        return reVal;
    }

    public long addIncomeItem(String category, Double sum, int day, int month, int year) {
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_NAME_INCOMECATEGORY, category);
        values.put(ItemEntry.COLUMN_NAME_INCOMESUM, sum);
        values.put(ItemEntry.COLUMN_NAME_DAY, day);
        values.put(ItemEntry.COLUMN_NAME_MONTH, month);
        values.put(ItemEntry.COLUMN_NAME_YEAR, year);
        return db.insert(ItemEntry.TABLE_NAME, null, values);
    }

    public long addExpenseItem(String shop, String article, String category, Double sum, int day, int month, int year) {
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

    public ArrayList<ContentValues> queryDailyShopItems(int day, int month, int year, String shop) {
        Cursor cursor = db.rawQuery("Select * from item where day=" + day + " AND month=" + month + " AND year=" + year + " AND shop='" + shop + "'", null);
        ArrayList<ContentValues> retVal = new ArrayList<ContentValues>();
        ContentValues map;
        while(cursor.moveToNext()) {

            map = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, map);
            retVal.add(map);
        }
        cursor.close();
        return retVal;
    }

    public ArrayList<ContentValues> queryItemsByMonth(int month, int year) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_NAME_INCOMECATEGORY,
                ItemEntry.COLUMN_NAME_INCOMESUM,
                "SUM (" + ItemEntry.COLUMN_NAME_SUM + ") AS " + ItemEntry.COLUMN_NAME_SUM,
                ItemEntry.COLUMN_NAME_SHOP,
                ItemEntry.COLUMN_NAME_DAY,
                ItemEntry.COLUMN_NAME_MONTH,
                ItemEntry.COLUMN_NAME_YEAR,
        };

        // Filter results WHERE "title" = 'My Title'
        String selection =  ItemEntry.COLUMN_NAME_MONTH + " = ? AND " +
                            ItemEntry.COLUMN_NAME_YEAR + " = ?";
        String[] selectionArgs = {String.valueOf(month), String.valueOf(year)};

        String groupBy = ItemEntry.COLUMN_NAME_DAY + ", IFNULL(" + ItemEntry.COLUMN_NAME_SHOP + ", "+ ItemEntry._ID +")";
        // How you want the results sorted in the resulting Cursor
        String sortOrder = ItemEntry.COLUMN_NAME_DAY + " DESC";

        Cursor cursor = db.query(
                ItemEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                groupBy,                                  // group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<ContentValues> retVal = new ArrayList<>();
        ContentValues map;
        while(cursor.moveToNext()) {

            map = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, map);
            retVal.add(map);
        }
        cursor.close();

        return retVal;

    }

    public static class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COLUMN_NAME_SHOP = "shop";
        public static final String COLUMN_NAME_ARTICLE = "article";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_SUM = "sum";
        public static final String COLUMN_NAME_INCOMESUM = "income_sum";
        public static final String COLUMN_NAME_INCOMECATEGORY = "income_category";
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
                    ItemEntry.COLUMN_NAME_SUM + " DOUBLE," +
                    ItemEntry.COLUMN_NAME_INCOMECATEGORY + " TEXT," +
                    ItemEntry.COLUMN_NAME_INCOMESUM + " DOUBLE)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;

    public class DBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 2;
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
