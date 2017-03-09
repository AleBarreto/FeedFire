package barreto.alessandro.feedlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import barreto.alessandro.feedlist.adapter.AdapterListFeed;
import barreto.alessandro.feedlist.model.Feed;
import barreto.alessandro.feedlist.view.activity.AddFeed;

import static barreto.alessandro.feedlist.R.id.fab;

public class MainActivity extends AppCompatActivity implements AdapterListFeed.OnClickItemFeed, View.OnClickListener {

    public static final String FEED_ROOT = "feed";
    public static final String  LIKE_ROOT = "like";

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

    /**
     * Retrieve Data from Firebase
     */
    private void retrieveData(){
        showProgress(true);
        mList = new ArrayList<>();
        DatabaseReference feedReference = FirebaseDatabase.getInstance().getReference().child(FEED_ROOT);
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
                startActivity(new Intent(MainActivity.this, AddFeed.class));
                break;
        }
    }


}
