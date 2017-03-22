package cheng.yunhan.scanner_v1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DailyExpenseList extends AppCompatActivity {
    private DAO DaoUtils;
    private Boolean updated = false;
    private ListView shoppingListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_expense_list);

        Intent intent = getIntent();
        String query_shop = intent.getStringExtra("shop");
        int query_day = intent.getIntExtra("day", 0);
        int query_month = intent.getIntExtra("month", 0);

        shoppingListView = (ListView) findViewById(R.id.dailyShoppingListView);

        DaoUtils = DAO.getInstance(this);

        ArrayList<ContentValues> shoppingList = DaoUtils.queryDailyShopItems(query_day, query_month, 2017, query_shop);

        ShoppingListViewAdapter shoppingListViewAdapter = new ShoppingListViewAdapter(this,0);
        for (ContentValues entry: shoppingList) {
            String article = entry.getAsString(DAO.ItemEntry.COLUMN_NAME_ARTICLE);
            String sum = entry.getAsString(DAO.ItemEntry.COLUMN_NAME_SUM);
            String category = entry.getAsString(DAO.ItemEntry.COLUMN_NAME_CATEGORY);
            shoppingListViewAdapter.add(article + sum + category);
        }
        shoppingListView.setAdapter(shoppingListViewAdapter);

    }


    public class ShoppingListViewAdapter extends ArrayAdapter<String> {

        public ShoppingListViewAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView tv = new TextView(parent.getContext());
            tv.setText(getItem(position));

            return tv;
        }
    }



    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent backIntent = new Intent();
        backIntent.putExtra("updated", updated);
        setResult(RESULT_OK, backIntent);
        finish();
    }
}
