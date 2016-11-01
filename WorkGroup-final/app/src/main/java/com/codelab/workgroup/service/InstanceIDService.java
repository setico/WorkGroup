package com.codelab.workgroup.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by setico on 10/31/16.
 */
public class InstanceIDService  extends FirebaseInstanceIdService {

        private static final String TAG = "FirebaseIIDService";
        private static final String DEFAULT_TOPIC = "default";


        @Override
        public void onTokenRefresh() {
            // If you need to handle the generation of a token, initially or
            // after a refresh this is where you should do that.
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "FCM Token: " + token);

            // Once a token is generated, we subscribe to topic.
            FirebaseMessaging.getInstance()
                    .subscribeToTopic(DEFAULT_TOPIC);
        }

}
