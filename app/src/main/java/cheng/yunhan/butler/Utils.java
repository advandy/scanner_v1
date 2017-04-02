package cheng.yunhan.butler;

import android.widget.EditText;

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
}
