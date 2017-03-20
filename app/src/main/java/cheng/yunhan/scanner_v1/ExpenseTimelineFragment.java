package cheng.yunhan.scanner_v1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

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
    private DAO DAOUtils;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView timelineRV;
    private TimelineAdapter mTimelineAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    private TreeMap<Date, ArrayList<TimeLineItem>> monthlyRecordsCollection = new TreeMap<>(new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            return o2.compareTo(o1);
        }
    });

    private TreeMap<Integer, ArrayList<ContentValues>> timelineDataSet = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
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
        DAOUtils = DAO.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_expense_main, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        timelineRV = (RecyclerView) rootView.findViewById(R.id.timelineRV);
        timelineRV.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        timelineRV.setLayoutManager(mLayoutManager);
        mTimelineAdapter = new TimelineAdapter(new ArrayList<TimeLineItem>());
        timelineRV.setAdapter(mTimelineAdapter);

        monthlyExpenseTv = (TextView) rootView.findViewById(R.id.monthlyExpenseSum);
        monthlyIncomeTv = (TextView) rootView.findViewById(R.id.monthlyIncomeSum);

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.addExpense);
        fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new AddRecord().execute(new ExpenseItem("09-03", 22, "Tennis"));
                //Intent intent = new Intent(getContext(), MainActivity.class);
                //startActivity(intent);
                DAOUtils.addExpenseItem("REWE", "Coca Cola", "Drink", 1.99, 19, 3, 2017);
                DAOUtils.addExpenseItem("REWE", "Coca Cola 1", "Drink", 1.99, 19, 3, 2017);

                DAOUtils.addExpenseItem("LIDL", "Coca Cola 1", "Drink", 1.99, 19, 3, 2017);
                DAOUtils.addExpenseItem("LIDL", "Coca Cola 1", "Drink", 1.99, 19, 3, 2017);

                DAOUtils.addIncomeItem("Salary", 1000.00, 1,3,2017);

                DAOUtils.addExpenseItem("REWE", "Coca Cola 2", "Drink", 1.99, 18, 3, 2017);
                DAOUtils.addExpenseItem("REWE", "Coca Cola 3", "Drink", 1.99,  18, 3, 2017);
                DAOUtils.addExpenseItem("REWE", "Coca Cola 4", "Drink", 1.99, 18, 3, 2017);

                new QueryMonthlyRecords().execute("Book");
            }
        });

        new QueryMonthlyRecords().execute("Book");


        return rootView;
    }

    private class QueryMonthlyRecords extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            timelineDataSet.clear();

            monthlyIncome = DAOUtils.queryMonthlyIncomeSum(3, 2017);
            monthlyExpense = DAOUtils.queryMonthlyExpenseSum(3, 2017);

            ArrayList<ContentValues> values = DAOUtils.queryItemsByMonth(3,2017);
            for (ContentValues obj : values) {
                Integer day = obj.getAsInteger("day");
                if (timelineDataSet.get(day) == null) {
                    timelineDataSet.put(day, new ArrayList<ContentValues>(Arrays.asList(obj)));
                } else {
                    timelineDataSet.get(day).add(obj);
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateView();

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
            //updateView(monthlyRecordsCollection);
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

    private void updateView() {

        monthlyIncomeTv.setText(new DecimalFormat("#.##").format(monthlyIncome));
        monthlyExpenseTv.setText(new DecimalFormat("#.##").format(monthlyExpense));

        mTimelineAdapter.items.clear();
        ArrayList<TimeLineItem> timeLineItems;
        for (Map.Entry<Integer, ArrayList<ContentValues>> entry: timelineDataSet.entrySet()) {
            String dateKey = "";
            Double sum = 0.00;
            timeLineItems = new ArrayList<>();
            ArrayList<ContentValues> values = entry.getValue();
            for (ContentValues value : values) {
                dateKey = value.getAsString("day")+ "-" +value.getAsString("month");
                if (value.getAsDouble("income_sum") != null) {
                    timeLineItems.add(new IncomeItem(dateKey, value.getAsDouble("income_sum"), value.getAsString("income_category")));
                } else {
                    sum = sum + value.getAsDouble("sum");
                    timeLineItems.add(new ExpenseItem(dateKey, value.getAsDouble("sum"), value.getAsString("shop")));
                }
            }

            mTimelineAdapter.items.add(new DateItem(dateKey, sum, UUID.randomUUID()));
            mTimelineAdapter.items.addAll(timeLineItems);
        }

        mTimelineAdapter.notifyDataSetChanged();
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

        public DateItem(String date, double sum, UUID id) {
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
    public interface onIconClicked {
        public void enableEdit();
    }

    public void deleteItem(TimeLineItem item) {

    }
    public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

        public ArrayList<TimeLineItem> items;

        @Override
        public int getItemViewType(int position) {
            TimeLineItem item = items.get(position);

            if (item instanceof DateItem ) {
                return 0;
            } else if (item instanceof  IncomeItem) {
                return 1;
            } else if (item instanceof  ExpenseItem) {
                return 2;
            }

            return 0;
        }

        public TimelineAdapter(ArrayList<TimeLineItem> items) {
            this.items = items;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View rootView;
            public boolean isEditMode = false;
            public ViewHolder(View rootView) {
                super(rootView);
                this.rootView = rootView;
            }
        }

        public class DateViewHolder extends TimelineAdapter.ViewHolder {
            public TextView date;
            public TextView dateSum;

            public DateViewHolder(View rootView, TextView date, TextView dateSum) {
                super(rootView);
                this.date = date;
                this.dateSum = dateSum;
            }
        }

        public class ExpenseViewHolder extends TimelineAdapter.ViewHolder {
            public TextView expense;
            public ImageButton icon;
            public ImageButton edit;
            public ImageButton delete;
            public ExpenseViewHolder(View rootView, final TextView expense, ImageButton icon, final ImageButton edit, final ImageButton delete) {
                super(rootView);
                this.expense = expense;
                this.icon = icon;
                this.edit = edit;
                this.delete = delete;
                this.icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isEditMode) {
                            isEditMode = false;
                            expense.setVisibility(View.VISIBLE);
                            delete.setVisibility(View.INVISIBLE);
                            edit.setVisibility(View.INVISIBLE);
                        } else {
                            isEditMode = true;
                            expense.setVisibility(View.INVISIBLE);
                            delete.setVisibility(View.VISIBLE);
                            edit.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        public class IncomeViewHolder extends TimelineAdapter.ViewHolder {
            public TextView income;
            public ImageButton icon;
            public IncomeViewHolder(View rootView, TextView income, ImageButton icon) {
                super(rootView);
                this.income = income;
                this.icon = icon;
            }
        }


        @Override
        public TimelineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case 0:
                    View dateRootView = layoutInflater.inflate(R.layout.daily_record, parent, false);
                    TextView dateSumTexView = (TextView) dateRootView.findViewById(R.id.dailySum);
                    TextView dateTextView = (TextView) dateRootView.findViewById(R.id.date);
                    return new DateViewHolder(dateRootView, dateTextView, dateSumTexView);
                case 1:
                    View dailyRootView = layoutInflater.inflate(R.layout.expense_item, parent, false);
                    TextView dailyIncomTextView = (TextView) dailyRootView.findViewById(R.id.incomeItem);
                    ImageButton icon = (ImageButton) dailyRootView.findViewById(R.id.categoryIcon);
                    return new IncomeViewHolder(dailyRootView, dailyIncomTextView, icon);
                case 2:
                    View dailyExpenseRootView = layoutInflater.inflate(R.layout.expense_item, parent, false);
                    TextView dailyExpense = (TextView) dailyExpenseRootView.findViewById(R.id.expenseItem);
                    ImageButton icon2 = (ImageButton) dailyExpenseRootView.findViewById(R.id.categoryIcon);
                    ImageButton edit = (ImageButton) dailyExpenseRootView.findViewById(R.id.edit);
                    ImageButton delete = (ImageButton) dailyExpenseRootView.findViewById(R.id.delete);
                    return new ExpenseViewHolder(dailyExpenseRootView, dailyExpense, icon2, edit, delete);
            }

            return null;
        }



        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final TimeLineItem item = items.get(position);

            if (item instanceof DateItem ) {
                DateViewHolder dateViewHolder = (DateViewHolder) holder;
                dateViewHolder.date.setText(item.date);
                dateViewHolder.dateSum.setText(item.sum + "");
            } else if (item instanceof ExpenseItem) {
                ExpenseViewHolder expenseViewHolder = (ExpenseViewHolder)holder;
                expenseViewHolder.expense.setText(((ExpenseItem) item).category + ": " + item.sum);
                expenseViewHolder.delete.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteItem(item);
                    }
                });
            } else if (item instanceof IncomeItem) {
                IncomeViewHolder incomeViewHolder = (IncomeViewHolder)holder;
                incomeViewHolder.income.setText(((IncomeItem) item).category + ": " + item.sum);
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
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
