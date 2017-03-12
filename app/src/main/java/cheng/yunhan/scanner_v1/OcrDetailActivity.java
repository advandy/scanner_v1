package cheng.yunhan.scanner_v1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OcrDetailActivity extends AppCompatActivity {

    private ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_detail);
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        ImageView im = (ImageView)findViewById(R.id.imageView2);
        im.setImageBitmap(decodeSampledBitmapFromFile(imagePath, 1000, 1000));

        FloatingActionButton ocrBtn = (FloatingActionButton) findViewById(R.id.ocrBtn);
        ocrBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                new ProcessOnlineOCR().execute();
            }
        });

        pb = (ProgressBar) findViewById(R.id.progressBar);

    }

    static boolean active;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    private class ProcessOnlineOCR extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            Thread thread = new Thread();
            try {
                thread.sleep(3000);
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);

            if (!active) {
                return;
            }

            pb.setVisibility(View.INVISIBLE);
            final Dialog dialog = new Dialog(OcrDetailActivity.this);
            dialog.setContentView(R.layout.ocr_popup);
            dialog.setTitle("OCR Result");
            ((TextView)dialog.findViewById(R.id.ocrTextView)).setText("djfdjfk, fjdlfjldf, jdlfjldjfld, fjdlfjldfjldjf, fjdlfjldfjkdjfdf,j lfdjlfkdfdklfjklfdjkfjdf.jfldjfldlfdf");
            dialog.show();
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
}
