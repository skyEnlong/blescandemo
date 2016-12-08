package com.example.enlong.blescandemo;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by enlong on 2016/11/14.
 */
public class AccountAuthenticatorService extends Service{

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
        CLog.e("en_long", " AccountAuthenticatorService service onCreate ");

    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.e("en_long", " AccountAuthenticatorService service onDestroy ");

    }

    /**
     * Created by enlong on 2016/11/14.
     */

    public static class Authenticator  extends AbstractAccountAuthenticator {

        public Authenticator(Context context) {
            super(context);
            Log.i("enlong", "Authenticator");
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            Log.i("enlong", "editProperties:" + s);
            Bundle bundle = new Bundle();
            return bundle;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
            Log.i("enlong", "addAccount:" + s +"__" + s1);

            return bundle;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            Log.i("enlong", "confirmCredentials:" + accountAuthenticatorResponse);
            return bundle;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            Log.i("enlong", "getAuthToken:" + accountAuthenticatorResponse);


            return bundle;
        }

        @Override
        public String getAuthTokenLabel(String s) {
            Log.i("enlong", "getAuthTokenLabel:" + s);

            return s;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            Log.i("enlong", "updateCredentials:" + accountAuthenticatorResponse);

            return bundle;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            Log.i("enlong", "hasFeatures: " + accountAuthenticatorResponse);
            Bundle bundle = new Bundle();

            return bundle;
        }
    }
}
