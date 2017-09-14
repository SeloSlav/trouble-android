package com.sourcey.materiallogindemo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.adapter.ConversationAdapter;
import com.sourcey.materiallogindemo.util.NetworkHelper;
import com.yitter.android.entity.Contact;
import com.yitter.android.entity.Conversation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sourcey.materiallogindemo.R.id.recyclerView;


public class ContactFragment extends Fragment {

    private static final String TAG = ContactFragment.class.getSimpleName();
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected List<Conversation> mContacts;
    private ConversationAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Retrieve data from local datastore
        Date initialDate = new Date();
        retrieveData(initialDate, false);

        // Test local datastore
        // testLocalDatastore();

        super.onCreate(savedInstanceState);
    }


    private void testLocalDatastore() {
        ParseQuery<Conversation> query = ParseQuery.getQuery("Contact");
        query.fromLocalDatastore();
        try {
            System.out.println("Local Database Count: " + String.valueOf(query.count()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Access to network?
        boolean isOnline = NetworkHelper.isOnline(getContext());

        // Set view
        View rootView = inflater.inflate(R.layout.tab_fragment_2, container, false);
        rootView.setTag(TAG);

        // Set swipe refresh listener for remote data retrieval
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        refreshData(isOnline);

        // Set layout manager
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Set RecyclerView layout and caching
        mRecyclerView = (RecyclerView) rootView.findViewById(recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        return rootView;
    }


    private void refreshData(final boolean isOnline) {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isOnline) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "No internet connection. Please try again later", Toast.LENGTH_SHORT).show();
                } else {
                    Date newRefreshDate = new Date();
                    retrieveData(newRefreshDate, true);
                }
            }
        });
    }


    private void retrieveData(Date refreshDate, boolean refresh) {
        ParseQuery<Conversation> contactQuery = new ParseQuery<>("Conversation");
        if (!refresh) {
            contactQuery.fromLocalDatastore(); // Query from local database on app start-up only
        }
        if (refresh) {
            contactQuery.whereLessThanOrEqualTo("createdAt", refreshDate); // Append only new data to the list when actively requesting remote data
        }
        contactQuery.addAscendingOrder("name");
        contactQuery.include("contact");
        contactQuery.findInBackground(new FindCallback<Conversation>() {

            @Override
            public void done(List<Conversation> conversations, ParseException e) {
                System.out.println("Conversations: " + conversations);

                // Turn off loading indicator
                mSwipeRefreshLayout.setRefreshing(false);

                // If there are no errors, assign Contact objects to adapter
                if (e == null) {
                    mContacts = new ArrayList<>();
                    adapter = new ConversationAdapter(getContext(), conversations);
                    adapter.setHasStableIds(true);
                    mRecyclerView.setAdapter(adapter);
                }
            }

        });
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
}
