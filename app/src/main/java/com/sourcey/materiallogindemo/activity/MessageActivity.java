package com.sourcey.materiallogindemo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.adapter.MessageAdapter;
import com.sourcey.materiallogindemo.authentication.LoginActivity;
import com.sourcey.materiallogindemo.util.CircleTransform;
import com.sourcey.materiallogindemo.util.NetworkHelper;
import com.squareup.picasso.Picasso;
import com.yitter.android.entity.Contact;
import com.yitter.android.entity.Conversation;
import com.yitter.android.entity.Message;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;

public class MessageActivity extends AppCompatActivity {

    /* Globals */
    private static final String TAG = MessageActivity.class.getSimpleName();
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected List<Message> mMessages;
    private MessageAdapter adapter;

    /* Handler Period */
    static final int POLL_INTERVAL = 3000; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Access to network?
        boolean isOnline = NetworkHelper.isOnline(getApplicationContext());

        // Retrieve Conversation object from previous fragment
        Intent i = getIntent();
        final String conversationObjectId = i.getStringExtra("conversationObjectIdExtra");
        System.out.println("Conversation Object Id (Received): " + conversationObjectId);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // If user is not logged in, redirect to login page
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            redirectLoginActivity();
        }

        // Set swipe refresh listener for remote data retrieval
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        refreshData(isOnline, conversationObjectId);
        Date newRefreshDate = new Date();
        retrieveConversation(newRefreshDate, true, conversationObjectId);

        // Create a handler which can run code periodically

        final Handler myHandler = new Handler();  // android.os.Handler
        Runnable mRefreshMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveConversation(new Date(), true, conversationObjectId);
                myHandler.postDelayed(this, POLL_INTERVAL);
            }
        };

        // myHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);

        // Set layout manager
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLinearLayoutManager.setStackFromEnd(true);

        // Set RecyclerView layout and caching
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        // mRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        // Set back button
        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set opener button
        ImageView quickOpenerButton = (ImageView) findViewById(R.id.quickOpenerButton);
        quickOpenerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(v.getContext());

                final String selection[] = new String[]{
                        "Opener 1",
                        "Opener 2",
                        "Opener 3",
                        "Close 1",
                        "Close 2",
                };

                alertDialogBuilder.setItems(selection,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText myEditText = (EditText) findViewById(R.id.EditText);
                                myEditText.getText().clear();
                                // Set edit text message
                                if (which == 0) {
                                    myEditText.setText("Coffee or red wine?");
                                } else if (which == 1) {
                                    myEditText.setText("You're not a huge Beyonce fan, are you?");
                                } else if (which == 2) {
                                    myEditText.setText("You hear about that concert that costs 45 cents?");
                                } else if (which == 3) {
                                    myEditText.setText("What's your phone number?");
                                } else if (which == 4) {
                                    myEditText.setText("Let's do back flips through the jungle together. I'll call you");
                                }
                            }


                        });

                alertDialogBuilder.show();
            }
        });

    }


    private class UpdateTask extends AsyncTask<String, String, String> {
        protected final String doInBackground(final String... strings) {
            Log.e(getClass().getName(), "ASYNC");
            Log.e(getClass().getName(), Arrays.toString(strings));

            // Subscription
            final Subscription sub = new BaseQuery.Builder("Message")
                    .build()
                    .subscribe();

            Log.e(getClass().getName(), String.valueOf(sub.isSubscribed()));

            sub.on(LiveQueryEvent.CREATE, new OnListener() {
                @Override
                public void on(final JSONObject object) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(getClass().toString(), object.toString());
                            // {"op":"create","clientId":13,"requestId":0,"object":{"conversationObject":{"__type":"Pointer","className":"Conversation","objectId":"lZsj4dPCvb"},"messageText":"What's your phone number?","author":{"__type":"Pointer","className":"_User","objectId":"2E0bbd3oSD"},"createdAt":"2017-09-15T15:40:39.303Z","updatedAt":"2017-09-15T15:40:39.303Z","__type":"Object","className":"Message","objectId":"7A4RHttAEr"}}

                            String mJsonString = object.toString();
                            JsonParser parser = new JsonParser();
                            JsonElement mJson =  parser.parse(mJsonString);
                            Gson gson = new Gson();
                            Message newObject = gson.fromJson(mJson, Message.class);

                            // TODO: Take JSON object, convert to GSON, add to mMessages, notify data adapter
                            if (newObject != null) {
                                Log.e(getClass().toString(), "This object is not null!");

                                ParseQuery<Message> obvMessageQuery = new ParseQuery<>(Message.class);
                                obvMessageQuery.whereEqualTo("objectId", "tMfyStFZec");
                                obvMessageQuery.findInBackground(new FindCallback<Message>() {
                                    @Override
                                    public void done(List<Message> messages, ParseException e) {

                                        if (e == null) {
                                            for (Message obvMessage : messages) {
                                                mMessages.add(0, obvMessage);
                                            }
                                        }

                                    }
                                });

                            }

                        }
                    });
                }
            });

            return null;
        }

    }


    private void refreshData(final boolean isOnline, final String conversationObjectId) {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isOnline) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), "No internet connection. Please try again later", Toast.LENGTH_SHORT).show();
                } else {
                    Date newRefreshDate = new Date();
                    retrieveConversation(newRefreshDate, true, conversationObjectId);
                }
            }
        });
    }


    private void retrieveConversation(final Date refreshDate, final boolean refresh, final String conversationObjectId) {

        // System.out.println("Is this being called?");

        ParseQuery<Conversation> conversationQuery = new ParseQuery<>(Conversation.class);
        conversationQuery.whereEqualTo("objectId", conversationObjectId);
        conversationQuery.include("contact");
        conversationQuery.findInBackground(new FindCallback<Conversation>() {

            @Override
            public void done(List<Conversation> conversations, final ParseException e) {

                if (e == null) {

                    for (final Conversation conversation : conversations) {
                        // System.out.println("Queried Conversation Object: " + conversation);

                        // Set Contact ParseObject
                        final Contact contact = conversation.getContact();

                        // Set name
                        TextView contact_name = (TextView) findViewById(R.id.contact_name);
                        if (contact.getName() != null) {
                            contact_name.setText(contact.getName());
                        }

                        // Set badge icon
                        ImageView contact_badgeIcon = (ImageView) findViewById(R.id.contact_badgeIcon);
                        if (contact.getBadgeIcon() != null) {

                            switch (contact.getBadgeIcon()) {
                                case "tinder":
                                    contact_badgeIcon.setImageResource(R.drawable.ic_tinder);
                                    break;
                                case "bumble":
                                    contact_badgeIcon.setImageResource(R.drawable.ic_bumble);
                                    break;
                                case "okcupid":
                                    contact_badgeIcon.setImageResource(R.drawable.ic_okcupid);
                                    break;
                                default:
                                    contact_badgeIcon.setImageResource(R.drawable.ic_star_empty);
                                    break;
                            }
                        }

                        // Set profile picture
                        ImageView contact_profilePicture = (ImageView) findViewById(R.id.contact_profilePicture);
                        if (contact.getProfilePicture() != null) {

                            String imageURL = contact.getProfilePicture().getUrl();

                            // Asynchronously display the message image downloaded from Parse
                            if (imageURL != null) {

                                contact_profilePicture.setVisibility(View.VISIBLE);

                                Picasso.with(getApplicationContext())
                                        .load(imageURL).transform(new CircleTransform())
                                        .placeholder(R.color.primary)
                                        .into(contact_profilePicture);

                            } else {
                                contact_profilePicture.setVisibility(View.GONE);
                            }
                        } else {
                            contact_profilePicture.setVisibility(View.GONE);
                        }

                        // Set click listener for sending message
                        ImageView sendMessageButton = (ImageView) findViewById(R.id.sendMessageButton);
                        sendMessageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                // Create new message
                                Message message = new Message();
                                message.put("author", ParseUser.getCurrentUser());
                                final EditText myEditText = (EditText) findViewById(R.id.EditText);
                                String messageToSend = myEditText.getText().toString();
                                message.put("messageText", messageToSend);
                                message.put("conversationObject", conversation);
                                message.put("conversationObjectId", conversation.getObjectId());

                                message.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        // Retrieve new messages
                                        retrieveMessages(refreshDate, true, conversation);

                                    }
                                });

                                // Clear and drop soft keyboard
                                myEditText.getText().clear();
                                myEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                            }
                        });

                        // Send sample message from Megan
                        ImageView meganProfilePicture = (ImageView) findViewById(R.id.contact_profilePicture);
                        meganProfilePicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));

                                // Create new message
                                final Message newMessage = new Message();

                                // Set Contact value
                                ParseQuery<Contact> contactParseQuery = new ParseQuery<>("Contact");
                                contactParseQuery.whereEqualTo("objectId", conversation.getContact().getObjectId());

                                try {
                                    newMessage.put("contact", contactParseQuery.getFirst());
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                                newMessage.put("messageText", "Hey sweetie!");
                                newMessage.put("conversationObject", conversation);
                                newMessage.put("conversationObjectId", conversation.getObjectId());

                                newMessage.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        // Retrieve new messages
                                        retrieveMessages(refreshDate, true, conversation);
                                    }
                                });

                                // Clear and drop soft keyboard
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            }
                        });

                        // Turn off loading indicator
                        mSwipeRefreshLayout.setRefreshing(false);

                        // Retrieve new messages
                        retrieveMessages(refreshDate, true, conversation);
                    }
                }
            }
        });

    }

    private void retrieveMessages(Date refreshDate, boolean refresh, final Conversation conversation) {
        ParseQuery<Message> messageQuery = new ParseQuery<>(Message.class);
        messageQuery.whereEqualTo("conversationObject", conversation);
        /*if (!refresh) {
            messageQuery.fromLocalDatastore(); // Query from local database on app start-up only
        }
        if (refresh) {
            messageQuery.whereLessThanOrEqualTo("createdAt", refreshDate); // Append only new data to the list when actively requesting remote data
        }*/
        messageQuery.addAscendingOrder("createdAt");
        messageQuery.include("author");
        messageQuery.include("contact");
        messageQuery.setLimit(1000);
        messageQuery.findInBackground(new FindCallback<Message>() {

            @Override
            public void done(List<Message> messages, ParseException e) {

                // Turn off loading indicator
                mSwipeRefreshLayout.setRefreshing(false);

                // If there are no errors, assign Contact objects to adapter
                if (e == null) {

                    adapter = new MessageAdapter(getApplicationContext(), messages);
                    // adapter.setHasStableIds(true);
                    mRecyclerView.setAdapter(adapter);

                    if (mMessages != null) {

                        /*mMessages.clear();
                        mMessages.addAll(messages);*/

                        // Listen for new Message objects and update UI
                        // new UpdateTask().execute();

                        /*ParseQuery<Message> obvMessageQuery = new ParseQuery<>(Message.class);
                        obvMessageQuery.whereEqualTo("objectId", "tMfyStFZec");
                        try {
                            mMessages.add(0, obvMessageQuery.getFirst());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }*/

                        adapter.notifyDataSetChanged();

                    }

                }
            }

        });
    }


    private void redirectLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(),
                    "Settings unavailable right now",
                    Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_logout) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null && currentUser.isAuthenticated()) {

                // Show loading indicator
                final ProgressDialog progressDialog = new ProgressDialog(MessageActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Logging Out...");
                progressDialog.show();

                // Dismiss loading indicator on successful login
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call onLogoutSuccess
                                progressDialog.dismiss();
                                onLogoutSuccess();
                            }
                        }, 1000);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

}
