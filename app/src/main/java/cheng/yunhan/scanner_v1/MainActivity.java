package cheng.yunhan.scanner_v1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private CustomAdapter viewAdapter;
    private File imageFile;
    private TextView noFileTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewAdapter = new CustomAdapter(this,new ArrayList<ImageDocument>());
        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(viewAdapter);
        noFileTv = (TextView)findViewById(R.id.noFileTV);

        FloatingActionButton scanBtn = (FloatingActionButton) findViewById(R.id.scan);
        scanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void takePhoto() {
        File file = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //file = new File(Environment.getExternalStorageDirectory()+File.separator + "temp_image_scanner.jpg");
            try {
                file =createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                imageFile = file;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
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

    public class ImageDocument {
        String timeStamp;
        Bitmap image;
        String imagePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            CropImage.activity(Uri.fromFile(imageFile))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Intent toDetailIntent = new Intent(MainActivity.this, OcrDetailActivity.class);
                toDetailIntent.putExtra("imagePath", result.getUri().getPath());
                startActivity(toDetailIntent);

                ImageDocument imageDocument = new ImageDocument();
                imageDocument.timeStamp =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageDocument.image =decodeSampledBitmapFromFile(result.getUri().getPath(),1000, 700);
                imageDocument.imagePath = result.getUri().getPath();
                viewAdapter.add(imageDocument);
                noFileTv.setVisibility(View.INVISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    public class CustomAdapter extends ArrayAdapter<ImageDocument> {
        private class ViewHolder {
            TextView tv;
            ImageView im;
        }


        public CustomAdapter(@NonNull Context context, @NonNull ArrayList<ImageDocument> objects) {
            super(context, 0, objects);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageDocument doc = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.document_view, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tv = (TextView)convertView.findViewById(R.id.docDescription);
                viewHolder.im = (ImageView)convertView.findViewById(R.id.imageView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.im.setImageBitmap(doc.image);
            viewHolder.tv.setText(doc.timeStamp);

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent toDetailIntent = new Intent(MainActivity.this, OcrDetailActivity.class);
                    toDetailIntent.putExtra("imagePath", getItem(position).imagePath);
                    startActivity(toDetailIntent);
                }
            });

            return convertView;
        }
    }
}
