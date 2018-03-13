package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inappfeedback;

import android.content.Context;
import android.os.Build;

import com.ibm.mobilefirstplatform.clientsdk.android.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2, constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class MFPInAppFeedBackListnerTest {

    protected Context activity = RuntimeEnvironment.application.getApplicationContext();

    @Test
    public void testSetContext() throws Exception{
        MFPInAppFeedBackListner.setContext(activity);
        assertEquals(activity,MFPInAppFeedBackListner.getContext());
    }

    @Test
    public void testSetUserIdentity() throws Exception{
        MFPInAppFeedBackListner.setUserIdentity("Sam");
        assertEquals("Sam",MFPInAppFeedBackListner.getUserIdentity());
    }

    @Test
    public void testTriggerFeedbackMode() throws Exception{
        try{
            MFPInAppFeedBackListner.triggerFeedbackMode(null);
            fail();
        }catch(Exception e){

        }
    }

}
