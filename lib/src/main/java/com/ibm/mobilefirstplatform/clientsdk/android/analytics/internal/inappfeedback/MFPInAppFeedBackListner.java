package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inappfeedback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

public class MFPInAppFeedBackListner {

    private static Activity currentActivity = null;

    private static Context context = null;
    private static String userIdentity = "undefined";

    public static void setContext(Context context){
        if(null == MFPInAppFeedBackListner.context) {
            MFPInAppFeedBackListner.context = context;
            Utility.setStorageLocation(MFPInAppFeedBackListner.context);
        }
    }

    public static void setUserIdentity(String userIdentity){
        MFPInAppFeedBackListner.userIdentity = userIdentity;
    }

    protected static String getUserIdentity(){
        return userIdentity;
    }

    public static Context getContext(){
        return MFPInAppFeedBackListner.context;
    }

    public static void triggerFeedbackMode(final Activity activity){

        if(null != MFPInAppFeedBackListner.context) {
            currentActivity = activity;
            callFeedbackEditScreenActivity();
        }
    }

    public static void sendAppFeedback(){
        SendAppFeedback.sendLogsToServer(false);
    }

    private static void callFeedbackEditScreenActivity(){
        Bitmap image = ScreenShot.takeScreenShotOfRootView(currentActivity.getWindow().getDecorView().getRootView());
        String filename = Utility.generateUniqueFileName(currentActivity.getClass().getName());
        Utility.saveIamgeToLocalStore(image, Utility.getImageFileName(filename));

        Intent intent = new Intent(currentActivity, EditFeedback.class);
        intent.putExtra("imagename", filename);
        intent.putExtra("imageWidth", image.getWidth());
        intent.putExtra("imageHeight", image.getHeight());
        currentActivity.startActivity(intent);
    }



    static class ScreenShot {
        public static Bitmap takeScreenShot(View v){
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache(true);
            Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);
            return b;
        }

        public static Bitmap takeScreenShotOfRootView(View v){
            return takeScreenShot(v.getRootView().getRootView());
        }
    }
}
