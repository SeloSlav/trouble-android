package com.sourcey.materiallogindemo.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sourcey.materiallogindemo.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by @santafebound on 10/25/2015.
 */
public class MessageItemViewAuthor extends FrameLayout {

    @Bind(R.id.messageText)
    TextView messageText;

    public MessageItemViewAuthor(Context context) {
        this(context, null);
    }

    public MessageItemViewAuthor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageItemViewAuthor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = inflate(context, R.layout.conversation_listview_item_author, this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ButterKnife.bind(this, view);
    }
}
