package com.sourcey.materiallogindemo.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sourcey.materiallogindemo.R;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by @santafebound on 10/25/2015.
 */
public class ConversationItemView extends FrameLayout {

    @Bind(R.id.name)
    TextView name;
    @Bind(R.id.profilePicture)
    ImageView profilePicture;
    @Bind(R.id.badgeIcon)
    ImageView badgeIcon;
    @Bind(R.id.previewText)
    TextView previewText;

    public ConversationItemView(Context context) {
        this(context, null);
    }

    public ConversationItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = inflate(context, R.layout.contacts_listview_item, this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ButterKnife.bind(this, view);
    }
}
