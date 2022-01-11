package com.example.echat.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN= "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderdId";
    public static final String KEY_RECEIVER_ID = "receivedId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTIONS_CONVERSIONS = "conversions";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY  = "availability";
    //for notifications
    public static final String REMOTE_MSG_AUTHORIZATION  = "authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE  = "content-Type";
    public static final String REMOTE_MSG_DATA  = "data";
    public static final String REMOTE_MSG_REGISTRATION  = "registration_ids ";




    public static HashMap<String,String> remoteHsgHeaders = null;
    public static HashMap<String,String> getRemoteHsgHeaders()
    {
        if(remoteHsgHeaders == null)
        {
            remoteHsgHeaders = new HashMap<>();
            remoteHsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,"key=AAAAYK5s-N0:APA91bGJS4XQsfOg8t3vNmKY_Y0ZEoIfQ64dkXUleoSZ9sNYkI3gBjU0UtVZBM15Fvs-xu-e4tN2cDpAcL7R602EYKeA5zF6mCSRE6A6dYATpFQ4BQgKNmAdIc8QjFCXOiK2d1qhwILe"
            );
            remoteHsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,"application.josn"
            );
        }
        return remoteHsgHeaders;
    }
}
