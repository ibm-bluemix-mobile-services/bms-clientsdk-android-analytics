package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;


import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Test;

import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics.log;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.APP_SESSION_ID_KEY;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.CATEGORY;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.LATITUDE_KEY;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.LONGITUDE_KEY;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.TIMESTAMP_KEY;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.USER_ID_KEY;
import static com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics.LOG_LOCATION_KEY;
public class BMSAnalyticsTest {

    protected static String DEFAULT_USER_ID="abcde";

    @Test
    public void logLocationtest() throws JSONException, InterruptedException {
        Logger logger = Logger.getLogger("tag");

        //if collectLocation is false
        BMSAnalytics.collectLocation =false;
        logger.error("You must enable collectLocation before location can be logged");
        waitForNotify(logger);



        //if collectLocation is true

        BMSAnalytics.collectLocation =true;


        JSONObject metadata = new JSONObject();
        String hashedUserID = UUID.nameUUIDFromBytes(DEFAULT_USER_ID.getBytes()).toString();


        metadata.put(CATEGORY, LOG_LOCATION_KEY);
        metadata.put(LATITUDE_KEY,"19.7321");//locationService.getLatitude()
        metadata.put(LONGITUDE_KEY,"72.1234"); //locationService.getLongitude()
        metadata.put(TIMESTAMP_KEY, (new Date()).getTime());
        metadata.put(APP_SESSION_ID_KEY, MFPAnalyticsActivityLifecycleListener.getAppSessionID());
        metadata.put(USER_ID_KEY,hashedUserID);

        logger.debug("JSONException encountered logging change in user context: ");
        waitForNotify(logger);

        log(metadata);


    }

    private static void waitForNotify(Object obj) throws InterruptedException {
        synchronized(obj) {
            obj.wait(10000);
        }
        if (obj.equals(LogPersister.WAIT_LOCK)) {
            Thread.sleep(100);  // always sleep an extra 100 ms to avoid post-wait log calls that may be sprinkled through the code
        }
    }

}
