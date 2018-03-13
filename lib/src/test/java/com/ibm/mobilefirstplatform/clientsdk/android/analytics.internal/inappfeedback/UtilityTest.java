package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inappfeedback;

import android.content.Context;
import android.os.Build;

import com.ibm.mobilefirstplatform.clientsdk.android.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2, constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class UtilityTest {

    protected Context activity = RuntimeEnvironment.application.getApplicationContext();

    @Test
    public void testDipToPixels() throws Exception{
        int dp10 = (int)Utility.dipToPixels(activity, 10);
        assertEquals("should be "+dp10,10,dp10);
    }

    @Test
    public void testSipToPixels() throws Exception{
        int sp10 = (int)Utility.spToPixels(activity, 10);
        assertEquals("should be "+sp10,10,sp10);
    }

    @Test
    public void testConvertFileToString() throws Exception{
        assertEquals("",Utility.convertFileToString("dummyFile"));
    }

    @Test
    public void testGetJSONfileName() throws Exception{
        assertEquals("dummyFile.json",Utility.getJSONfileName("dummyFile"));
    }

    @Test
    public void testGetScreenName() throws Exception{
        assertEquals("dummyFile",Utility.getScreenName("dummyFile_1234567"));
    }

    @Test
    public void testGetImageFileName() throws Exception{
        assertEquals("dummyFile_1234567.png",Utility.getImageFileName("dummyFile_1234567"));
    }

    @Test
    public void testGetTimeCreated() throws Exception{
        assertEquals("1234567",Utility.getTimeCreated("dummyFile_1234567"));
    }

    @Test
    public void testGetDeviceID() throws Exception{
        try{
            Utility.getDeviceID(activity);
            fail();
        }catch(Exception e){

        }

    }

    public void testAddDataToFile() throws Exception{
        Utility.addDataToFile("file1.json", "Hello world", true);
        assertEquals("Hello world",Utility.convertFileToString("file1.json"));
        Utility.addDataToFile("file1.json", "Hello world1", false);
        assertEquals("Hello world1",Utility.convertFileToString("file1.json"));
        Utility.addDataToFile("file1.json", "world2", true);
        assertEquals("Hello world1\nworld2",Utility.convertFileToString("file1.json"));
    }

    public void testSettingStorageLocation() throws Exception{
        assertEquals(null,Utility.storageDirectory);
        Utility.setStorageLocation(activity);
        assertEquals(activity.getFilesDir().getPath()+"/feedback/",Utility.storageDirectory);
    }

    public void testRemoveEntry() throws Exception{
        String instanceName1 = "dummyInstance_1234567";
        String instanceName2 = "dummyInstance_1234567";
        JSONObject appFeedBacksummaryJSON = new JSONObject();
        appFeedBacksummaryJSON.put("saved", new JSONArray().put(instanceName1));
        appFeedBacksummaryJSON.put("send", new JSONObject());

        JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");
        savedArray.put(instanceName2);

        assertEquals(2, savedArray.length());
        Utility.removeEntry(savedArray,instanceName2);
        assertEquals(1, savedArray.length());
    }

    public void testSendLogsToServer() throws Exception{
        SendAppFeedback.sendLogsToServer(false);
    }
}
