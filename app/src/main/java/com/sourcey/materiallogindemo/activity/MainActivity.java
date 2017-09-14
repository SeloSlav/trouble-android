package com.sourcey.materiallogindemo.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.adapter.FragmentPagerAdapter;
import com.sourcey.materiallogindemo.authentication.LoginActivity;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set typeface for action bar title
        TextView feedTitleText = (TextView) findViewById(R.id.feed_title);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Allura-Regular.ttf");
        // Set feed title text
        feedTitleText.setTypeface(tf);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // If user is not logged in, redirect to login page
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            redirectLoginActivity();
        }

        // Set up fragment manager
        getFragmentManager();

        // Create navigation drawer
        /*createNavigationDrawer(savedInstanceState, toolbar);*/

        // Set up floating action menu
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_bang:
                        Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_date:
                        Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_contact:
                        Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        break;

                }
                return false;
            }
        });

    }


    public FragmentManager getFragmentManager() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_menu_bang));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_menu_contact));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        final PagerAdapter adapter = new FragmentPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                setFeedTitle(viewPager);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setFeedTitle(viewPager);
            }

        });

        // setFeedTitle(viewPager);

        viewPager.getAdapter().notifyDataSetChanged();

        return null;
    }


    private void setFeedTitle(ViewPager viewPager) {
        // Set typeface for action bar title
        TextView feedTitleText = (TextView) findViewById(R.id.feed_title);

        if (viewPager.getCurrentItem() == 0) {
            // Set feed title typeface
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Allura-Regular.ttf");
            feedTitleText.setTypeface(tf);

            // Set feed title text
            feedTitleText.setText(R.string.app_name);
            feedTitleText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            feedTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        } else if (viewPager.getCurrentItem() == 1) {
            // Set feed title typeface
            Typeface tf = Typeface.DEFAULT;
            feedTitleText.setTypeface(tf);

            // Set feed title text
            feedTitleText.setText(R.string.viewpager_conversations);
            feedTitleText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            feedTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }


    private void redirectLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
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
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
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
    }


    private void onLogoutSuccess() {
        Toast.makeText(getApplicationContext(),
                "Successfully Logged out",
                Toast.LENGTH_LONG).show();

        ParseUser.logOut();

        // Redirect to login activity
        redirectLoginActivity();
    }
}
