package barreto.alessandro.feedlist.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Alessandro on 05/03/2017.
 */

public class FacebookSign {

    private FragmentActivity mActivity;
    private CallbackManager mCallbackManager;
    private InfoLoginFaceCallback mFaceCallback;

    public FacebookSign(FragmentActivity mActivity, InfoLoginFaceCallback mFaceCallback) {
        this.mActivity = mActivity;
        this.mFaceCallback = mFaceCallback;
        mCallbackManager = CallbackManager.Factory.create();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data){
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    public void signIn(){
        List<String> permissionNeeds= Arrays.asList("email", "public_profile");
        LoginManager.getInstance().logInWithReadPermissions(
                mActivity,
                permissionNeeds);
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResults) {
                        handleFacebookAccessToken(loginResults.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        if (mFaceCallback != null){
                            mFaceCallback.cancelLoginFace();
                        }
                    }

                    @Override
                    public void onError(FacebookException e) {
                        if (mFaceCallback != null){
                            mFaceCallback.errorLoginFace( e );
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            mFaceCallback.cancelLoginFace();
                        }else{
                           mFaceCallback.getInfoFace();
                        }
                    }
                });
    }

    public interface InfoLoginFaceCallback {
        void getInfoFace();
        void cancelLoginFace();
        void errorLoginFace(FacebookException e);
    }

    public static void keyHash(Context context){

        // Add code to print out the key hash
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "barreto.alessandro.feedlist",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {
            Log.e("Facebook",ignored.getMessage());
        }

    }

}
