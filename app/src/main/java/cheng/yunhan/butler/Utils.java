package cheng.yunhan.butler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.EditText;

import java.util.HashMap;

/**
 * Created by rabbitsong on 02/04/17.
 */

public class Utils {

    public static boolean checkEditText(EditText editText) {
        String str = editText.getText().toString();
        if (str.matches("")|| str == null) {
            editText.setError("cannot be empty");
            return true;
        }
        return false;
    }

    public static HashMap<String, Integer> getIcons(){
        return new HashMap<String, Integer>(){
            {
                put("Rewe", R.drawable.rewe);
                put("Penny", R.drawable.penny);
                put("Aldi", R.drawable.aldi);
                put("Lidl", R.drawable.lidl);
                put("Dm", R.drawable.dm);
            }
        };
    };

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
