/*
 * IBM Confidential OCO Source Materials
 *
 * 5725-I43 Copyright IBM Corp. 2006, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *
 */
package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;


import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.BaseRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.internal.LogPersisterDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

/**
 * <p>
 * the Analytics service.
 * MFPAnalytics provides means of persistently capturing analytics data and provides a method call to send captured data to
 * </p>
 * <p>
 * Capture is on by default.
 * </p>
 * <p>
 * When this MFPAnalytics class's capture flag is turned on via enable method call,
 * all analytics will be persisted to file in the following JSON object format:
 * <p>
 * <pre>
 * {
 *   "timestamp"    : "17-02-2013 13:54:27:123",  // "dd-MM-yyyy hh:mm:ss:S"
 *   "level"        : "ERROR",                    // ERROR || WARN || INFO || LOG || DEBUG
 *   "package"      : "your_tag",                 // typically a class name, app name, or JavaScript object name
 *   "msg"          : "the message",              // a helpful log message
 *   "metadata"     : {"hi": "world"},            // (optional) additional JSON metadata
 *   "threadid"     : long                        // (optional) id of the current thread
 * }
 * </pre>
 * </p>
 * <p>
 * Log data is accumulated persistently to a log file until the file size is greater than FILE_SIZE_LOG_THRESHOLD.
 * At this point the log file is rolled over. Log data will only be captured once
 * {@link com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient#initialize(Context, String, String, String)} is called.  Once both files are full, the oldest log data
 * is pushed out to make room for new log data.
 * </p>
 * <p>
 * Log file data is sent to the Bluemix server when this class's send() method is called and the accumulated log
 * size is greater than zero.  When the log data is successfully uploaded, the persisted local log data is deleted.
 * </p>
 */
public class BMSAnalytics {
    protected static final Logger logger = Logger.getLogger(LogPersister.INTERNAL_PREFIX + "analytics");

    protected static String clientApiKey = null;
    protected static String appName = null;
    protected static boolean hasUserContext = false;
    public static boolean isRecordingNetworkEvents = false;
    public static boolean collectLocation = true;
    public static MFPAnalyticsLocationListener locationService = null;


    protected static String DEFAULT_USER_ID="";

    public static final String CATEGORY = "$category";
    public static final String TIMESTAMP_KEY = "$timestamp";
    public static final String LONGITUDE_KEY = "$longitude";
    public static final String LATITUDE_KEY = "$latitude";

    public static final String APP_SESSION_ID_KEY = "$appSessionID";
    public static final String USER_ID_KEY = "$userID";
    public static final String LOG_LOCATION_KEY = "logLocation";
    public static final String USER_SWITCH_CATEGORY = "userSwitch";
    public static final String INITIAL_CTX_CATEGORY = "initialCtx";

