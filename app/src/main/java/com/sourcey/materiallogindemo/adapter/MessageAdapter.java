package com.sourcey.materiallogindemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.util.CircleTransform;
import com.squareup.picasso.Picasso;
import com.yitter.android.entity.Contact;
import com.yitter.android.entity.Message;

import java.util.List;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* ----- GLOBALS ----- */
    private final int ME = 0, THEM = 1;

    private Context mContext;
    private List<Message> mMessages;
    private MessageAdapter adapter;

    /* ----- CONSTRUCTOR ----- */
    public MessageAdapter(Context context, List<Message> messages) {
        super();

        this.mMessages = messages;
        this.mContext = context;
        this.adapter = this;
    }


    /* ----- ADAPTER AND VIEW HOLDER CONFIGURATION ----- */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ME:
                View v1 = inflater.inflate(R.layout.conversation_listview_item_author, parent, false);
                viewHolder = new ViewHolder(v1);
                break;
            case THEM:
                View v2 = inflater.inflate(R.layout.conversation_listview_item_contact, parent, false);
                viewHolder = new ViewHolder2(v2);
                break;
            default:
                View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder = new RecyclerViewSimpleTextViewHolder(v);
                break;
        }
        return viewHolder;
    }


    private void configureDefaultViewHolder(RecyclerViewSimpleTextViewHolder vh, int position) {
        vh.getLabel().setText(String.valueOf(mMessages.get(position)));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case ME:
                ViewHolder vh1 = (ViewHolder) holder;
                configureViewHolder(vh1, position);
                break;
            case THEM:
                ViewHolder2 vh2 = (ViewHolder2) holder;
                configureViewHolder(vh2, position);
                break;
            default:
                RecyclerViewSimpleTextViewHolder vh = (RecyclerViewSimpleTextViewHolder) holder;
                configureDefaultViewHolder(vh, position);
                break;
        }
    }


    @Override
    public int getItemCount() {
        if (mMessages == null) {
            return 0;
        } else {
            return mMessages.size();
        }
    }


    public int getItemViewType(int position) {
        final Message message = mMessages.get(position);
        //noinspection ConstantConditions
        if (message.getAuthor() != null) {
            return ME;
        } else if (message.getContact() != null) {
            return THEM;
        } else {
            return ME;
        }
    }


    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        ViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.messageText);

        }
    }


    private class ViewHolder2 extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView profilePicture;

        ViewHolder2(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.messageText);
            profilePicture = (ImageView) itemView.findViewById(R.id.profilePicture);

        }
    }


    private void configureViewHolder(ViewHolder viewHolder, int position) {

        final Message message = mMessages.get(position);
        final Contact contact = message.getContact();

        // Set all Contact content
        setMessageContent(viewHolder, message);

        // Set click events
        setFeedItemClickListeners(viewHolder);

        // Fade effect when scrolling
        fadeinViews(viewHolder);
    }


    private void configureViewHolder(ViewHolder2 viewHolder2, int position) {

        final Message message = mMessages.get(position);
        final Contact contact = message.getContact();

        // Set all Contact content
        setMessageContent(viewHolder2, message, contact);

        // Set click events
        setFeedItemClickListeners(viewHolder2);

        // Fade effect when scrolling
        fadeinViews(viewHolder2);
    }


    /* ----- BASE CONTACT CONTENT ----- */
    private void setMessageContent(final ViewHolder viewHolder, Message message) {

        // Set previewText
        if (message.getMessageText() != null) {
            viewHolder.messageText.setText(message.getMessageText());
        }

        /*messageItemViewAuthor.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(v.getContext());

                String selection[] = new String[]{
                        "Copy"
                };

                alertDialogBuilder.setItems(selection,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String copiedText = messageItemViewAuthor.messageText.getText().toString();

                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("copied text", copiedText);
                                clipboard.setPrimaryClip(clip);

                                Toast.makeText(mContext, "Copied", Toast.LENGTH_SHORT).show();
                            }
                        });

                alertDialogBuilder.show();

                return false;
            }
        });*/

        Message.unpinAllInBackground(mMessages);
        Message.pinAllInBackground(mMessages); // Store in local database
    }


    private void setMessageContent(final ViewHolder2 viewHolder2, Message message, Contact contact) {

        // Set previewText
        if (message.getMessageText() != null) {
            viewHolder2.messageText.setText(message.getMessageText());
        }

        // Set profile picture
        downloadProfilePicture(viewHolder2, contact);

        /*messageItemViewAuthor.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(v.getContext());

                String selection[] = new String[]{
                        "Copy"
                };

                alertDialogBuilder.setItems(selection,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String copiedText = messageItemViewAuthor.messageText.getText().toString();

                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("copied text", copiedText);
                                clipboard.setPrimaryClip(clip);

                                Toast.makeText(mContext, "Copied", Toast.LENGTH_SHORT).show();
                            }
                        });

                alertDialogBuilder.show();

                return false;
            }
        });*/

        Message.unpinAllInBackground(mMessages);
        Message.pinAllInBackground(mMessages); // Store in local database
    }


    private void downloadProfilePicture(ViewHolder2 messageItemViewAuthor, Contact contact) {
        if (contact.getProfilePicture() != null) {
            String imageURL = contact.getProfilePicture().getUrl();

            // Asynchronously display the message image downloaded from Parse
            if (imageURL != null) {

                messageItemViewAuthor.profilePicture.setVisibility(View.VISIBLE);

                Picasso.with(mContext)
                        .load(imageURL).transform(new CircleTransform())
                        .placeholder(R.color.primary)
                        .into(messageItemViewAuthor.profilePicture);

            } else {
                messageItemViewAuthor.profilePicture.setVisibility(View.GONE);
            }
        } else {
            messageItemViewAuthor.profilePicture.setVisibility(View.GONE);
        }
    }


    /* ----- CLICK LISTENER ----- */
    private void setFeedItemClickListeners(ViewHolder viewHolder) {
        viewHolder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                return false;
            }
        });
    }


    private void setFeedItemClickListeners(ViewHolder2 viewHolder2) {
        viewHolder2.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                return false;
            }
        });
    }


    /* ----- FADE EFFECT ----- */
    private void fadeinViews(ViewHolder messageItemViewAuthor) {
        Animation animFadeIn;

        animFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fadein);

        /*messageItemViewAuthor.profilePicture.setAnimation(animFadeIn);
        messageItemViewAuthor.profilePicture.setVisibility(View.VISIBLE);

        messageItemViewAuthor.name.setAnimation(animFadeIn);
        messageItemViewAuthor.name.setVisibility(View.VISIBLE);*/

        messageItemViewAuthor.messageText.setAnimation(animFadeIn);
        messageItemViewAuthor.messageText.setVisibility(View.VISIBLE);
    }


    private void fadeinViews(ViewHolder2 messageItemViewAuthor) {
        Animation animFadeIn;

        animFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fadein);

        /*messageItemViewAuthor.profilePicture.setAnimation(animFadeIn);
        messageItemViewAuthor.profilePicture.setVisibility(View.VISIBLE);

        messageItemViewAuthor.name.setAnimation(animFadeIn);
        messageItemViewAuthor.name.setVisibility(View.VISIBLE);*/

        messageItemViewAuthor.messageText.setAnimation(animFadeIn);
        messageItemViewAuthor.messageText.setVisibility(View.VISIBLE);
    }

}
