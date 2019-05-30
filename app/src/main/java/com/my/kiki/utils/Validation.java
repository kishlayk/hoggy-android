package com.my.kiki.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

public class Validation {


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public static boolean isValidMail(EditText editText, String msg) {

        String emailaddress = editText.getText().toString().trim();
        editText.setError(null);
        editText.requestFocus();
        if (emailaddress.isEmpty() || !isValidEmail(emailaddress)) {
            editText.setError(msg);

            return false;
        } else {
            //  editText.setError(msg);
        }

        return true;
    }


    public static boolean hasText(EditText editText, String msg) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        editText.requestFocus();
        if (text.length() == 0) {
            editText.setError(msg);
            return false;
        }
        return true;
    }



}
