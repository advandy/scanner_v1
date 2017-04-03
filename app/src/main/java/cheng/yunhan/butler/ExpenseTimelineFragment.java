package cheng.yunhan.butler;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


import static android.app.Activity.RESULT_OK;

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
    static final int DISPLAY_DAILY_EXPENSE = 1;
    static final int REQUEST_SCAN = 3;

    private int month;
    private int year;

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
     * @return A new instance of fragment ExpenseTimelineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpenseTimelineFragment newInstance() {
        ExpenseTimelineFragment fragment = new ExpenseTimelineFragment();

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISPLAY_DAILY_EXPENSE && resultCode == RESULT_OK) {
            Boolean isUpdated = data.getBooleanExtra("updated", false);
            if (isUpdated) {
                new QueryMonthlyRecords().execute();
            }
        }
        
        if (requestCode == REQUEST_SCAN) {
            // TODO: 24.03.2017
        }
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

        final View inputModeView = (View)rootView.findViewById(R.id.input_mode);

        ImageButton manual = (ImageButton) inputModeView.findViewById(R.id.manuel_input);
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputModeView.setVisibility(View.GONE);
                final Dialog manualDialog = new Dialog(getContext());
                manualDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                manualDialog.setContentView(R.layout.manual_add_item);

                final EditText article = (EditText) manualDialog.findViewById(R.id.article);

                final EditText count = (EditText) manualDialog.findViewById(R.id.count);

                final EditText sum = (EditText) manualDialog.findViewById(R.id.sum);

                final Spinner spinner = (Spinner) manualDialog.findViewById(R.id.shopSpinner);
                Button cancel = (Button)manualDialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        manualDialog.dismiss();
                    }
                });

                Button ok = (Button)manualDialog.findViewById(R.id.ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean error = false;

                        if (Utils.checkEditText(article) == true) {
                            error = true;
                        }

                        if (Utils.checkEditText(sum) == true) {
                            error = true;
                        }

                        if (Utils.checkEditText(count) == true) {
                            error = true;
                        }

                        if (error) {
                            return;
                        }

                        manualDialog.dismiss();

                        String articleStr = String.valueOf(article.getText());
                        Double sumValue = Double.valueOf(String.valueOf(sum.getText()));
                        Integer countValue = Integer.valueOf(String.valueOf(count.getText()));
                        String shop = (String) spinner.getSelectedItem();
                        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        int year = Calendar.getInstance().get(Calendar.YEAR);
                        DAOUtils.addExpenseItem(shop, articleStr, "", countValue, sumValue, day, month, year);
                        Intent intent = new Intent(getContext(), DailyExpenseList.class);
                        intent.putExtra("day", day);
                        intent.putExtra("month", month);
                        intent.putExtra("shop", shop);
                        startActivityForResult(intent, DISPLAY_DAILY_EXPENSE);
                        new QueryMonthlyRecords().execute("Book");
                    }
                });


                manualDialog.show();
            }
        });

        final ImageButton takePhoto = (ImageButton) inputModeView.findViewById(R.id.scan_input);
        takePhoto.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputModeView.setVisibility(View.GONE);
                final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                if (!sharedPref.getBoolean("hideTutorialDialog", false)) {
                    final Dialog tutorialDialog = new Dialog(getContext(), android.R.style.Theme_Light_NoTitleBar_Fullscreen);
                    tutorialDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    tutorialDialog.setContentView(R.layout.take_photo_tutorial);

                    ViewPager viewPager = (ViewPager) tutorialDialog.findViewById(R.id.tutorialViewpager);

                    viewPager.setAdapter(new CustomViewPagerAdapter());
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                        View[] dots = {
                                tutorialDialog.findViewById(R.id.dot1),
                                tutorialDialog.findViewById(R.id.dot2),
                                tutorialDialog.findViewById(R.id.dot3),
                                tutorialDialog.findViewById(R.id.dot4),
                                tutorialDialog.findViewById(R.id.dot5)
                        };
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            for (View dot:dots) {
                                ((ImageView)dot).setImageResource(R.drawable.dot);
                            }
                            ((ImageView)dots[position]).setImageResource(R.drawable.greydot);
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });


                    final CheckBox checkBox = (CheckBox) tutorialDialog.findViewById(R.id.noShow);
                    Button camera = (Button)tutorialDialog.findViewById(R.id.launchCamera);
                    camera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("hideTutorialDialog", checkBox.isChecked());
                            editor.commit();
                            tutorialDialog.dismiss();
                            Intent intent = new Intent(getContext(), takePhotoActivity.class);
                            startActivity(intent);
                        }
                    });
                    tutorialDialog.show();
                } else {
                    Intent intent = new Intent(getContext(), takePhotoActivity.class);
                    startActivity(intent);
                }
            }
        });

        ImageButton tile = (ImageButton) rootView.findViewById(R.id.tile);
        tile.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentChange(0);
            }
        });

        ImageButton report = (ImageButton) rootView.findViewById(R.id.report);
        report.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentChange(2);
            }
        });

        ImageButton fab = (ImageButton)rootView.findViewById(R.id.addExpense);
        fab.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (inputModeView.getVisibility() == View.VISIBLE) {
                inputModeView.setVisibility(View.GONE);
            } else {
                inputModeView.setVisibility(View.VISIBLE);
            }
            }
        });

        Calendar calendar = Calendar.getInstance();
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);

        new QueryMonthlyRecords().execute("Book");


        return rootView;
    }

    private class CustomViewPagerAdapter extends PagerAdapter {
        int[] mResources = {
            R.drawable.correct,
            R.drawable.wrong1,
            R.drawable.wrong2,
            R.drawable.wrong3,
            R.drawable.correct
        };

        @Override
        public int getCount() {
            return mResources.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(mResources[position]);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView)object);
        }
    }


    private class QueryMonthlyRecords extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            timelineDataSet.clear();

            monthlyIncome = DAOUtils.queryMonthlyIncomeSum(month, year);
            monthlyExpense = DAOUtils.queryMonthlyExpenseSum(month, year);

            ArrayList<ContentValues> values = DAOUtils.queryItemsByMonth(month, year);

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
        Date key = dateFormat.parse(record.contentValues.getAsString("date"));
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
                    timeLineItems.add(new IncomeItem(value));
                } else {
                    sum = sum + value.getAsDouble("sum");
                    timeLineItems.add(new ExpenseItem(value));
                }
            }

            mTimelineAdapter.items.add(new DateItem(dateKey, sum));
            mTimelineAdapter.items.addAll(timeLineItems);
        }

        mTimelineAdapter.notifyDataSetChanged();
    }


    public class TimeLineItem {
        public ContentValues contentValues;
    }

    public class DateItem extends  TimeLineItem {
        public String date;
        public double sum;
        public DateItem(String date, double sum) {
            this.date = date;
            this.sum = sum;
        }
    }

    public class ExpenseItem extends TimeLineItem {
        public ContentValues contentValues;

        public ExpenseItem(ContentValues contentValues) {
            this.contentValues = contentValues;
        }
    }


    public class IncomeItem extends TimeLineItem {
        public ContentValues contentValues;

        public IncomeItem(ContentValues contentValues) {
            this.contentValues = contentValues;
        }
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
            public ImageButton icon;
            public boolean isEditMode = false;
            public ViewHolder(View rootView, ImageButton icon) {
                super(rootView);
                this.rootView = rootView;
                this.icon = icon;
            }

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

        private Map<String, Integer> map = Utils.getIcons();

        private int getIconImage(String shop) {
            Integer icon = map.get(shop);
            if (icon != null) {
                return icon;
            }

            return R.drawable.savepig;
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
                dateViewHolder.date.setText(((DateItem)item).date);
                dateViewHolder.dateSum.setText(new DecimalFormat("#0.00").format(((DateItem)item).sum));
            } else if (item instanceof ExpenseItem) {
                ExpenseViewHolder expenseViewHolder = (ExpenseViewHolder)holder;
                String shop = ((ExpenseItem) item).contentValues.getAsString(DAO.ItemEntry.COLUMN_NAME_SHOP);
                String sum = (((ExpenseItem) item).contentValues.getAsString(DAO.ItemEntry.COLUMN_NAME_SUM));
                expenseViewHolder.expense.setText( shop + " " + sum);

                expenseViewHolder.icon.setBackgroundResource(getIconImage(shop));
                expenseViewHolder.icon.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer day = ((ExpenseItem) item).contentValues.getAsInteger("day");
                        Integer month = ((ExpenseItem) item).contentValues.getAsInteger("month");
                        String shop = ((ExpenseItem) item).contentValues.getAsString("shop");
                        Intent intent = new Intent(getContext(), DailyExpenseList.class);
                        intent.putExtra("day", day);
                        intent.putExtra("month", month);
                        intent.putExtra("shop", shop);
                        startActivityForResult(intent, DISPLAY_DAILY_EXPENSE);

                    }
                });
            } else if (item instanceof IncomeItem) {
                IncomeViewHolder incomeViewHolder = (IncomeViewHolder)holder;

                incomeViewHolder.income.setText(((IncomeItem)item).contentValues.getAsString(DAO.ItemEntry.COLUMN_NAME_INCOMECATEGORY) + " " + (((IncomeItem)item).contentValues.getAsString(DAO.ItemEntry.COLUMN_NAME_INCOMESUM)));
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
        void onFragmentChange(Integer section);
    }
}
