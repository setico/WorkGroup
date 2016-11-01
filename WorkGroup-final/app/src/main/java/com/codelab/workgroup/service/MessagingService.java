package com.codelab.workgroup.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by setico on 10/31/16.
 */
public class MessagingService extends FirebaseMessagingService {

        private static final String TAG = "FMService";

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            // Handle data payload of FCM messages.
            Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
            Log.d(TAG, "FCM Notification Message: " +
                    remoteMessage.getNotification());
            Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());
        }

}
