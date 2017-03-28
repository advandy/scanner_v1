package cheng.yunhan.butler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class takePhotoActivity extends Activity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_OCR = 2;
    File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        takePhoto();
    }

    private void backToTimeline() {
        Intent backIntent = new Intent();
        setResult(RESULT_CANCELED, backIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Intent intent = new Intent(takePhotoActivity.this, CropImageActivity.class);
            intent.putExtra("imageUri", Uri.fromFile(imageFile).getPath());

            startActivity(intent);
            /*CropImage.activity(Uri.fromFile(imageFile))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);*/
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            imageFile.delete();
            backToTimeline();
        }

        if (requestCode == REQUEST_OCR) {
            if (resultCode == RESULT_CANCELED) {
                imageFile.delete();
                backToTimeline();
            } else if (resultCode == RESULT_OK) {
                // TODO: 24.03.2017 Process response from server
                imageFile.delete();
                backToTimeline();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Intent toDetailIntent = new Intent(takePhotoActivity.this, OcrDetailActivity.class);
                toDetailIntent.putExtra("imagePath", result.getUri().getPath());
                startActivityForResult(toDetailIntent, REQUEST_OCR);

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
                imageFile.delete();
                backToTimeline();
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_CANCELED) {
                imageFile.delete();
                backToTimeline();
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
                if (Build.VERSION.SDK_INT >= 24) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "cheng.yunhan.butler.provider",
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
