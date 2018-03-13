package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inappfeedback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utility {

    protected static String storageDirectory = null;
    protected static final String LOG_TAG_NAME = Utility.class.getName();
    protected static String appFeedBackSummaryFile = "AppFeedBackSummary.json";

    protected static float dipToPixels(Context applicationContext, int dipValue) {
        DisplayMetrics metrics = applicationContext.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    protected static float spToPixels(Context applicationContext, float spValue) {
        DisplayMetrics metrics = applicationContext.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, metrics);
    }

    protected static Bitmap getTempBitMap(Context applicationContext, String filename){
        Bitmap bitmap = null;
        try {
            Uri uri = Uri.parse("file://" + Utility.storageDirectory + filename);
            bitmap = BitmapFactory.decodeStream(applicationContext.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected static Bitmap getMasterBitMap(Bitmap tempBitmap) {
        Bitmap bitmapMaster = null;
        Bitmap.Config config;
        if(tempBitmap.getConfig() != null){
            config = tempBitmap.getConfig();
        }else{
            config = Bitmap.Config.ARGB_8888;
        }

        //bitmapMaster is Mutable bitmap
        bitmapMaster = Bitmap.createBitmap(
                tempBitmap.getWidth(),
                tempBitmap.getHeight(),
                config);

        return bitmapMaster;
    }

    protected static Canvas getCanvas(Bitmap tempBitmap, Bitmap bitmapMaster ){
        Canvas canvasMaster = new Canvas(bitmapMaster);
        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);
        return canvasMaster;
    }

    protected static void saveIamgeToLocalStore(Bitmap finalBitmap, String fileName) {
        File myDir = new File(Utility.storageDirectory);
        myDir.mkdirs();

        if (!myDir.exists()) {
            //TODO: Crash feed back mode
        }

        File file = new File(myDir, fileName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String convertFileToString(String fileName) {

        String returnStr = "";
        String dir = Utility.storageDirectory;
        File file = new File(dir, fileName);

        if(file.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    returnStr +=line;
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(Utility.LOG_TAG_NAME, "convertFileToString: " + file.getName() + ":" +fileName + ":" + returnStr);
        return returnStr;
    }

    protected static String getJSONfileName(String instanceName){
        return instanceName+".json";
    }

    protected static String getScreenName(String instanceFileName){
        return instanceFileName.substring(0,instanceFileName.indexOf("_"));
    }

    protected static String getTimeCreated(String instanceFileName){
        return instanceFileName.substring(instanceFileName.indexOf("_")+1);
    }

    protected static String getDeviceID(Context context) {
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return UUID.nameUUIDFromBytes(uuid.getBytes()).toString();
    }

    protected static void addDataToFile(String fileName, String data, boolean append){
        String dir = Utility.storageDirectory;
        File file = new File(dir, fileName);
        try {
            FileOutputStream fis = new FileOutputStream(file,append);
            PrintStream ps = new PrintStream(fis);
            ps.print(data+"\n");
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected static void discardFeedbackFiles(String filename) {
        String path = Utility.storageDirectory;

        //Delete image file
        File file = new File(path, getImageFileName(filename));
        if (file.exists() && !file.delete()) {
            Log.e(Utility.LOG_TAG_NAME, "file could not be deleted :" + file.getPath());
        }

        //Delete Json file if exists
        File jsonFile = new File(path, getJSONfileName(filename));
        if (jsonFile.exists() && !jsonFile.delete()) {
            Log.e(Utility.LOG_TAG_NAME, "file could not be deleted :" + file.getPath());
        }

        //Delete entry from saved AppFeedBackSummary.json
        String appFeedBackSummary = Utility.convertFileToString(appFeedBackSummaryFile);
        if (!"".equals(appFeedBackSummary) && !"{}".equals(appFeedBackSummary) && appFeedBackSummary.contains(filename)) {
            try {
                JSONObject appFeedBacksummaryJSON = new JSONObject(appFeedBackSummary);
                JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");
                JSONObject sentObject = (JSONObject) appFeedBacksummaryJSON.get("send");
                savedArray = removeEntry(savedArray,filename);
                appFeedBacksummaryJSON.put("saved", savedArray);
                appFeedBacksummaryJSON.put("send", sentObject);
                addDataToFile(appFeedBackSummaryFile, appFeedBacksummaryJSON.toString(),false);
            } catch (JSONException je) {
                //should not get any exception
                je.printStackTrace();
            }
        }
    }

    protected static String generateUniqueFileName(String baseName){
        long now = new Date().getTime();
        return baseName+"_"+now;
    }

    protected static String getImageFileName(String baseName){
        return baseName+".png";
    }

    protected static void setStorageLocation(Context context){
        Utility.storageDirectory = context.getFilesDir().getPath()+"/feedback/";
        Log.d(Utility.LOG_TAG_NAME, "Utility.storageDirectory: "+Utility.storageDirectory);
    }


    protected static void createZipArchive(List<String> fileList, String zipFile){
        try {
            int BUFFER = 512;
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (String filename : fileList) {
                filename = Utility.storageDirectory + filename;
                Log.d(Utility.LOG_TAG_NAME, "Compress : Adding: " + filename);
                FileInputStream fi = new FileInputStream(filename);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(filename.substring(filename.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String addAndFetchSentTimeFromScreenFeedBackJson(String jsonFile, long timeSent, boolean setSentTime){
        String screenFeedBackJsonString = Utility.convertFileToString(jsonFile);
        String actualTimeSent = null;
        try{
            JSONObject screenFeedBackJson = new JSONObject(screenFeedBackJsonString);
            try{
                actualTimeSent = (String)screenFeedBackJson.get("timeSent");
            }catch(JSONException js ){
                //do nothing. Exception will be thrown if no timeSent entry present
            }

            //Add timeSent only if not set previously
            if(setSentTime && (actualTimeSent==null || actualTimeSent.equals(""))) {
                actualTimeSent = ""+timeSent;
                screenFeedBackJson.put("timeSent", actualTimeSent);
                Utility.addDataToFile(jsonFile, screenFeedBackJson.toString(), false);
            }
        }catch(JSONException js ){

        }
        return actualTimeSent;
    }

    protected static JSONArray removeEntry(JSONArray inputJsonArray, String entryToRemove){

        JSONArray output = new JSONArray();
        int len = inputJsonArray.length();
        for (int i = 0; i < len; i++)   {

            try {
                String element =  (String)inputJsonArray.get(i);
                if(!element.equals(entryToRemove)){
                    output.put(element);
                }
            } catch (JSONException e) {

            }
        }
        return output;
    }

    protected synchronized static void updateSummaryJson(String sentElement, String timeSent) {
        String appFeedBackSummary = Utility.convertFileToString(appFeedBackSummaryFile);
        Log.d(Utility.LOG_TAG_NAME, "Entering updateSummaryJson: appFeedBackSummary: " + appFeedBackSummary);
        if (!"".equals(appFeedBackSummary) && !"{}".equals(appFeedBackSummary)) {
            try {
                JSONObject appFeedBacksummaryJSON = new JSONObject(appFeedBackSummary);
                JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");
                JSONObject sentObject = (JSONObject)appFeedBacksummaryJSON.get("send");

                JSONArray perTimeSentArray = null;
                try{
                    perTimeSentArray = (JSONArray)sentObject.get(timeSent);
                }catch (JSONException je){
                    //do nothing. Exception will be thrown if no Array present for given timeSent
                }

                if(perTimeSentArray!=null){
                    perTimeSentArray.put(sentElement);
                }else{
                    perTimeSentArray = new JSONArray();
                    perTimeSentArray.put(sentElement);
                }

                savedArray = removeEntry(savedArray,sentElement);
                sentObject.put(timeSent,perTimeSentArray);

                appFeedBacksummaryJSON.put("saved",savedArray);
                appFeedBacksummaryJSON.put("send", sentObject);
                Log.d(Utility.LOG_TAG_NAME, "appFeedBackSummary: " + appFeedBacksummaryJSON.toString());
                Utility.addDataToFile(appFeedBackSummaryFile, appFeedBacksummaryJSON.toString(), false);
            }catch (JSONException je){
                je.printStackTrace();
            }
        }
    }
}
