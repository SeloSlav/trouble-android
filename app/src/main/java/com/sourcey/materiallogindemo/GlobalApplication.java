package com.sourcey.materiallogindemo;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.yitter.android.entity.Contact;
import com.yitter.android.entity.Conversation;
import com.yitter.android.entity.Message;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class GlobalApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();

        ParseObject.registerSubclass(Contact.class);
        ParseObject.registerSubclass(Conversation.class);
        ParseObject.registerSubclass(Message.class);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(getString(R.string.parse_app_id))
                .clientKey(getString(R.string.parse_client_key))
                .server("https://parseapi.back4app.com/")
                .enableLocalDataStore()
                .build()
        );

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

    }

}
