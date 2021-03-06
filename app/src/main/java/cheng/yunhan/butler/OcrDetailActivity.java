package cheng.yunhan.butler;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

public class OcrDetailActivity extends AppCompatActivity {
    final Context context = this;
    private ProgressBar pb;
    private String imagePath, shopName;
    private int day, month, year;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_detail);
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        ImageView im = (ImageView)findViewById(R.id.imageView2);
        im.setImageBitmap(Utils.decodeSampledBitmapFromFile(imagePath, 1000, 1000));
        shopName = intent.getStringExtra("shopName");
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (shopName.matches((String) adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }

        FloatingActionButton ocrBtn = (FloatingActionButton) findViewById(R.id.ocrBtn);
        ocrBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                shopName = (String) spinner.getSelectedItem();
                Context context = OcrDetailActivity.this;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, "OCR process in the background", duration);
                toast.show();
                new ProcessOnlineOCR().execute();
            }
        });

        pb = (ProgressBar) findViewById(R.id.progressBar);


    }

    private class ProcessOnlineOCR extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            HttpClientBuilder builder= HttpClientBuilder.create();
            HttpClient httpClient = builder.build();
            HttpPost httppost = new HttpPost(Utils.POST_PIC_URL);
            File imageFile = new File(imagePath);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntityBuilder.addPart("picFile", new FileBody(imageFile));
            multipartEntityBuilder.addTextBody("superMarket", shopName);
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
        protected void onPostExecute(String result) {
            pb.setVisibility(View.INVISIBLE);
            try {
                ArrayList<ContentValues> shoppinglist = new ArrayList<>();
                JSONObject json = new JSONObject(result); //Convert String to JSON Object

                JSONObject resp = json.getJSONObject("result");
                String picUrl = json.getString("pic_url");
                JSONArray items = resp.getJSONArray("items");
                for (int i = 0; i < items.length(); i ++) {
                    JSONObject item = items.getJSONObject(i);
                    String article = item.getString("name");
                    Double sum = item.getDouble("price");
                    int count = item.getInt("count");
                    ContentValues value = new ContentValues();
                    value.put(DAO.ItemEntry.COLUMN_NAME_COUNT, count);
                    value.put(DAO.ItemEntry.COLUMN_NAME_ARTICLE, article);
                    value.put(DAO.ItemEntry.COLUMN_NAME_SUM, sum);
                    value.put(DAO.ItemEntry.COLUMN_NAME_SHOP, shopName);
                    value.put(DAO.ItemEntry.COLUMN_NAME_DAY, day);
                    value.put(DAO.ItemEntry.COLUMN_NAME_MONTH, month);
                    value.put(DAO.ItemEntry.COLUMN_NAME_YEAR, year);
                    shoppinglist.add(value);
                }

                Intent intent = new Intent(OcrDetailActivity.this, DailyExpenseList.class);
                intent.putExtra("day", day);
                intent.putExtra("month", month);
                intent.putExtra("shop", shopName);
                intent.putExtra("picUrl", picUrl);
                intent.putParcelableArrayListExtra("shoppinglist", shoppinglist);
                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(context, result + " Please try later", Toast.LENGTH_LONG);
                toast.show();
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
}
