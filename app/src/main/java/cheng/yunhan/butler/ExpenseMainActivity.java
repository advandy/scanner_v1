package cheng.yunhan.butler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cheng.yunhan.butler.customview.BottomImageView;
import cheng.yunhan.butler.customview.ChooseArea;


public class ExpenseMainActivity extends AppCompatActivity implements ExpenseTimelineFragment.OnFragmentInteractionListener, ExpenseBooksFragment.OnBookChosenListener {

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
        /*super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);


        final BottomImageView bottomImageView = (BottomImageView)findViewById(R.id.layout2_bottomView);
        bottomImageView.setDrawingCacheEnabled(true);
        ImageView zoomArea = (ImageView)findViewById(R.id.layout2_imageAbove);
        final ChooseArea chooseArea = (ChooseArea)findViewById(R.id.layout2_topView);

        bottomImageView.setZoomView(zoomArea);
        chooseArea.setBottomView(bottomImageView);

        int width = 500;
        int height = 500;

        chooseArea.setRegion(new Point(100, 100), new Point(width - 100, 100), new Point(width - 100, height - 100), new Point(100, height - 100));

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap1= bottomImageView.getDrawingCache();
                Bitmap bitmap2= bottomImageView.getDrawingCache();

                Point[] points = chooseArea.getPoints();

                int width = bottomImageView.getWidth();

                int height = bottomImageView.getHeight();

                Bitmap resultingImage=Bitmap.createBitmap(width, height, bitmap1.getConfig());

                Canvas canvas = new Canvas(resultingImage);

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                Path path=new Path();
                path.moveTo(points[0].x, points[0].y);
                path.lineTo(points[1].x, points[1].y);
                path.lineTo(points[2].x, points[2].y);
                path.lineTo(points[3].x, points[3].y);
                path.lineTo(points[0].x, points[0].y);

                canvas.drawPath(path, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap2, 0, 0, paint);

                bottomImageView.setImageBitmap(resultingImage);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try {
                    File image = File.createTempFile(
                            imageFileName,  *//* prefix *//*
                            ".jpg",         *//* suffix *//*
                            storageDir      *//* directory *//*
                    );

                    OutputStream outStream = new FileOutputStream(image);
                    resultingImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_main);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
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

    @Override
    public void onFragmentChange(Integer section) {
        mViewPager.setCurrentItem(section);
    }

    @Override
    public void onBookChosen(String book) {
        ExpenseTimelineFragment fragment = (ExpenseTimelineFragment)mSectionsPagerAdapter.getItem(1);
        //fragment.refresh(book);
        mViewPager.setCurrentItem(1);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private ExpenseBooksFragment books;
        private ExpenseBooksFragment books2;
        private ExpenseTimelineFragment timelineFragment;

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    if (books == null) {
                        books = new ExpenseBooksFragment();
                    }
                    return books;
                case 1:
                    if (timelineFragment == null) {
                        timelineFragment = ExpenseTimelineFragment.newInstance();
                    }
                    return timelineFragment ;
                case 2:
                    if (books2 == null) {
                        books2 = new ExpenseBooksFragment();
                    }
                    return books2 ;
            }
            return null;
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
