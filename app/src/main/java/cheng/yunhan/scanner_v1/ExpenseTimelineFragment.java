package cheng.yunhan.scanner_v1;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import static android.app.Activity.RESULT_CANCELED;
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
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_SCAN = 3;

    private File imageFile;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISPLAY_DAILY_EXPENSE && resultCode == RESULT_OK) {
            Boolean isUpdated = data.getBooleanExtra("updated", false);
            if (isUpdated) {
                new QueryMonthlyRecords().execute();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            CropImage.activity(Uri.fromFile(imageFile))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(mainActivity);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            imageFile.delete();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Intent toDetailIntent = new Intent(getContext(), OcrDetailActivity.class);
                toDetailIntent.putExtra("imagePath", result.getUri().getPath());
                startActivity(toDetailIntent);

                //MainActivity.ImageDocument imageDocument = new MainActivity.ImageDocument(null, null, null);
                //imageDocument.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Bitmap image = decodeSampledBitmapFromFile(result.getUri().getPath(),1000, 1000);
                //imageDocument.imagePath = result.getUri().getPath();
                OutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(imageFile);
                    image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_CANCELED) {
                imageFile.delete();
            }
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

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.addExpense);
        fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new AddRecord().execute(new ExpenseItem("09-03", 22, "Tennis"));
                //Intent intent = new Intent(getContext(), MainActivity.class);
                //startActivity(intent);
            /*    DAOUtils.addExpenseItem("REWE", "Coca Cola", "Drink", 1.99, 19, 3, 2017);
                DAOUtils.addExpenseItem("REWE", "Coca Cola 1", "Drink", 1.99, 19, 3, 2017);

                DAOUtils.addExpenseItem("LIDL", "Coca Cola 1", "Drink", 1.99, 19, 3, 2017);
                DAOUtils.addExpenseItem("LIDL", "Coca Cola 1", "Drink", 1.99, 19, 3, 2017);

                DAOUtils.addIncomeItem("Salary", 1000.00, 1,3,2017);

                DAOUtils.addExpenseItem("REWE", "Coca Cola 2", "Drink", 1.99, 18, 3, 2017);
                DAOUtils.addExpenseItem("REWE", "Coca Cola 3", "Drink", 1.99,  18, 3, 2017);
                DAOUtils.addExpenseItem("REWE", "Coca Cola 4", "Drink", 1.99, 18, 3, 2017);

                new QueryMonthlyRecords().execute("Book");*/

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.input_modes_popup);
                //dialog.setTitle("OCR Result");
                //((TextView)dialog.findViewById(R.id.ocrTextView)).setText(text);
                dialog.show();

                final Button takePhoto = (Button)dialog.findViewById(R.id.scan_input);
                takePhoto.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent intent = new Intent(getContext(), takePhotoActivity.class);
                        startActivityForResult(intent, REQUEST_SCAN);
                    }
                });
            }
        });

        new QueryMonthlyRecords().execute("Book");


        return rootView;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }
    private void takePhoto() {
        File file = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            //file = new File(Environment.getExternalStorageDirectory()+File.separator + "temp_image_scanner.jpg");
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                if (Build.VERSION.SDK_INT >= 24) {
                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                            "cheng.yunhan.scanner_v1.provider",
                            file);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                }
                imageFile = file;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private class QueryMonthlyRecords extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            timelineDataSet.clear();

            monthlyIncome = DAOUtils.queryMonthlyIncomeSum(3, 2017);
            monthlyExpense = DAOUtils.queryMonthlyExpenseSum(3, 2017);

            ArrayList<ContentValues> values = DAOUtils.queryItemsByMonth(3, 2017);

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
/*                this.icon.setOnClickListener(new View.OnClickListener() {
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
                });*/
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
                dateViewHolder.date.setText(((DateItem)item).date);
                dateViewHolder.dateSum.setText(((DateItem)item).sum + "");
            } else if (item instanceof ExpenseItem) {
                ExpenseViewHolder expenseViewHolder = (ExpenseViewHolder)holder;
                expenseViewHolder.expense.setText(((ExpenseItem) item).contentValues.getAsString(DAO.ItemEntry.COLUMN_NAME_SHOP) + " " + (((ExpenseItem) item).contentValues.getAsString(DAO.ItemEntry.COLUMN_NAME_SUM)));
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

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
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
