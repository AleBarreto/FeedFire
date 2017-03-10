package barreto.alessandro.feedlist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import barreto.alessandro.feedlist.adapter.AdapterListFeed;
import barreto.alessandro.feedlist.model.Feed;

import static barreto.alessandro.feedlist.R.id.fab;

public class MainActivity extends AppCompatActivity implements AdapterListFeed.OnClickItemFeed, View.OnClickListener {

    public static final String FEED_ROOT = "feed";
    public static final String  LIKE_ROOT = "like";
    public static final int GET_PHOTO = 11;

    private RecyclerView recyclerView;
    private ProgressBar mProgressBar;

    private List<Feed> mList;

    private boolean flagLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


    }

    @Override
    protected void onResume() {
        super.onResume();

        retrieveData();

    }

    @Override
    public void onClickItemFeed(int position, View view) {
        Feed feed = mList.get(position);
        switch (view.getId()){
            case R.id.iv_like:
                addLike( feed );
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case fab:
                //startActivity(new Intent(MainActivity.this, AddFeed.class));
                getPhoto();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_PHOTO && resultCode == RESULT_OK) {
            Log.e("File", "" + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));
            sendPhotoFirebase( data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH) );
        }

    }

    private void sendPhotoFirebase(String file){
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("Uploading");
        dialog.show();

        Uri uri = Uri.fromFile(new File(file));
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("image_feed/"+ Calendar.getInstance().getTime()+".jpg");
        reference.putFile( uri ).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(FEED_ROOT);
                String key = databaseReference.push().getKey();

                if (user != null){
                    Feed feed = new Feed();
                    feed.setIdUser( user.getUid() );
                    feed.setName(user.getDisplayName());
                    feed.setPhotoAvatar( user.getPhotoUrl() == null ? "default_uri" : user.getPhotoUrl().toString() );
                    feed.setPhotoFeed( taskSnapshot.getDownloadUrl().toString() );
                    feed.setText(MainActivity.this.getString(R.string.text_2));
                    feed.setTime(Calendar.getInstance().getTimeInMillis());
                    feed.setIdFeed(key);
                    databaseReference.child(key).setValue( feed );
                }

                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                dialog.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        });
    }

    private void getPhoto(){
        new SandriosCamera(MainActivity.this, GET_PHOTO)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .enableImageCropping(true)
                .launchCamera();
    }

    /**
     * Retrieve Data from Firebase
     */
    private void retrieveData(){
        showProgress(true);
        mList = new ArrayList<>();
        DatabaseReference feedReference = FirebaseDatabase.getInstance().getReference().child(FEED_ROOT).orderByChild("time").getRef();
        feedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Feed feed = snapshot.getValue( Feed.class );
                    mList.add(feed);
                }
                initRecyclerView(mList);
                showProgress(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Init data in RecyclerView
     * @param list
     */
    private void initRecyclerView(List<Feed> list){
        recyclerView.setAdapter( new AdapterListFeed(list,this));
    }

    /**
     * Bind views XML with JavaAPI
     */
    private void initViews(){
        mProgressBar = (ProgressBar)findViewById(R.id.pb);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        recyclerView = (RecyclerView)findViewById(R.id.rv_list_feed);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showProgress(boolean b){
        mProgressBar.setVisibility( b ? View.VISIBLE:View.GONE );
    }

    private void addLike(final Feed feed){
        flagLike = true;
        final DatabaseReference referenceLike = FirebaseDatabase.getInstance().getReference().child(LIKE_ROOT);
        final FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        referenceLike.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (flagLike){
                    if (dataSnapshot.child( feed.getIdFeed() ).hasChild( auth.getUid())){
                        referenceLike.child(feed.getIdFeed()).child(auth.getUid()).removeValue();
                        flagLike = false;
                    }else{
                        referenceLike.child(feed.getIdFeed()).child(auth.getUid()).setValue(true);
                        flagLike = false;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