    public static String overrideServerHost = null;
    /**
     * Initialize BMSAnalytics API.
     * This must be called before any other BMSAnalytics.* methods
     *
     * @param app Android Application to instrument with MFPAnalytics.
     * @param applicationName Application's common name.  Should be consistent across platforms.
     * @param clientApiKey The Client API Key used to communicate with your MFPAnalytics service.
     * @param hasUserContext If false, Analytics only records one user per device. If true, setting the user identity will keep a record of all users.
     * @param collectLocation If true, Analytics will begin to record location metadeta
     * @param contexts One or more context attributes MFPAnalytics will register event listeners for.
     */
    static public void init(Application app, String applicationName, String clientApiKey, boolean hasUserContext, boolean collectLocation, Analytics.DeviceEvent... contexts) {

        Context context = app.getApplicationContext();

        //Initialize LogPersister
        LogPersister.setLogLevel(Logger.getLogLevel());
        LogPersister.setContext(context);

        //Instrument Logger with LogPersisterDelegate
        LogPersisterDelegate logPersisterDelegate = new LogPersisterDelegate();
        Logger.setLogPersister(logPersisterDelegate);

        Analytics.setAnalyticsDelegate(new BMSAnalyticsDelegate());

        BMSAnalytics.clientApiKey = clientApiKey;

        if(contexts != null){
            for(Analytics.DeviceEvent event : contexts){
                switch(event){
                    case LIFECYCLE:
                        MFPActivityLifeCycleCallbackListener.init(app);
                        break;
                    case NETWORK:
                        isRecordingNetworkEvents = true;
                        break;
                    case ALL:
                        MFPActivityLifeCycleCallbackListener.init(app);
                        isRecordingNetworkEvents = true;
                        break;
                    case NONE:
                        break;
                }
            }
        }

         if (!hasUserContext) {
         //    Use device ID as default user ID:
            DEFAULT_USER_ID = getDeviceID(context);
            setUserIdentity(DEFAULT_USER_ID, true);
         }

	    if (collectLocation) {
            locationService = MFPAnalyticsLocationListener.getInstance(context);
            BMSAnalytics.collectLocation = collectLocation;
            locationService.init();
        }


        BMSAnalytics.hasUserContext = hasUserContext;
        appName = applicationName;

        //Intercept requests to add device metadata header
        BaseRequest.registerInterceptor(new MetadataHeaderInterceptor(context.getApplicationContext()));
        BaseRequest.registerInterceptor(new NetworkLoggingInterceptor());

        enable();
    }

    /**
     * Initialize MFPAnalytics API.
     * This must be called before any other MFPAnalytics.* methods
     *
     * @deprecated As of release 1.1.0, replaced by {@link #init(Application, String, String, boolean, boolean, Analytics.DeviceEvent...)}}
     * please use the new init with user collection boolean. Using this method will
     * only collect anonymous users and throw exceptions when trying to set user identity
     *
     *
     * @param app Android Application to instrument with MFPAnalytics.
     * @param applicationName Application's common name.  Should be consistent across platforms.
     * @param clientApiKey The Client API Key used to communicate with your MFPAnalytics service.
     * @param contexts One or more context attributes MFPAnalytics will register event listeners for.
     */
    @Deprecated
    static public void init(Application app, String applicationName, String clientApiKey, Analytics.DeviceEvent... contexts) {
        init(app, applicationName, clientApiKey, false, false, contexts);
    }

    static protected String getDeviceID(Context context) {
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return uuid;
    }

    /**
     * Enable persistent capture of analytics data.  Enable, and thus capture, is the default.
     */
    public static void enable(){
        LogPersister.setAnalyticsCapture(true);
    }

    /**
     * Disable persistent capture of analytics data.
     */
    public static void disable(){
        LogPersister.setAnalyticsCapture(false);
    }

    /**
     * Determine if the capture of analytics events is enabled.
     * @return true if capture of analytics is enabled
     */
    public static boolean isEnabled(){
        return LogPersister.getAnalyticsCapture();
    }

    /**
     * Send the accumulated log data when the persistent log buffer exists and is not empty.  The data
     * accumulates in the log buffer from the use of {@link BMSAnalytics} with capture
     * (see {@link BMSAnalytics#enable()}) turned on.
     *
     */
    public static void send(){
        //locationService.unregister();
        LogPersister.sendAnalytics(null);

    }

    /**
     * See {@link BMSAnalytics#send()}
     *
     * @param listener RequestListener which specifies an onSuccess callback and an onFailure callback (see {@link ResponseListener})
     */
    public static void send(ResponseListener listener) {
        LogPersister.sendAnalytics(listener);
    }

    /**
     * Log an analytics event.
     *
     * @param eventDescription An object that contains the description for the event
     */
    public static void log(final JSONObject eventDescription) {
        logger.analytics("", eventDescription);
    }

