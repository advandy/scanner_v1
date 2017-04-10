package cheng.yunhan.butler;

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

    public static String POST_PIC_URL = "http://46.101.168.87:8080/ocr_processor/proxy/";
    public static String POST_PROCESSED_URL = "http://46.101.168.87:8080/ocr_processor/processed/";

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
}
