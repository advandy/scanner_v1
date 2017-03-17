package cheng.yunhan.scanner_v1;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExpenseTimelineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExpenseTimelineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseTimelineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private DailyItemAdapter dailyItemAdapter;
    private GridView timelineGrid;
    private TreeMap<Date, ArrayList<TimeLineItem>> monthlyRecordsCollection = new TreeMap<>(new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            return o2.compareTo(o1);
        }
    });
    private Double monthlyIncome = 0.00;
    private TextView monthlyIncomeTv;
    private Double monthlyExpense = 0.00;
    private TextView monthlyExpenseTv;
    public String book;

    private ExpenseMainActivity mainActivity;

    private OnFragmentInteractionListener mListener;

    public ExpenseTimelineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExpenseTimelineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpenseTimelineFragment newInstance(String param1, String param2) {
        ExpenseTimelineFragment fragment = new ExpenseTimelineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mainActivity = (ExpenseMainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_expense_main, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        timelineGrid = (GridView) rootView.findViewById(R.id.timelineGrid);
        monthlyExpenseTv = (TextView) rootView.findViewById(R.id.expenseItem);
        monthlyIncomeTv = (TextView) rootView.findViewById(R.id.incomeItem);

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.addExpense);
        fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddRecord().execute(new ExpenseItem("09-03", 22, "Tennis"));
            }
        });

        new InitTimeLine().execute("Book");


        return rootView;
    }

    private class InitTimeLine extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            DateFormat dateFormat = new SimpleDateFormat("dd-mm");
            String dateStr = "16-03";
            Date date = null;
            try {
                date = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            dateStr = "12-03";
            Date date1 = new Date();
            try {
                date1 = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            dateStr = "19-03";
            Date date2 = new Date();
            try {
                date2 = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            dailyItemAdapter = new DailyItemAdapter(getContext(), new ArrayList<TimeLineItem>());

            DateItem dateItem = new DateItem("16.03", 100);
            ExpenseItem expenseItem1 = new ExpenseItem("16.03", 20, "shopping");
            ExpenseItem expenseItem2 = new ExpenseItem("16.03", 10, "sport");
            ExpenseItem expenseItem3 = new ExpenseItem("16.03", 2, "traffic");
            IncomeItem incomeItem = new IncomeItem("16.03", 2000, "bonus");

            monthlyRecordsCollection.put(date2, new ArrayList<TimeLineItem>(Arrays.asList(expenseItem1, incomeItem, expenseItem3, expenseItem2)));
            monthlyRecordsCollection.put(date1, new ArrayList<TimeLineItem>(Arrays.asList(expenseItem1, expenseItem1, expenseItem2)));
            monthlyRecordsCollection.put(date, new ArrayList<TimeLineItem>(Arrays.asList(expenseItem1, expenseItem3, expenseItem2, incomeItem)));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateView(monthlyRecordsCollection);

        }
    }


    private class AddRecord extends AsyncTask<TimeLineItem, Void, Void> {

        @Override
        protected Void doInBackground(TimeLineItem... params) {
            try {
                addRecord(params[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateView(monthlyRecordsCollection);
        }
    }



    private void addRecord(TimeLineItem record) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("dd-mm");
        Date key = dateFormat.parse(record.date);
        ArrayList<TimeLineItem> values =  monthlyRecordsCollection.get(key);

        if (values == null) {
            monthlyRecordsCollection.put(key, new ArrayList<TimeLineItem>(Arrays.asList(record)));
        } else {
            values.add(record);
        }
    }

    private void updateView(TreeMap<Date, ArrayList<TimeLineItem>> records) {
        DateFormat dateFormat = new SimpleDateFormat("dd-mm");
        dailyItemAdapter.clear();
        for (Map.Entry<Date, ArrayList<TimeLineItem>> entry: records.entrySet()) {
            Date key = entry.getKey();
            Double sum = 0.00;

            ArrayList<TimeLineItem> values = entry.getValue();
            for (TimeLineItem value : values) {
                sum = sum + value.sum;
            }
            dailyItemAdapter.add(new DateItem(dateFormat.format(key), sum));
            dailyItemAdapter.addAll(values);
        }
        if (timelineGrid.getAdapter() == null) {
            timelineGrid.setAdapter(dailyItemAdapter);
        }
    }


    public class TimeLineItem {
        public String date;
        public double sum;

        public TimeLineItem(String date, double sum) {
            this.date = date;
            this.sum = sum;
        }
    }

    public class DateItem extends  TimeLineItem {

        public DateItem(String date, double sum) {
            super(date, sum);
        }
    }

    public class ExpenseItem extends TimeLineItem {
        public String category;

        public ExpenseItem(String date, double sum, String category) {
            super(date, sum);
            this.category = category;
        }
    }

    public class IncomeItem extends TimeLineItem {
        public String category;

        public IncomeItem(String date, double sum, String category) {
            super(date, sum);
            this.category = category;
        }
    }

    public class DailyRecord {
        public String date;
        public double sum;
        public ArrayList<ExpenseItem> expenseItems;

        public DailyRecord(String date, double sum, ArrayList<ExpenseItem> expenseItems) {
            this.date = date;
            this.sum = sum;
            this.expenseItems = expenseItems;
        }
    }

    public void refresh(String book) {

    }


    public class DailyItemAdapter extends ArrayAdapter<TimeLineItem> {
        private ArrayList<TimeLineItem> items;
        private Context mContext;

        public DailyItemAdapter(Context c, ArrayList<TimeLineItem> items) {
            super(c, 0, items);
            this.mContext = c;
            this.items = items;
        }

        public int getCount() {
            return items.size();
        }

        public TimeLineItem getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        class ViewHolder {
            TextView incomeItemTV;
            TextView expenseItemTV;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            TimeLineItem item = getItem(position);
/*            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.expense_item, parent, false);
                viewHolder.incomeItemTV = (TextView)convertView.findViewById(R.id.incomeItem);
                viewHolder.expenseItemTV = (TextView)convertView.findViewById(R.id.expenseItem);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (item.isIncome) {
                viewHolder.incomeItemTV.setText(item.category + ": " + item.sum);
            } else {
                viewHolder.expenseItemTV.setText(item.category + ": " + item.sum);
            }*/
            View rootView = null;
            if (item instanceof DateItem ) {
                rootView = LayoutInflater.from(mContext).inflate(R.layout.daily_record, parent, false);
                TextView tv = (TextView) rootView.findViewById(R.id.date);
                tv.setText(item.date);
                tv = (TextView)rootView.findViewById(R.id.dailySum);
                tv.setText(item.sum + "");
            } else if (item instanceof  ExpenseItem) {
                rootView = LayoutInflater.from(mContext).inflate(R.layout.expense_item, parent, false);
                TextView tv = (TextView) rootView.findViewById(R.id.expenseItem);
                tv.setText(((ExpenseItem) item).category + ": " + item.sum);
            } else if (item instanceof  IncomeItem) {
                rootView = LayoutInflater.from(mContext).inflate(R.layout.expense_item, parent, false);
                TextView tv = (TextView) rootView.findViewById(R.id.incomeItem);
                tv.setText(((IncomeItem) item).category + ": " + item.sum);
            }
            return rootView;
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
