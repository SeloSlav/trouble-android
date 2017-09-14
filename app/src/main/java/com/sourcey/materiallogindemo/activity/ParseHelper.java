package com.sourcey.materiallogindemo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.parse.ParseUser;
import com.sourcey.materiallogindemo.R;
import com.sourcey.materiallogindemo.authentication.LoginActivity;

class ParseHelper {

    void logOut(final Context context, ParseUser currentUser) {
        if (currentUser != null && currentUser.isAuthenticated()) {

            // Show loading indicator
            final ProgressDialog progressDialog = new ProgressDialog(null,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging Out...");
            progressDialog.show();

            // Dismiss loading indicator on successful login
            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call onLogoutSuccess
                            progressDialog.dismiss();
                            onLogoutSuccess(context);
                        }
                    }, 1000);
        }
    }

    private void onLogoutSuccess(Context context) {
        Toast.makeText(context,
                "Successfully Logged out",
                Toast.LENGTH_LONG).show();

        ParseUser.logOut();

        // Redirect to login activity
        redirectLoginActivity(context);
    }

    private void redirectLoginActivity(Context context) {
        Intent intent = new Intent(null, LoginActivity.class);
        context.startActivity(intent);
    }
}