    /**
     * Log location event
     */
    public static void logLocation(){
        if (!BMSAnalytics.collectLocation) {
            logger.error("You must enable collectLocation before location can be logged");
            return;
        }

        // Create metadata object to log
        JSONObject metadata = new JSONObject();
        String hashedUserID = UUID.nameUUIDFromBytes(DEFAULT_USER_ID.getBytes()).toString();

        try {
            metadata.put(CATEGORY, LOG_LOCATION_KEY);
            if(locationService != null) metadata.put(LATITUDE_KEY,locationService.getLatitude());
            if(locationService != null) metadata.put(LONGITUDE_KEY,locationService.getLongitude());
            metadata.put(TIMESTAMP_KEY, (new Date()).getTime());
            metadata.put(APP_SESSION_ID_KEY, MFPAnalyticsActivityLifecycleListener.getAppSessionID());
            metadata.put(USER_ID_KEY,hashedUserID);

        } catch (JSONException e) {
            logger.debug("JSONException encountered logging change in user context: " + e.getMessage());
        }

        log(metadata);

    }

    /**
     * Specify current application user.  This value will be hashed to ensure privacy.
     * If your application does not have user context, then nothing will happen.
     *
     * @param user User User id for current app user.
     * @param isInitialCtx True if it's a user in the initial context (i.e. when app first starts)
     */
    private static void setUserIdentity(final String user, boolean isInitialCtx) {
        if (!isInitialCtx && !BMSAnalytics.hasUserContext) {
            // log it to file:
            logger.error("Cannot set user identity with anonymous user collection enabled.");
            return;
        }

        // Create metadata object to log
        JSONObject metadata = new JSONObject();

        DEFAULT_USER_ID=user;
        String hashedUserID = UUID.nameUUIDFromBytes(user.getBytes()).toString();

        try {
            if (isInitialCtx) {
                metadata.put(CATEGORY, INITIAL_CTX_CATEGORY);
            } else {
                metadata.put(CATEGORY, USER_SWITCH_CATEGORY);
            }
            if (BMSAnalytics.collectLocation && locationService.getInitLocationRequests()) {
                metadata.put(LONGITUDE_KEY, locationService.getLongitude());
                metadata.put(LATITUDE_KEY, locationService.getLatitude());
            }

            metadata.put(TIMESTAMP_KEY, (new Date()).getTime());
            metadata.put(APP_SESSION_ID_KEY, MFPAnalyticsActivityLifecycleListener.getAppSessionID());
            metadata.put(USER_ID_KEY, hashedUserID);
        } catch (JSONException e) {
            logger.debug("JSONException encountered logging change in user context: " + e.getMessage());
        }

        log(metadata);
    }

    /**
     * Specify current application user.  This value will be hashed to ensure privacy.
     * If your application does not have user context, then nothing will happen.
     *
     * @param user User User id for current app user.
     */
    public static void setUserIdentity(final String user) {
        setUserIdentity(user, false);
    }

    /**
     * @deprecated As of 1.1.0, going to be removed as of 2.0
     * since there is anonymous collection and named user collection
     *
     * Does not do anything now
     */
    @Deprecated
    public static void clearUserIdentity(){
        //used to set identity to default, but now, with anonymous, the user is always default, and with
        //named users, there is no default
    }

    public static String getClientApiKey(){
        return clientApiKey;
    }

    public static String getAppName(){
        return appName;
    }



    /**
     * Implements the android life cycle callbacks to be registered with the application.
     *
     * Implemented as a singleton so that application callbacks can only be registered once.
     */
    private static class MFPActivityLifeCycleCallbackListener implements Application.ActivityLifecycleCallbacks {
        private static MFPActivityLifeCycleCallbackListener instance;

        public static void init(Application app) {
            if (instance == null) {
                instance = new MFPActivityLifeCycleCallbackListener();

                app.registerActivityLifecycleCallbacks(instance);
                MFPAnalyticsActivityLifecycleListener.getInstance().onResume();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            MFPAnalyticsActivityLifecycleListener.getInstance().onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            MFPAnalyticsActivityLifecycleListener.getInstance().onPause();
        }

        // we do not currently instrument any other lifecycle callbacks
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

}
