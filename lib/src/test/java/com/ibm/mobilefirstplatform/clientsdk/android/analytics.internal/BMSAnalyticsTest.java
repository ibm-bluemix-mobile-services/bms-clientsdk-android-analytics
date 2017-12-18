package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;



import org.json.JSONException;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class BMSAnalyticsTest {


    @Test
    public void logLocationtest() throws JSONException, InterruptedException {

        //if collectLocation is true
        BMSAnalytics.collectLocation =true;

        BMSAnalytics.logLocation();

        //if collectLocation is false
        BMSAnalytics.collectLocation =false;

        BMSAnalytics.logLocation();


   //     assertTrue(true);



    }

    

}
