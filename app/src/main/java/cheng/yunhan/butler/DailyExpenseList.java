package cheng.yunhan.butler;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class DailyExpenseList extends AppCompatActivity {
    private DAO DaoUtils;
    private Boolean updated = false;
    private ListView shoppingListView;
    private int query_day;
    private int query_month;
    private String query_shop;
    private ShoppingListViewAdapter shoppingListViewAdapter;

    private boolean draftModus = true;
    private ArrayList<ContentValues> shoppingList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_expense_main, menu);
        if (draftModus == true) {
            MenuItem saveItem = menu.findItem(R.id.save);
            saveItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save:
                if (shoppingList.size() == 0) {
                    Toast.makeText(this, "No items to be saved", Toast.LENGTH_SHORT);
                }
                DaoUtils.saveDailyExpense(shoppingList);
                break;
            case R.id.addItem:
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.edit_item);

                final ContentValues entry = new ContentValues();

                final EditText article = (EditText) dialog.findViewById(R.id.article);
                article.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_ARTICLE));

                final EditText count = (EditText) dialog.findViewById(R.id.count);
                count.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_COUNT));

                final EditText sum = (EditText) dialog.findViewById(R.id.sum);
                sum.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_SUM));

                Button ok = (Button) dialog.findViewById(R.id.ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean error = false;
                        if (Utils.checkEditText(article)) {
                            error = true;
                        }

                        if (Utils.checkEditText(sum)) {
                            error = true;
                        }

                        if (Utils.checkEditText(count)){
                            error = true;
                        }

                        if (error) {
                            return;
                        }

                        String articleStr = String.valueOf(article.getText());
                        Double sumValue = Double.valueOf(String.valueOf(sum.getText()));
                        Integer countValue = Integer.valueOf(String.valueOf(count.getText()));

                        entry.put(DAO.ItemEntry.COLUMN_NAME_ARTICLE, articleStr);
                        entry.put(DAO.ItemEntry.COLUMN_NAME_SUM, String.valueOf(sum.getText()));
                        entry.put(DAO.ItemEntry.COLUMN_NAME_COUNT, String.valueOf(count.getText()));
                        entry.put(DAO.ItemEntry.COLUMN_NAME_SHOP, query_shop);
                        entry.put(DAO.ItemEntry.COLUMN_NAME_DAY, query_day);
                        entry.put(DAO.ItemEntry.COLUMN_NAME_MONTH, query_month);
                        entry.put(DAO.ItemEntry.COLUMN_NAME_YEAR, 2017);
                        if (draftModus == false) {
                            DaoUtils.saveDailyExpense(new ArrayList<ContentValues>(Arrays.asList(entry)));
                            //DaoUtils.addExpenseItem(query_shop, articleStr, "", countValue, sumValue, query_day, query_month, 2017);
                        }
                        shoppingListViewAdapter.add(entry);
                        updated = true;
                        dialog.dismiss();
                    }
                });

                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_daily_expense_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        query_shop = intent.getStringExtra("shop");
        query_day = intent.getIntExtra("day", 0);
        query_month = intent.getIntExtra("month", 0);

        shoppingListView = (ListView) findViewById(R.id.dailyShoppingListView);

        DaoUtils = DAO.getInstance(this);

        shoppingList = intent.getParcelableArrayListExtra("shoppinglist");

        if (shoppingList == null) {
            shoppingList = DaoUtils.queryDailyShopItems(query_day, query_month, 2017, query_shop);
            draftModus = false;
        }
        shoppingListViewAdapter = new ShoppingListViewAdapter(this,0);
        shoppingListViewAdapter.addAll(shoppingList);
        shoppingListView.setAdapter(shoppingListViewAdapter);

    }


    public class ShoppingListViewAdapter extends ArrayAdapter<ContentValues> {

        public ShoppingListViewAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.shop_list_item, null);
            TextView article = (TextView) view.findViewById(R.id.article);
            TextView count = (TextView) view.findViewById(R.id.count);
            TextView sum = (TextView) view.findViewById(R.id.sum);
            final ContentValues entry = getItem(position);
            article.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_ARTICLE));
            sum.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_SUM));
            count.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_COUNT));

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.edit_item);

                    final EditText article = (EditText) dialog.findViewById(R.id.article);
                    article.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_ARTICLE));

                    final EditText count = (EditText) dialog.findViewById(R.id.count);
                    count.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_COUNT));

                    final EditText sum = (EditText) dialog.findViewById(R.id.sum);
                    sum.setText(entry.getAsString(DAO.ItemEntry.COLUMN_NAME_SUM));

                    Button ok = (Button) dialog.findViewById(R.id.ok);
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String id = entry.getAsString(DAO.ItemEntry._ID);

                            entry.put(DAO.ItemEntry.COLUMN_NAME_ARTICLE, String.valueOf(article.getText()));
                            entry.put(DAO.ItemEntry.COLUMN_NAME_SUM, String.valueOf(sum.getText()));
                            entry.put(DAO.ItemEntry.COLUMN_NAME_COUNT, String.valueOf(count.getText()));

                            if (draftModus == false) {
                                DaoUtils.updateExpenseItem(id, entry);
                            }
                            updated = true;
                            shoppingListViewAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialog.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return true;
                }
            });

            return view;
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
