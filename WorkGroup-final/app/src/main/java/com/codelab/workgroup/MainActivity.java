package com.codelab.workgroup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codelab.workgroup.model.Topic;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteApi;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private Toolbar mToolbar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseDatabase mFirebaseDatabase;

    private RecyclerView topicList;
    private FirebaseRecyclerAdapter<Topic,TopicHolder> topicListAdapter;
    private static int REQUEST_INVITE = 903;

    private InterstitialAd mInterstitialAd;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, AuthActivity.class));
                finish();
                return true;
            case R.id.action_invite:
                sendInvitation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppInvite.API)
                .build();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        } else {
            Log.i("State","Connected");
        }

        //initialize firebase database;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Query topicsQuery = mFirebaseDatabase.getReference().child("topics");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("769C96A58CD443AB570924E7F07FF556")
                .build();
        mInterstitialAd.loadAd(adRequest);

        topicList = (RecyclerView) findViewById(R.id.topic_list);
        topicList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        topicListAdapter = new FirebaseRecyclerAdapter<Topic, TopicHolder>(Topic.class, R.layout.topic_items, TopicHolder.class,topicsQuery ) {
            @Override
            protected void populateViewHolder(TopicHolder viewHolder, final Topic model, int position) {
                viewHolder.title.setText(model.getTitle());
                viewHolder.author.setText(model.getUser());
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(MainActivity.this,TopicDetailActivity.class);
                        i.putExtra("key",model.getKey());
                        startActivity(i);
                    }
                });

            }
        };
        topicList.setAdapter(topicListAdapter);
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    public void newTopic(View v){
        startActivity(new Intent(this,NewTopicActivity.class));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent.
                String[] ids = AppInviteInvitation
                        .getInvitationIds(resultCode, data);
                Log.d("MainActivity", "Invitations sent: " + ids.length);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
        super.onBackPressed();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static class TopicHolder extends RecyclerView.ViewHolder{
        public TextView author;
        public TextView title;
        public View view;

        public TopicHolder(View itemView) {
            super(itemView);
            view = itemView;
            author = (TextView)itemView.findViewById(R.id.author);
            title = (TextView)itemView.findViewById(R.id.title);
        }
    }

}
