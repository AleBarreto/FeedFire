package barreto.alessandro.feedlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import barreto.alessandro.feedlist.MainActivity;
import barreto.alessandro.feedlist.R;
import barreto.alessandro.feedlist.model.Feed;
import barreto.alessandro.feedlist.view.widget.CustomImageView;

/**
 * Created by Alessandro on 04/03/2017.
 */

public class AdapterListFeed extends RecyclerView.Adapter<AdapterListFeed.MyViewHolder>{

    private List<Feed> mList;
    private OnClickItemFeed onClickItemFeed;

    public AdapterListFeed(List<Feed> mList, OnClickItemFeed onClickItemFeed) {
        this.mList = mList;
        this.onClickItemFeed = onClickItemFeed;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_feed,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Feed feed = mList.get(position);

        holder.setTvName(feed.getName());
        holder.setTvContent( feed.getText() );
        holder.setTvTime( feed.getTime() );
        holder.setIvAvatar( feed.getPhotoAvatar() );
        holder.setIvContent( feed.getPhotoFeed() );

        holder.changeLikeImg( feed.getIdFeed() );


    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivAvatar,ivLike;
        private CustomImageView ivContent;
        private TextView tvName,tvTime,tvContent,tvLike;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            ivAvatar = (ImageView)itemView.findViewById( R.id.iv_avatar );
            tvName = (TextView)itemView.findViewById( R.id.tv_name );
            tvTime = (TextView)itemView.findViewById( R.id.tv_time );
            tvLike = (TextView)itemView.findViewById(R.id.tv_like);
            tvContent = (TextView)itemView.findViewById( R.id.tv_content );
            ivContent = (CustomImageView)itemView.findViewById( R.id.iv_feed );
            ivLike = (ImageView)itemView.findViewById( R.id.iv_like );
            ivLike.setOnClickListener(this);

        }

        public void setIvAvatar(String url){
            if (ivAvatar == null)return;
            if (url.equals("default_uri")){
                Glide.with(ivAvatar.getContext())
                        .load(R.mipmap.ic_launcher)
                        .centerCrop()
                        .transform(new CircleTransform(ivAvatar.getContext()))
                        .override(50,50)
                        .into( ivAvatar );
            }else{
                Glide.with(ivAvatar.getContext())
                        .load( url )
                        .centerCrop()
                        .transform(new CircleTransform(ivAvatar.getContext()))
                        .override(50,50)
                        .into(ivAvatar);
            }
        }

        public void setIvContent(String url){
            if (ivContent == null)return;

            Glide.with(ivContent.getContext()).load(url).centerCrop().into(ivContent);

        }

        public void setTvName(String text){
            if (tvName == null)return;
            tvName.setText( text );
        }

        public void setTvTime(String text){
            if (tvTime == null)return;
        }

        public void setTvContent(String text){
            if (tvContent == null)return;
            tvContent.setText( text );
        }

        public void changeLikeImg(final String feedKey){
            final DatabaseReference referenceLike = FirebaseDatabase.getInstance().getReference().child(MainActivity.LIKE_ROOT);
            final FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
            referenceLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long totalLike = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(feedKey)) {
                            totalLike = snapshot.getChildrenCount();
                            break;
                        }
                    }

                    if (dataSnapshot.child(feedKey).hasChild(auth.getUid())){
                        // ta curtido
                        ivLike.setImageResource( R.drawable.ic_thumb_up_blue_24dp );
                    }else{
                        // nao ta curtido
                        ivLike.setImageResource( R.drawable.ic_thumb_up_grey600_24dp );
                    }
                    tvLike.setText(totalLike+" likes");
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        @Override
        public void onClick(View view) {
            if (onClickItemFeed != null){
                onClickItemFeed.onClickItemFeed(getAdapterPosition(),view);
            }
        }
    }

    /**
     * Click item list
     */
    public interface OnClickItemFeed{
        void onClickItemFeed(int position, View view);
    }

}
