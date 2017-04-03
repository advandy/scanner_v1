package cheng.yunhan.butler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cheng.yunhan.butler.customview.BottomImageView;
import cheng.yunhan.butler.customview.ChooseArea;

public class CropImageActivity extends AppCompatActivity {

    private String imagePath;
    private Bitmap image;
    private BottomImageView bottomImageView;
    private ChooseArea chooseArea;
    private int originHeight;
    private int originWidth;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imageUri");

        image = BitmapFactory.decodeFile(imagePath);

        originWidth = image.getWidth();
        originHeight = image.getHeight();


        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.crop_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        bottomImageView = (BottomImageView)findViewById(R.id.layout2_bottomView);
        bottomImageView.setDrawingCacheEnabled(true);
        bottomImageView.setImageBitmap(image);
        ImageView zoomArea = (ImageView)findViewById(R.id.layout2_imageAbove);
        zoomArea.setImageBitmap(image);
        zoomArea.buildDrawingCache();

        chooseArea = (ChooseArea)findViewById(R.id.layout2_topView);

        bottomImageView.setZoomView(zoomArea);
        chooseArea.setBottomView(bottomImageView);

        int width = 500;
        int height = 500;

        chooseArea.setRegion(new Point(100, 100), new Point(width - 100, 100), new Point(width - 100, height - 100), new Point(100, height - 100));

        pb = (ProgressBar) findViewById(R.id.cropProgress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crop_image, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.all:
                int height = bottomImageView.getHeight();
                int width = bottomImageView.getWidth();
                chooseArea.setRegion(new Point(0, 0), new Point(width, 0), new Point(width, height), new Point(0, height));
                chooseArea.invalidate();
                break;
            case R.id.crop:
                pb.setVisibility(View.VISIBLE);
                int imageViewHeight = bottomImageView.getHeight();

                int imageViewWidth = bottomImageView.getWidth();

                Bitmap bitmap1= image;

                Point[] points = chooseArea.getPoints();

                width = bitmap1.getWidth();

                height = bitmap1.getHeight();

                Float yFactor = (float)originHeight/(float)imageViewHeight;
                Float xFactor = (float)originWidth/(float)imageViewWidth;

                Bitmap resultingImage = Bitmap.createBitmap(width, height, bitmap1.getConfig());

                Canvas canvas = new Canvas(resultingImage);

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                Path path = new Path();
                path.moveTo(points[0].x*xFactor , points[0].y*yFactor);
                path.lineTo(points[1].x*xFactor, points[1].y*yFactor);
                path.lineTo(points[2].x*xFactor, points[2].y*yFactor);
                path.lineTo(points[3].x*xFactor, points[3].y*yFactor);
                path.lineTo(points[0].x*xFactor, points[0].y*yFactor);

                canvas.drawPath(path, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap1, 0, 0, paint);

                resultingImage = getRectImage(points, xFactor, yFactor, resultingImage);
                try {
                    File image = new File(imagePath);
                    OutputStream outStream = new FileOutputStream(image);
                    resultingImage.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
                    outStream.flush();
                    outStream.close();
                    pb.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(CropImageActivity.this, OcrDetailActivity.class);
                    intent.putExtra("imagePath", imagePath);
                    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        return true;
    }

    private boolean noReset = false;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (noReset == true) {
            return;
        }
        //super.onWindowFocusChanged(hasFocus);
        int height = bottomImageView.getHeight();
        int width = bottomImageView.getWidth();
        chooseArea.setRegion(new Point(0, 0), new Point(width, 0), new Point(width, height), new Point(0, height));
        chooseArea.invalidate();
        noReset = true;
    }

    private Bitmap getRectImage(Point[] points, Float xFactor, Float yFactor, Bitmap image) {


        int x = points[0].x < points[3].x ? points[0].x : points[3].x;
        int y = points[0].y < points[1].y ? points[0].y :  points[1].y;

        int leftx = x;
        int rightx = points[1].x > points[2].x ? points[1].x : points[2].x;

        int topy = y;
        int bottomy = points[2].y > points[3].y ? points[2].y : points[3].y;

        int xAxis = Math.round(x*xFactor);
        int yAxis = Math.round(y*yFactor);
        int picWidth = Math.round(rightx*xFactor - leftx*xFactor);
        int picHeight = Math.round(bottomy*yFactor- topy*yFactor);

        return Bitmap.createBitmap(image, xAxis, yAxis, picWidth, picHeight);
    }
}
