# bms-clientsdk-android-analytics

[![Build Status](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics.svg?branch=master)](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics)
[![Build Status](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics.svg?branch=development)](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a7341d4023484dfeb09bf79d0db97484)](https://www.codacy.com/app/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ibm-bluemix-mobile-services/bms-clientsdk-android-analytics&amp;utm_campaign=Badge_Grade)
[![Coverage Status](https://coveralls.io/repos/github/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics/badge.svg?branch=code-coverage)](https://coveralls.io/github/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics?branch=code-coverage)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ibm.mobilefirstplatform.clientsdk.android/analytics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ibm.mobilefirstplatform.clientsdk.android/analytics)

##Release Notes:

###1.2.0
- Added location for log recording

###1.1.12
- Updated okhttp version from 2.7.4 to 3.9.0 

###1.1.11
- Fixed Analytics to log RequestMethod in NetworkRequests data for a network access

###1.1.10
- Fixed issue where Analytics log recording was not being enabled when initializing.

###1.1.9
- Fixed issue where application session timestamp was not being correctly recorded.

###1.1.8
- Reverted back to also monitoring calls to the Analytics service.

###1.1.7
- Change network transaction monitoring so that calls to the Analytics service are not logged.

###1.1.6
- Fixed issue where developers would get a 403 when sending logs/analytics to the Mobile Analytics service.

###1.1.5:
- Added the ability to record network transactions by using `Analytics.DeviceEvent.NETWORK` as one of the device events when calling `Analytics.initialize()`.

###1.1.4:
- Fixed issue regarding an exception log that was being shown when reading the SDK version number.

###1.1.3:
- Fixed NullPointerException when calling `Analytics.send()` or `Logger.send()` without a response listener.

###1.1.2:
- Fixed problem with `Analytics.init()` where if you set `hasUserContext` to `false`, it would not properly initialize.

###1.1.1:
- Changed Javadoc and corrected small logic error

###1.1.0:
- Added new initializer in order to avoid double counting anonymous users and named users as the same. Deprecated `BMSAnalytics.clearUserIdentity()`, will be removed in 2.x.

###1.0.5:
- Android Nougat officially supported; changed target SDK version to Android 24.

###1.0.3:
- Fixed error with App Sessions not being properly counted.

Known limitation - currently v2.x of Android SDK does not submit monitoring data. This is a work in progress and will be delivered in following months. If you’d like to continue receiving monitoring data in the service dashboard you can continue using the v1.x SDK.

Copyright 2016 IBM Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
