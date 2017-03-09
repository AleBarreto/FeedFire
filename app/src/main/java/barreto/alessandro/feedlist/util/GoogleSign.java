package barreto.alessandro.feedlist.util;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import barreto.alessandro.feedlist.R;

/**
 * Created by Alessandro on 04/03/2017.
 */

public class GoogleSign {

    private static final int RC_SIGN_IN = 10;

    private GoogleApiClient mGoogleApiClient;
    private FragmentActivity context;
    private OnInfoLoginGoogleCallback mGoogleCallback;

    public GoogleSign(FragmentActivity context, OnInfoLoginGoogleCallback mGoogleCallback) {
        this.context = context;
        this.mGoogleCallback = mGoogleCallback;
        getConfigDefaultLogin();
    }

    private void getConfigDefaultLogin(){
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(context /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        mGoogleCallback.connectionFailedApiClient( connectionResult );
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    // [START onactivityresult]
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                mGoogleCallback.loginFailed();
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START signin]
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        context.startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            mGoogleCallback.loginFailed();
                        }else{
                            mGoogleCallback.getInfoLoginGoogle(acct);
                        }
                    }
                });
    }
    // [END auth_with_google]

    public interface OnInfoLoginGoogleCallback{
        void getInfoLoginGoogle(GoogleSignInAccount account);
        void connectionFailedApiClient(ConnectionResult connectionResult);
        void loginFailed();
    }


}
