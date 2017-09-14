package com.sourcey.materiallogindemo.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.adapter.ConversationAdapter;
import com.sourcey.materiallogindemo.adapter.FragmentPagerAdapter;
import com.sourcey.materiallogindemo.adapter.MessageAdapter;
import com.sourcey.materiallogindemo.authentication.LoginActivity;
import com.sourcey.materiallogindemo.fragment.ContactFragment;
import com.sourcey.materiallogindemo.util.CircleTransform;
import com.sourcey.materiallogindemo.util.NetworkHelper;
import com.squareup.picasso.Picasso;
import com.yitter.android.entity.Contact;
import com.yitter.android.entity.Conversation;
import com.yitter.android.entity.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static com.sourcey.materiallogindemo.R.id.recyclerView;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = MessageActivity.class.getSimpleName();
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected List<Message> mMessages;
    private MessageAdapter adapter;

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

        System.out.println("Is this being called?");

        ParseQuery<Conversation> conversationQuery = new ParseQuery<>("Conversation");
        conversationQuery.whereEqualTo("objectId", conversationObjectId);
        conversationQuery.findInBackground(new FindCallback<Conversation>() {

            @Override
            public void done(List<Conversation> conversations, final ParseException e) {

                if (e == null) {

                    for (final Conversation conversation : conversations) {
                        System.out.println("Queried Conversation Object: " + conversation);

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
                            public void onClick(View v) {
                                // Create new message
                                Message message = new Message();
                                message.put("author", ParseUser.getCurrentUser());
                                final EditText myEditText = (EditText) findViewById(R.id.EditText);
                                String messageToSend = myEditText.getText().toString();
                                message.put("messageText", messageToSend);
                                message.put("conversationObject", conversation);

                                try {
                                    // Save message to remote server
                                    message.save();

                                    // Clear and rop soft keyboard
                                    myEditText.getText().clear();
                                    myEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                    // Retrieve newly posted message
                                    retrieveConversation(new Date(), true, conversationObjectId);


                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });

                        // Turn off loading indicator
                        mSwipeRefreshLayout.setRefreshing(false);

                        // If there are no errors, assign Contact objects to adapter
                        retrieveMessages(refreshDate, refresh, conversation);
                    }
                }
            }
        });

    }

    private void retrieveMessages(Date refreshDate, boolean refresh, Conversation conversation) {
        ParseQuery<Message> messageQuery = new ParseQuery<>("Message");
        messageQuery.whereEqualTo("conversationObject", conversation);
        if (!refresh) {
            messageQuery.fromLocalDatastore(); // Query from local database on app start-up only
        }
        if (refresh) {
            messageQuery.whereLessThanOrEqualTo("createdAt", refreshDate); // Append only new data to the list when actively requesting remote data
        }
        messageQuery.addAscendingOrder("createdAt");
        messageQuery.include("author");
        messageQuery.include("contact");
        messageQuery.findInBackground(new FindCallback<Message>() {

            @Override
            public void done(List<Message> messages, ParseException e) {
                System.out.println("Messages: " + messages);

                // Turn off loading indicator
                mSwipeRefreshLayout.setRefreshing(false);

                // If there are no errors, assign Contact objects to adapter
                if (e == null) {
                    mMessages = new ArrayList<>();
                    adapter = new MessageAdapter(getApplicationContext(), messages);
                    adapter.setHasStableIds(true);
                    mRecyclerView.setAdapter(adapter);

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
