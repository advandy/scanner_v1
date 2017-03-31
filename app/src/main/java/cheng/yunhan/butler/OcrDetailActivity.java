package cheng.yunhan.butler;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.concurrent.Cancellable;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

public class OcrDetailActivity extends AppCompatActivity {
    final Context context = this;
    private ProgressBar pb;
    private String imagePath;
    private int day, month, year;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.CustomTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_detail);
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);

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
                Toast toast = Toast.makeText(context, "OCR process in the background", duration);
                toast.show();
                new ProcessOnlineOCR().execute();
            }
        });

        pb = (ProgressBar) findViewById(R.id.progressBar);


    }

    private String processOCR(String imagePath, String txt) throws FileNotFoundException {
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
                    return txt;
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
            return textStr;

        }

        return "";
    }

    private void showOCRResult(String text) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.ocr_popup);
        dialog.setTitle("OCR Result");
        ((TextView)dialog.findViewById(R.id.ocrTextView)).setText(text);
        dialog.show();
    }

    private boolean active=false;

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    private class ProcessOnlineOCR extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            HttpClientBuilder builder= HttpClientBuilder.create();
            HttpClient httpClient = builder.build();
            HttpPost httppost = new HttpPost("http://47.92.27.34:8080/ocr_processor/test/");
            //HttpPost httppost = new HttpPost("http://192.168.0.0:8080/fake/test/");
            File imageFile = new File(imagePath);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntityBuilder.addPart("picFile", new FileBody(imageFile));
            multipartEntityBuilder.addTextBody("superMarket", "DM");
            HttpEntity entity = multipartEntityBuilder.build();
            httppost.setEntity(entity);
            String responseString = "";
            HttpEntity responseEntity = null;
            try {
                HttpResponse response = httpClient.execute(httppost);
                responseEntity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();;
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(responseEntity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }
                Log.i("", String.valueOf(responseString));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result ) {
            //super.onPostExecute(aVoid);
            try {
                ArrayList<ContentValues> shoppinglist = new ArrayList<>();
                JSONObject json = new JSONObject(result); //Convert String to JSON Object

                JSONObject resp = json.getJSONObject("result");
                JSONArray items = resp.getJSONArray("items");
                for (int i = 0; i < items.length(); i ++) {
                    JSONObject item = items.getJSONObject(0);
                    String article = item.getString("name");
                    Double sum = item.getDouble("price");
                    int count = item.getInt("count");
                    ContentValues value = new ContentValues();
                    value.put(DAO.ItemEntry.COLUMN_NAME_COUNT, count);
                    value.put(DAO.ItemEntry.COLUMN_NAME_ARTICLE, article);
                    value.put(DAO.ItemEntry.COLUMN_NAME_SUM, sum);
                    value.put(DAO.ItemEntry.COLUMN_NAME_SHOP, "DM");
                    value.put(DAO.ItemEntry.COLUMN_NAME_DAY, day);
                    value.put(DAO.ItemEntry.COLUMN_NAME_MONTH, month);
                    value.put(DAO.ItemEntry.COLUMN_NAME_YEAR, year);
                    shoppinglist.add(value);
                }

                Intent intent = new Intent(OcrDetailActivity.this, DailyExpenseList.class);
                intent.putExtra("day", day);
                intent.putExtra("month", month);
                intent.putExtra("shop", "DM");
                intent.putParcelableArrayListExtra("shoppinglist", shoppinglist);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
