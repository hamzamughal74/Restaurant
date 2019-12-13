package com.digital.restaurant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.core.ServerValues;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.model.value.ServerTimestampValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.grpc.Server;

public class MetaActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String dataPassKey = "dataPassKey";
    private EditText descriptionEt;

    private String selectedCategory = "";
    private String description = null;
    private ArrayList<String> fileUrls = new ArrayList<>();
    private List<Uri> files = new ArrayList<>();
    private  String fileUrl;
    private String type;
    private String extension;
    private  ArrayList<String> catArray;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;
    private String uid;
    private  String childDirectory="";
    private  ArrayList<String>  group= new ArrayList<>();
    ArrayList<String> arrList = new ArrayList<String>();
    private List<String> mList = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta);
        catArray = new ArrayList<>();
        files = getIntent().getParcelableArrayListExtra(dataPassKey);
        initView();



    }

    private void initView() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
       // storageRef = storage.getReferenceFromUrl("gs://newchat-b9188.appspot.com/");
        //gs://production-digital-attraction.appspot.com
        storageRef = storage.getReferenceFromUrl("gs://production-digital-attraction.appspot.com/");
        progressDialog = new ProgressDialog(this);
        Spinner categoriesSpinner = findViewById(R.id.categoriesSpinner);

//        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        FirebaseFirestore.getInstance().collection("restaurant").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                DocumentSnapshot documentSnapshot = task.getResult();
//
//
//
//
//
//
//
//
//            }
//
//
//        });



//        final String[] daysArray = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_item, mList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//         final Spinner sItems = (Spinner) findViewById(R.id.categoriesSpinner);
//        sItems.setAdapter(adapter);
//        adapter.notifyDataSetChanged();


        categoriesSpinner.setOnItemSelectedListener(this);
        descriptionEt = findViewById(R.id.descriptionEt);
        findViewById(R.id.uploadBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.uploadBtn) {
            if (areInputsOkay()) {
                    progressDialog.show();
                if (isNetworkAvailable()) {
                    for (int i = 0 ; i<files.size();i++){

                        uploadAllFiles(i);
                    }
                } else {
                    OfflineUploadModel model = new OfflineUploadModel();
                    model.setSelectedCategory(catArray);
                    model.setDescription(description);
                    model.setFiles(files);
                    model.setFileUrls(fileUrls);
                    OfflineUploadModel.persistForUpload(this, model);
                    new AlertDialog.Builder(this).setTitle("No Internet")
                            .setMessage("Votre appareil semble être hors ligne, vos données seront téléchargées plus tard si vous avez une connexion Internet fonctionnelle.")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    OneoffTask myTask = new OneoffTask.Builder()
                                            .setService(OfflineDataUploadService.class)
                                            .setExecutionWindow(
                                                    10, 60)
                                            .setTag(getString(R.string.app_name))
                                            .build();
                                    GcmNetworkManager.getInstance(MetaActivity.this).schedule(myTask);
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            }).show();
                }
            }
        }
    }

    private int currentlyUploading = 0;

    public void uploadAllFiles(int count) {

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressDialog.setTitle("Uploading File " + (count + 1) + " of " + files.size());
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        Uri fileToUpload = files.get(count);
        type = mime.getExtensionFromMimeType(getContentResolver()
                .getType(fileToUpload));

        if (type != null && (type.contains("mp4") || type.contains("3gp"))) {

            childDirectory = uid + "/data";
            extension = "video";
        }
        else{

            extension = "image";

            childDirectory = uid+"/data";}

        String fileName = fileToUpload.getLastPathSegment();
        if (fileName == null)
            fileName = new Random(System.currentTimeMillis()).nextInt() + "";
        final StorageReference filepath = storageRef.child(childDirectory).child(fileName);
        filepath.putFile(fileToUpload).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Le progrès " + ((int) progress) + " %");
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.getResult() != null)
                    fileUrls.add(task.getResult().toString());
                    fileUrl  = task.getResult().toString();

//                if (++currentlyUploading == files.size()) {
                    createNewNode();
//                } else {
//                    uploadAllFiles();
//                }
            }
        });
    }

    private void createNewNode() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

       Calendar  calendar = Calendar.getInstance();
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss");
       String CurrentDate = simpleDateFormat.format(calendar.getTime());

        Node node = new Node(catArray, description, fileUrl,CurrentDate,"."+type,extension);
     //   db.collection("data").add(node).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

        db.collection("restaurant/"+uid+"/content/").add(node).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
               // Data Uploaded!
                Toast.makeText(MetaActivity.this, "Données téléchargées!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Failed to uploaded
                Toast.makeText(MetaActivity.this, "Échec du téléchargement!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing())
        progressDialog.dismiss();
    }

    private boolean areInputsOkay() {
        if (selectedCategory == null) {
            Toast.makeText(this, "Choisissez une catégorie", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(descriptionEt.getText())) {
            descriptionEt.setError("Champs obligatoires");
            return false;
        }
        description = descriptionEt.getText().toString();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {

            selectedCategory = parent.getItemAtPosition(pos).toString();
          if (!selectedCategory.equals("Select")) {
              if (!catArray.contains(selectedCategory)) {
                  catArray.add(parent.getItemAtPosition(pos).toString());
              }
          }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
