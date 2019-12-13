package com.digital.restaurant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionMenu moreOptionsMenu;
    private int SELECT_IMAGE = 1234;
    private Button continueBtn;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private DrawerLayout drawerLayout;
    private ConstraintLayout drawer;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Restaurant App";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private Uri currentFileUri = null;
    private ArrayList<Uri> files = new ArrayList<>();
    private PreviewsAdapter previewsAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    String uid;
    public static final int GALLERY_INTENT_CALLED = 1;
    public static final int GALLERY_KITKAT_INTENT_CALLED = 2;

    private FirebaseFirestore db;
    private Task<DocumentSnapshot> note;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        continueBtn = findViewById(R.id.continueBtn);
        initFloatingActionButton();

        previewsAdapter = new PreviewsAdapter(files);
        RecyclerView previewsRv = findViewById(R.id.previewsRv);
        previewsRv.setAdapter(previewsAdapter);
        previewsRv.setLayoutManager(new GridLayoutManager(this, 3));
        previewsRv.addItemDecoration(new PreviewItemDecoration());

        continueBtn.setOnClickListener(this);
        initToolbar();
        findViewById(R.id.logoutBtn).setOnClickListener(this);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
      //  Toast.makeText(getBaseContext(), ""+uid, Toast.LENGTH_SHORT).show();





    }

    private void initToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        drawer = findViewById(R.id.drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mDrawerToggle.syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerToggle.syncState();
            }
        };
        drawerLayout.addDrawerListener(mDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mDrawerToggle.syncState();
    }

    private void initFloatingActionButton() {
        moreOptionsMenu = findViewById(R.id.moreOptionsMenu);
        findViewById(R.id.actionGallery).setOnClickListener(this);
        findViewById(R.id.actionCaptureVideo).setOnClickListener(this);
        findViewById(R.id.actionCaptureImage).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionGallery:
                moreOptionsMenu.close(true);
                invokegal();
                break;
            case R.id.actionCaptureVideo:
                moreOptionsMenu.close(true);
                if (hasCameraPermission())
                    captureVideo();
                else
                    requestCameraPermission(CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
                break;
            case R.id.actionCaptureImage:
                moreOptionsMenu.close(true);
                if (hasCameraPermission())
                    captureImage();
                else
                    requestCameraPermission(CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                break;
            case R.id.continueBtn:
                Intent intent = new Intent(MainActivity.this, MetaActivity.class);
                intent.putParcelableArrayListExtra(MetaActivity.dataPassKey, files);
                startActivity(intent);
                break;
            case R.id.logoutBtn:
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString(LoginScreen.userPersistenceKey, null).apply();
                startActivity(new Intent(MainActivity.this, LoginScreen.class));
                finish();
                break;
        }
    }

    private void invokegal() {


        if (Build.VERSION.SDK_INT <19){
            Intent intent = new Intent();
            intent.setType("image/* video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
         //   startActivityForResult(intent, GALLERY_INTENT_CALLED);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_INTENT_CALLED);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/* video/*");
        //    startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_KITKAT_INTENT_CALLED);
        }

       // old code
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                currentFileUri = data.getData();
                onNewFileAdded();
            } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                onNewFileAdded();
            } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
                onNewFileAdded();
            }
            // new code

            else if (requestCode == GALLERY_INTENT_CALLED) {
                currentFileUri = data.getData();
                onNewFileAdded();
            }

            else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                currentFileUri = data.getData();
                onNewFileAdded();
            }



        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        currentFileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentFileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        currentFileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentFileUri); // set the image file
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerLayout.isDrawerOpen(drawer))
            drawerLayout.openDrawer(drawer);
        else
            drawerLayout.closeDrawer(drawer);
        return super.onOptionsItemSelected(item);
    }

    private void onNewFileAdded() {
        files.add(currentFileUri);
        currentFileUri = null;
        previewsAdapter.notifyDataSetChanged();
        if (files.size() > 0) {
            continueBtn.setEnabled(true);
        } else {
            continueBtn.setEnabled(false);
        }
    }

    private boolean hasCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission(int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasCameraPermission()) {
            if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
                captureVideo();
            } else {
                captureImage();
            }
        } else {
           // Permission denied
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawer))
            drawerLayout.closeDrawer(drawer);
        else
            super.onBackPressed();
    }
}