package barreto.alessandro.feedlist.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import barreto.alessandro.feedlist.MainActivity;
import barreto.alessandro.feedlist.R;
import barreto.alessandro.feedlist.model.User;
import barreto.alessandro.feedlist.util.FacebookSign;
import barreto.alessandro.feedlist.util.GoogleSign;
import barreto.alessandro.feedlist.util.SharedPrefsUtils;
import in.championswimmer.libsocialbuttons.buttons.BtnFacebook;
import in.championswimmer.libsocialbuttons.buttons.BtnGoogleplus;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleSign.OnInfoLoginGoogleCallback,
        FacebookSign.InfoLoginFaceCallback {

    private static final String USER_ROOT = "user";
    private static final String KEY_LOGIN = "key_login";

    private GoogleSign mGoogleSign;
    private FacebookSign mFacebookSign;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //check preferences login
        if ( SharedPrefsUtils.getBooleanPreference(this,KEY_LOGIN,false) ){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

        initViews();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGoogleSign.onActivityResult(requestCode, resultCode, data); // Google Login
        mFacebookSign.onActivityResult(requestCode, resultCode, data);// Facebook Login
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_google:
                signInGoogle();
                break;
            case R.id.btn_facebook:
                signInFacebook();
                break;
        }
    }


    //[GOOGLE LOGIN START]
    @Override
    public void getInfoLoginGoogle(GoogleSignInAccount account) {
        sendUserFirebase();
    }

    @Override
    public void connectionFailedApiClient(ConnectionResult connectionResult) {
        addProgressBar(false);
        toast( "Error PlayServices cod ="+connectionResult );
    }

    @Override
    public void loginFailed() {
        addProgressBar(false);
        toast( "Login Failed, try again." );
    }
    //[GOOGLE LOGIN END]

    // [FACEBOOK LOGIN START]
    @Override
    public void getInfoFace() {
        sendUserFirebase();
    }

    @Override
    public void cancelLoginFace() {
        addProgressBar(false);
        toast( "Login Failed, try again." );
    }

    @Override
    public void errorLoginFace(FacebookException e) {
        addProgressBar(false);
        toast( "Error Facebook Exception message ="+e.getMessage() );
    }
    //[FACEBOOK LOGIN END]


    /**
     * Bind views XML with JavaAPI
     */
    private void initViews(){
        mFacebookSign = new FacebookSign(this,this);
        mGoogleSign = new GoogleSign(this,this);
        BtnGoogleplus mBtnGoogleplus = (BtnGoogleplus) findViewById(R.id.btn_google);
        BtnFacebook btnFacebook = (BtnFacebook)findViewById(R.id.btn_facebook);;
        mProgressBar = (ProgressBar)findViewById(R.id.pb);
        btnFacebook.setOnClickListener(this);
        mBtnGoogleplus.setOnClickListener(this);
    }

    /**
     * Login with Google
     */
    private void signInGoogle(){
        mGoogleSign.signIn();
        addProgressBar(true);
    }

    /**
     * Login with Google
     */
    private void signInFacebook(){
        mFacebookSign.signIn();
        addProgressBar(true);
    }

    /**
     * Show toast
     * @param mensage
     */
    private void toast(String mensage){
        Toast.makeText(this,mensage,Toast.LENGTH_LONG).show();
    }

    /**
     * Show ProgressBar
     * @param flag
     */
    private void addProgressBar(boolean flag){
        mProgressBar.setVisibility( flag ? View.VISIBLE : View.GONE );
    }

    /**
     * Send user Firebase
     */
    private void sendUserFirebase( ){
        DatabaseReference referenceUser = FirebaseDatabase.getInstance().getReference().child(USER_ROOT);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            User user = new User();
            user.setName( firebaseUser.getDisplayName());
            user.setEmail( firebaseUser.getEmail() );
            user.setPhotoUrl( firebaseUser.getPhotoUrl() == null ? "default_uri" : firebaseUser.getPhotoUrl().toString() );
            user.setuId( firebaseUser.getUid() );
            referenceUser.child( firebaseUser.getUid() ).setValue( user ).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        // open MainActivity
                        SharedPrefsUtils.setBooleanPreference( LoginActivity.this,KEY_LOGIN,true );
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }else{
                        //error to send user firebase
                        toast( "Login Failed Send User, try again." );
                    }
                    addProgressBar(false);
                }
            });
        }
    }
}
