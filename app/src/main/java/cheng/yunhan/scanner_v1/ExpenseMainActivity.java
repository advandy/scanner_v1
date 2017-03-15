package cheng.yunhan.scanner_v1;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpenseMainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(1);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expense_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private DailyRecordAdapter dailyRecordAdapter;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_expense_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            final GridView timelineGrid = (GridView) rootView.findViewById(R.id.timelineGrid);
            dailyRecordAdapter = new DailyRecordAdapter(getContext(), new ArrayList<DailyRecord>());

            FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.addExpense);
            fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                    ExpenseItem expenseItem1 = new ExpenseItem("sd", 30, "");
                    ExpenseItem expenseItem2 = new ExpenseItem("fdf", 20, "");
                    expenseItem2.isIncome = true;
                    ExpenseItem expenseItem3 = new ExpenseItem("df", 12, "");

                    ArrayList<ExpenseItem> lists = new ArrayList<ExpenseItem>(Arrays.asList(expenseItem1, expenseItem2, expenseItem3));

                    DailyRecord dailyRecord = new DailyRecord("15.03", 62, lists);
                    dailyRecordAdapter.add(dailyRecord);

                }
            });

            ExpenseItem expenseItem1 = new ExpenseItem("Sport", 30, "");
            ExpenseItem expenseItem2 = new ExpenseItem("Food", 20, "");
            ExpenseItem expenseItem3 = new ExpenseItem("Traffic", 12, "");

            ArrayList<ExpenseItem> lists = new ArrayList<ExpenseItem>(Arrays.asList(expenseItem1, expenseItem2, expenseItem3));

            DailyRecord dailyRecord = new DailyRecord("15.03", 62, lists);
            dailyRecordAdapter.add(dailyRecord);

            timelineGrid.setAdapter(dailyRecordAdapter);
            return rootView;
        }
    }

    public static class DailyRecord {
        public String date;
        public double sum;
        public ArrayList<ExpenseItem> expenseItems;

        public DailyRecord(String date, double sum, ArrayList<ExpenseItem> expenseItems) {
            this.date = date;
            this.sum = sum;
            this.expenseItems = expenseItems;
        }
    }

    public static class ExpenseItem {
        public String category;
        public double sum;
        public String remark;
        public boolean isIncome;

        public ExpenseItem(String category, double sum, String remark) {
            this.category = category;
            this.sum = sum;
            this.remark = remark;
        }

    }

    public static class DailyRecordAdapter extends ArrayAdapter<DailyRecord> {
        private Context mContext;
        private ArrayList<DailyRecord> records;

        public DailyRecordAdapter(Context context, ArrayList<DailyRecord> records) {
            super(context, 0, records);
            this.mContext = context;
            this.records = records;
        }

        public DailyRecord getItem(int position) {
            return records.get(position);
        }


        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;

            View dailyRecordView = LayoutInflater.from(mContext).inflate(R.layout.daily_record, parent, false);
            tv = (TextView) dailyRecordView.findViewById(R.id.date);
            tv.setText(getItem(position).date);

            tv = (TextView) dailyRecordView.findViewById(R.id.dailySum);
            tv.setText(getItem(position).sum + "");

            GridView dailyItems = (GridView) dailyRecordView.findViewById(R.id.dailyItems);
            ViewGroup.LayoutParams params = dailyItems.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 94 * getItem(position).expenseItems.size(), mContext.getResources().getDisplayMetrics());

            dailyItems.setLayoutParams(params);

            DailyItemAdapter dailyItemAdapter = new DailyItemAdapter(mContext, getItem(position).expenseItems);

            dailyItems.setAdapter(dailyItemAdapter);

            return dailyRecordView;
        }
    }

    public static class DailyItemAdapter extends ArrayAdapter<ExpenseItem> {
        private ArrayList<ExpenseItem> items;
        private Context mContext;

        public DailyItemAdapter(Context c, ArrayList<ExpenseItem> items) {
            super(c, 0, items);
            this.mContext = c;
            this.items = items;
        }

        public int getCount() {
            return items.size();
        }

        public ExpenseItem getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.expense_item, parent, false);
            TextView tv;
            if (getItem(position).isIncome) {
                tv = (TextView)itemView.findViewById(R.id.incomeItem);
            } else {
                tv = (TextView)itemView.findViewById(R.id.expenseItem);
            }
            tv.setText(getItem(position).category + ": " + getItem(position).sum);
            return itemView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
