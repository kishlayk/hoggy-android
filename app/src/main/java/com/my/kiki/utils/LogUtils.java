package com.my.kiki.utils;

import android.util.Log;


public class LogUtils {

    public static String DEFAULT_TAG = "Kiki App";// MainApplication.getGlobalContext().getClass().getSimpleName();

    /**
     * Print info log
     * @param tag tag
     * @param message message
     */
    public static void i(String tag, String message) {
        if (Utils.SHOULD_PRINT_LOG) {
            Log.i(DEFAULT_TAG, message);
        }
    }

    /**
     * Print debug log
     * @param tag tag
     * @param message message
     */
    public static void d(String tag, String message) {
        if (Utils.SHOULD_PRINT_LOG) {
            Log.d(DEFAULT_TAG, message);
        }
    }

    /**
     * Print error log
     * @param tag tag
     * @param message message
     */
    public static void e(String tag, String message) {
        if (Utils.SHOULD_PRINT_LOG) {
            Log.e(DEFAULT_TAG, message);
        }
    }

    /**
     * Print info log
     * @param message message
     */
    public static void i(String message) {
        if (Utils.SHOULD_PRINT_LOG) {
            Log.i(DEFAULT_TAG, message);
        }
    }

    /**
     * Print debug log
     * @param message message
     */
    public static void d(String message) {
        if (Utils.SHOULD_PRINT_LOG) {
            Log.d(DEFAULT_TAG, message);
        }
    }

    /**
     * Print error log
     * @param message message
     */
    public static void e(String message) {
        if (Utils.SHOULD_PRINT_LOG) {
            Log.e(DEFAULT_TAG, message);
        }
    }
}
