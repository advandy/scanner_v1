package cheng.yunhan.scanner_v1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

public class OcrDetailActivity extends AppCompatActivity {

    private ProgressBar pb;
    private String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_detail);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        ImageView im = (ImageView)findViewById(R.id.imageView2);
        im.setImageBitmap(decodeSampledBitmapFromFile(imagePath, 1000, 1000));

        FloatingActionButton ocrBtn = (FloatingActionButton) findViewById(R.id.ocrBtn);
        ocrBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                active = true;
                Context context = OcrDetailActivity.this;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "Upload Image and process OCR", duration);
                toast.show();
                new ProcessOnlineOCR().execute();
            }
        });

        pb = (ProgressBar) findViewById(R.id.progressBar);

    }

    private void writeOcrTextToFile(String imagePath, String txt) throws FileNotFoundException {
        File storageDir = OcrDetailActivity.this.getExternalFilesDir("OCRText");

        String[] paths = imagePath.split("/");
        final String fileName = paths[paths.length-1];

        File[] files = storageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(fileName);
            }
        });

        File ocrTextFile = null;
        if (files.length == 0) {
            try {
                ocrTextFile = File.createTempFile(
                        fileName,  /* prefix */
                        ".txt",         /* suffix */
                        storageDir      /* directory */
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (ocrTextFile != null) {
                try {
                    FileOutputStream fOut = new FileOutputStream(ocrTextFile);
                    OutputStreamWriter outputStream = new OutputStreamWriter(fOut);
                    outputStream.append(txt);
                    outputStream.close();
                    fOut.flush();
                    fOut.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ocrTextFile = files[0];
            BufferedReader br = new BufferedReader(new FileReader(ocrTextFile));

            StringBuilder text = new StringBuilder();

            try {
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }

            String textStr = text.toString();
            Log.e("",text.toString());

        }
    }

    private boolean active=false;

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
                writeOcrTextToFile(imagePath, (new Date()).getTime() + "Some random words");
                thread.sleep(3000);
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
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
            ((TextView)dialog.findViewById(R.id.ocrTextView)).setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum");
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
