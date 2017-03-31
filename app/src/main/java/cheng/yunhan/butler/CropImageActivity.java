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
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cheng.yunhan.butler.customview.BottomImageView;
import cheng.yunhan.butler.customview.ChooseArea;

public class CropImageActivity extends AppCompatActivity {

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imageUri");

        final Bitmap image = BitmapFactory.decodeFile(imagePath);

        final int originWidth = image.getWidth();
        final int originHeight = image.getHeight();


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);


        final BottomImageView bottomImageView = (BottomImageView)findViewById(R.id.layout2_bottomView);
        bottomImageView.setDrawingCacheEnabled(true);
        bottomImageView.setImageBitmap(image);
        ImageView zoomArea = (ImageView)findViewById(R.id.layout2_imageAbove);
        zoomArea.setImageBitmap(image);
        zoomArea.buildDrawingCache();

        final ChooseArea chooseArea = (ChooseArea)findViewById(R.id.layout2_topView);

        bottomImageView.setZoomView(zoomArea);
        chooseArea.setBottomView(bottomImageView);

        int width = 500;
        int height = 500;

        chooseArea.setRegion(new Point(100, 100), new Point(width - 100, 100), new Point(width - 100, height - 100), new Point(100, height - 100));

        Button wholePicBtn = (Button) findViewById(R.id.button3);
        wholePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int height = bottomImageView.getHeight();
                int width = bottomImageView.getWidth();
                chooseArea.setRegion(new Point(0, 0), new Point(width, 0), new Point(width, height), new Point(0, height));
                chooseArea.invalidate();
            }
        });

        Button btn = (Button) findViewById(R.id.button);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.cropProgress);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                int imageViewHeight = bottomImageView.getHeight();

                int imageViewWidth = bottomImageView.getWidth();

                Bitmap bitmap1= image;
                //Bitmap bitmap2= bitmap1.copy(bitmap1.getConfig(), false);

                Point[] points = chooseArea.getPoints();

                int width = bitmap1.getWidth();

                int height = bitmap1.getHeight();

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

                //resultingImage = Bitmap.createBitmap(resultingImage, points[0].x*xFactor.intValue(), points[0].y*yFactor.intValue(), points[1].x*xFactor.intValue() - points[0].x*xFactor.intValue(), points[3].y*yFactor.intValue() - points[0].y*yFactor.intValue() );
                resultingImage = getRectImage(points, xFactor, yFactor, resultingImage);
                try {
                    File image = new File(imagePath);
                    OutputStream outStream = new FileOutputStream(image);
                    resultingImage.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
                    outStream.flush();
                    outStream.close();
                    pb.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(CropImageActivity.this, OcrDetailActivity.class);
                    intent.putExtra("imagePath", imagePath);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CropImageActivity.this, ExpenseMainActivity.class);
        startActivity(intent);
    }

    private Bitmap getRectImage(Point[] points, Float xFactor, Float yFactor, Bitmap image) {

        int witdth= image.getWidth();
        int height = image.getHeight();

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
