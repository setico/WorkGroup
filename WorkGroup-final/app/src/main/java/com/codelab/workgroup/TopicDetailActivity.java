package com.codelab.workgroup;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codelab.workgroup.model.Comment;
import com.codelab.workgroup.model.Topic;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class TopicDetailActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    private RecyclerView commentList;
    private FirebaseRecyclerAdapter<Comment,CommentHolder> commentListAdapter;
    private String key;
    private EditText comment;
    private ImageView topicCover;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        key = getIntent().getStringExtra("key");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //initialize firebase database;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Query commentsQuery = mFirebaseDatabase.getReference().child("comments").child(key);

        mFirebaseDatabase.getReference().child("topics").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Topic topic = dataSnapshot.getValue(Topic.class);
                getSupportActionBar().setTitle(topic.getTitle());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        topicCover = (ImageView) findViewById(R.id.topic_cover);
        comment = (EditText) findViewById(R.id.comment);
        commentList = (RecyclerView) findViewById(R.id.comment_list);
        commentList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        commentListAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(Comment.class, R.layout.topic_details_items, CommentHolder.class,commentsQuery ) {
            @Override
            protected void populateViewHolder(CommentHolder viewHolder, Comment model, int position) {
                viewHolder.comment.setText(model.getComment());
                viewHolder.userName.setText(model.getUser());
                Glide
                        .with(TopicDetailActivity.this)
                        .load(model.getuserPhoto())
                        .into(viewHolder.userPhoto);

            }
        };
        commentList.setAdapter(commentListAdapter);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference coverRef = mFirebaseStorage.getReference().child("topics").child(key+".jpg");
        coverRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide
                        .with(TopicDetailActivity.this)
                        .load(uri)
                        .into(topicCover);

            }
        });

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("comment_length", 5L);

        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // Fetch remote config.
        fetchConfig();


    }

    public void sendComment(View v){
        String commentStr = comment.getText().toString();
        if(!commentStr.trim().isEmpty()){
            mFirebaseDatabase.getReference().child("comments").child(key).push().setValue(new Comment(mFirebaseUser.getDisplayName(),mFirebaseUser.getPhotoUrl().toString(),commentStr)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    comment.setText("");
                }
            });
        }
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 0; // because we are in developer mode so that
        // each fetch goes to the server.

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via
                        // FirebaseRemoteConfig get<type> calls.
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                });
    }


    /**
     * Apply retrieved length limit to edit text field.
     * This result may be fresh from the server or it may be from cached
     * values.
     */
    private void applyRetrievedLengthLimit() {
        Long comment_length =
                mFirebaseRemoteConfig.getLong("comment_length");
        comment.setFilters(new InputFilter[]{new
                InputFilter.LengthFilter(comment_length.intValue())});
    }


    public static class CommentHolder extends RecyclerView.ViewHolder{
        public TextView userName;
        public ImageView userPhoto;
        public TextView comment;


        public CommentHolder(View itemView) {
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_photo);
            userName = (TextView) itemView.findViewById(R.id.user_name);
        }
    }


}
