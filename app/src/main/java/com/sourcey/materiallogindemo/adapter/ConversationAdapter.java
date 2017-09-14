package com.sourcey.materiallogindemo.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.activity.MessageActivity;
import com.sourcey.materiallogindemo.util.CircleTransform;
import com.sourcey.materiallogindemo.util.NetworkHelper;
import com.squareup.picasso.Picasso;
import com.yitter.android.entity.Contact;
import com.yitter.android.entity.Conversation;
import com.yitter.android.entity.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    /* ----- GLOBALS ----- */
    private Context mContext;
    private List<Conversation> mContacts;
    private ConversationAdapter adapter;

    /* ----- CONSTRUCTOR ----- */
    public ConversationAdapter(Context context, List<Conversation> conversation) {
        super();

        this.mContacts = conversation;
        this.mContext = context;
        this.adapter = this;
    }


    /* ----- ADAPTER AND VIEW HOLDER CONFIGURATION ----- */
    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationViewHolder(new ConversationItemView(mContext));
    }


    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        bindContactItemView((ConversationItemView) holder.itemView, position);
    }


    @Override
    public int getItemCount() {
        if (mContacts == null) {
            return 0;
        } else {
            return mContacts.size();
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }


    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ConversationItemView conversationItemView;

        ConversationViewHolder(ConversationItemView view) {
            super(view);
            this.conversationItemView = view;
        }
    }


    private void bindContactItemView(ConversationItemView feedItemView, int position) {

        final Conversation conversation = mContacts.get(position);
        final Contact contact = conversation.getContact();

        // Set all Contact content
        setContactContent(feedItemView, conversation, contact);

        // Set click events
        setFeedItemClickListeners(feedItemView, conversation, position);

        // Fade effect when scrolling
        fadeinViews(feedItemView);
    }


    /* ----- BASE CONTACT CONTENT ----- */
    private void setContactContent(final ConversationItemView conversationItemView, final Conversation conversation, Contact contact) {

        // Set previewText
        if (conversation != null) {
            ParseQuery<Message> messageQuery = new ParseQuery<>("Message");
            messageQuery.whereEqualTo("conversationObject", conversation);
            messageQuery.addDescendingOrder("createdAt");
            messageQuery.setLimit(1);
            messageQuery.findInBackground(new FindCallback<Message>() {

                @Override
                public void done(List<Message> messages, ParseException e) {
                    if (e == null) {
                        for (Message message : messages) {
                            conversationItemView.previewText.setText(message.getMessageText());
                        }
                    }
                }

            });
        }

        // Set name
        if (contact.getName() != null) {
            conversationItemView.name.setText(contact.getName());
        }

        // Set badge icon
        if (contact.getBadgeIcon() != null) {
            switch (contact.getBadgeIcon()) {
                case "tinder":
                    conversationItemView.badgeIcon.setImageResource(R.drawable.ic_tinder);
                    break;
                case "bumble":
                    conversationItemView.badgeIcon.setImageResource(R.drawable.ic_bumble);
                    break;
                case "okcupid":
                    conversationItemView.badgeIcon.setImageResource(R.drawable.ic_okcupid);
                    break;
                default:
                    conversationItemView.badgeIcon.setImageResource(R.drawable.ic_star_empty);
                    break;
            }
        }

        // Display attached profile picture
        downloadProfilePicture(conversationItemView, contact);

        Conversation.unpinAllInBackground(mContacts);
        Conversation.pinAllInBackground(mContacts); // Store in local database
    }


    /* ----- CLICK LISTENER ----- */
    private void setFeedItemClickListeners(ConversationItemView feedItemView, final Conversation conversation, final int position) {
        feedItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveMessageActivity(v, conversation);
            }
        });

        feedItemView.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveMessageActivity(v, conversation);
            }
        });

        feedItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                // showDeleteCommentAlertDialog(position, v, contact);
                return false;
            }
        });
    }


    private void showDeleteCommentAlertDialog(final int position, View v, final Contact contact) {
        boolean isOnline = NetworkHelper.isOnline(mContext);
        if (!isOnline) {
            Toast.makeText(mContext, "Cannot remove contacts at this time", Toast.LENGTH_SHORT).show();
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getRootView().getContext());
            alertDialog.setMessage("Do you want to remove this Contact?");
            alertDialog.setTitle("Remove Contact");
            alertDialog.setIcon(R.drawable.ic_menu_contact);

            /*alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    // Show loading indicator
                    final ProgressDialog progressDialog = new ProgressDialog(mContext,
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Removing Contact...");
                    progressDialog.show();

                    // Dismiss loading indicator on successful deletion
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    // On complete call delete contact method
                                    progressDialog.dismiss();
                                    *//*try {
                                        // deleteContact(contact, position);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }*//*
                                    // onLoginFailed();
                                }
                            }, 1000);

                }
            });*/

            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing
                }
            });

            alertDialog.show();

        }
    }


    /*private void deleteContact(Contact contact, int position) throws ParseException {
        contact.unpin();
        contact.delete();
        Toast.makeText(mContext, R.string.contact_deleted, Toast.LENGTH_SHORT).show();
        mContacts.remove(position);
        adapter.notifyItemRemoved(position);
        notifyItemRangeChanged(position, mContacts.size());
        adapter.notifyDataSetChanged();
    }*/


    private void retrieveMessageActivity(View v, Conversation conversation) {
        v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
        Intent messagesIntent = new Intent(mContext, MessageActivity.class);
        System.out.println("Conversation Object Id: " + conversation.getObjectId());
        messagesIntent.putExtra("conversationObjectIdExtra", conversation.getObjectId());
        messagesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(messagesIntent);
    }

    /*Intent i = new Intent();
i.putExtra("name_of_extra", myParcelableObject);*/


    private void downloadProfilePicture(ConversationItemView conversationItemView, Contact contact) {
        if (contact.getProfilePicture() != null) {
            String imageURL = contact.getProfilePicture().getUrl();

            // Asynchronously display the message image downloaded from Parse
            if (imageURL != null) {

                conversationItemView.profilePicture.setVisibility(View.VISIBLE);

                Picasso.with(mContext)
                        .load(imageURL).transform(new CircleTransform())
                        .placeholder(R.color.primary)
                        .into(conversationItemView.profilePicture);

            } else {
                conversationItemView.profilePicture.setVisibility(View.GONE);
            }
        } else {
            conversationItemView.profilePicture.setVisibility(View.GONE);
        }
    }


    /* ----- FADE EFFECT ----- */
    private void fadeinViews(ConversationItemView conversationItemView) {
        Animation animFadeIn;

        animFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fadein);

        conversationItemView.profilePicture.setAnimation(animFadeIn);
        conversationItemView.profilePicture.setVisibility(View.VISIBLE);

        conversationItemView.name.setAnimation(animFadeIn);
        conversationItemView.name.setVisibility(View.VISIBLE);

        conversationItemView.previewText.setAnimation(animFadeIn);
        conversationItemView.previewText.setVisibility(View.VISIBLE);
    }

}
