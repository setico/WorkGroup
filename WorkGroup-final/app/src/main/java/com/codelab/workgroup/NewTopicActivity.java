package com.codelab.workgroup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codelab.workgroup.model.Topic;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class NewTopicActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView topicCover;
    private EditText title;

    private Uri cover_Uri;
    public static int PHOTO_REQUEST = 900;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_topic,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_topic);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        topicCover = (ImageView)findViewById(R.id.cover);
        title = (EditText) findViewById(R.id.title);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    private void save(){
        if(cover_Uri!=null&&!title.getText().toString().trim().isEmpty()){
            final String key =  mFirebaseDatabase.getReference().child("topics").push().getKey();
            StorageReference coverRef = mFirebaseStorage.getReference().child("topics").child(key+".jpg");
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cover_Uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = coverRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mFirebaseDatabase.getReference().child("topics").child(key).setValue(new Topic(key, mFirebaseUser.getDisplayName(), title.getText().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        });

                    }
                });
            }catch (Exception e){

            }



        }else {
            Snackbar.make(getCurrentFocus(),"Veuillez renseigner toutes les informations.",Snackbar.LENGTH_SHORT).show();

        }

    }

    public void addPhoto(View v){
        startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.INTERNAL_CONTENT_URI), PHOTO_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                //Load image
                cover_Uri = data.getData();
                Glide.with(NewTopicActivity.this)
                        .load(cover_Uri)
                        .into(topicCover);
            }catch (Exception e){

            }
        }
    }
}
