package cheng.yunhan.scanner_v1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;

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
    private DailyRecordAdapter dailyRecordAdapter;
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

                ArrayList<ExpenseItem> lists = new ArrayList<ExpenseItem>(Arrays.asList(expenseItem1, expenseItem2, expenseItem3, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2, expenseItem2));

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        dailyRecordAdapter.clear();
    }

    public class ExpenseItem {
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

    public class DailyRecordAdapter extends ArrayAdapter<DailyRecord> {
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

        class ViewHolder {
            TextView dateTV;
            TextView dailySumTv;
            GridView dailyItemsGV;
        }
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            DailyRecord item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_record, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.dateTV = (TextView) convertView.findViewById(R.id.date);
                viewHolder.dailySumTv = (TextView) convertView.findViewById(R.id.dailySum);
                viewHolder.dailyItemsGV = (GridView) convertView.findViewById(R.id.dailyItems);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.dateTV.setText(item.date);

            viewHolder.dailySumTv.setText(item.sum + "");
            ViewGroup.LayoutParams params = viewHolder.dailyItemsGV.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 86 * item.expenseItems.size(), mContext.getResources().getDisplayMetrics());

            viewHolder.dailyItemsGV.setLayoutParams(params);

            DailyItemAdapter dailyItemAdapter = new DailyItemAdapter(mContext, item.expenseItems);

            viewHolder.dailyItemsGV .setAdapter(dailyItemAdapter);

            return convertView;
        }
    }

    public class DailyItemAdapter extends ArrayAdapter<ExpenseItem> {
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

        class ViewHolder {
            TextView incomeItemTV;
            TextView expenseItemTV;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ExpenseItem item = getItem(position);
            ViewHolder viewHolder;
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
            }
            return convertView;
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
