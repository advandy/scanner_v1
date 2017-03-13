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
import android.support.v4.content.FileProvider;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.bitmap;

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

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] imageFiles = storageDir.listFiles();
        Log.e("files", String.valueOf(imageFiles));
        if (imageFiles.length > 0) {
            noFileTv.setVisibility(View.INVISIBLE);
        }
        for (File imageFile: imageFiles) {
            String name = imageFile.getName();
            String imagePath = imageFile.getAbsolutePath();
            Bitmap image = decodeSampledBitmapFromFile(imagePath, 1000, 1000);
            viewAdapter.add(new ImageDocument(name, image, imagePath));
        }
    }

    public class ImageDocument {
        public String timeStamp;
        public Bitmap image;
        public String imagePath;
        public CharSequence ocrText;

        public ImageDocument(String name, Bitmap image, String imagePath) {
            this.timeStamp = name;
            this.image = image;
            this.imagePath = imagePath;
        }
    }

    private void takePhoto() {
        File file = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //file = new File(Environment.getExternalStorageDirectory()+File.separator + "temp_image_scanner.jpg");
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "cheng.yunhan.scanner_v1.provider",
                        file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            CropImage.activity(Uri.fromFile(imageFile))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            imageFile.delete();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Intent toDetailIntent = new Intent(MainActivity.this, OcrDetailActivity.class);
                toDetailIntent.putExtra("imagePath", result.getUri().getPath());
                startActivity(toDetailIntent);

                ImageDocument imageDocument = new ImageDocument(null, null, null);
                imageDocument.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageDocument.image = decodeSampledBitmapFromFile(result.getUri().getPath(),1000, 1000);
                imageDocument.imagePath = result.getUri().getPath();
                OutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(imageFile);
                    imageDocument.image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                viewAdapter.add(imageDocument);
                noFileTv.setVisibility(View.INVISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_CANCELED) {
                imageFile.delete();
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
