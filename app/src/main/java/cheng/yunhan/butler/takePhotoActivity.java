package cheng.yunhan.butler;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cheng.yunhan.butler.customview.CameraPreview;

import static android.content.ContentValues.TAG;

@SuppressWarnings("ALL")
public class takePhotoActivity extends Activity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    File imageFile;
    String shopName;
    private Camera mCamera;
    private CameraPreview mPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        shopName = intent.getStringExtra("shopName");

        setContentView(R.layout.camera_preview);

        lauchCamera();

        //takePhoto();
    }

    private void lauchCamera() {
        mCamera = getCameraInstance();

        if (mCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            Button captureButton = (Button) findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            mCamera.takePicture(null, null, mPicture);
                        }
                    }
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);

            mCamera.release();
            mCamera = null;
        }
    }

    private void backToTimeline() {
        Intent backIntent = new Intent();
        setResult(RESULT_CANCELED, backIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            lauchCamera();
        } else {
            onBackPressed();
        }
    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //ask for authorisation
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
            } else {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

            }
        }
        catch (Exception e){
            Log.e("", String.valueOf(e));
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = null;
            try {
                pictureFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (pictureFile == null){
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                imageFile = pictureFile;
                Intent intent = new Intent(takePhotoActivity.this, CropImageActivity.class);
                intent.putExtra("imageUri", Uri.fromFile(imageFile).getPath());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("shopName", shopName);
                startActivity(intent);
                finish();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Intent intent = new Intent(takePhotoActivity.this, CropImageActivity.class);
            intent.putExtra("imageUri", Uri.fromFile(imageFile).getPath());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("shopName", shopName);
            startActivity(intent);
            finish();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            imageFile.delete();
            backToTimeline();
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

        try {
            boolean hasFlash = this.getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (hasFlash) {
                Camera cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }

        } catch (Exception e2) {
            Toast.makeText(getApplicationContext(), "Torch Failed: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
        }

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
}
