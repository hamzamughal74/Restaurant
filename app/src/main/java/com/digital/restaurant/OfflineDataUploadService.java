package com.digital.restaurant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class OfflineDataUploadService extends GcmTaskService {

    private ArrayList<String> selectedCategory = new ArrayList<>();
    private String description = null;
    private ArrayList<String> fileUrls = new ArrayList<>();
    private List<Uri> files = new ArrayList<>();
    private StorageReference storageRef;
    private int notificationId = new Random(System.currentTimeMillis()).nextInt();
    private String uid;
    private String fileUrl;
    private int count = 0;
    private String type="";
    private String extension;
    private int currentlyUploading = -1;

    @Override
    public int onRunTask(TaskParams taskParams) {
        OfflineUploadModel model = OfflineUploadModel.getPersisted(this);
        if (model != null) {
            selectedCategory.addAll(model.getSelectedCategory());
            description = model.getDescription();
            fileUrls = model.getFileUrls();
            files = model.getFiles();
            storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://production-digital-attraction.appspot.com/");
            uploadAllFiles();
            generateNotification(notificationId, "uploading data...");
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }



    public void uploadAllFiles() {
        if (currentlyUploading<files.size())
        currentlyUploading++;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        Uri fileToUpload = files.get(count);
         type = mime.getExtensionFromMimeType(getContentResolver()
                .getType(fileToUpload));
        String childDirectory;
        if (type != null && (type.contains("mp4") || type.contains("3gp"))){

            childDirectory = uid + "/data";
            extension = "video";
        }

        else{

            childDirectory = uid + "/data";
            extension = "image";
        }
        String fileName = fileToUpload.getLastPathSegment();
        if (fileName == null)
            fileName = new Random(System.currentTimeMillis()).nextInt() + "";
        final StorageReference filepath = storageRef.child(childDirectory).child(fileName);
        filepath.putFile(fileToUpload).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.getResult() != null)
                    fileUrls.add(task.getResult().toString());
                    fileUrl = task.getResult().toString();
                    createNewNode();
                    if (count<files.size()){

                        uploadAllFiles();
                        count++;
                    }



            }
        });
    }

    private void createNewNode() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss");
        String CurrentDate = simpleDateFormat.format(calendar.getTime());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Node node = new Node(selectedCategory, description, fileUrl,CurrentDate,type,extension);
     //   db.collection("data").add(node).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
        db.collection("restaurant/"+uid+"/content/").add(node).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                OfflineUploadModel.persistForUpload(OfflineDataUploadService.this, null);
                dismissPreviousNotification();
                generateNotification(notificationId + 1, "Data Uploaded Successfully!");
            }
        });
    }

    private void generateNotification(int notificationId, String message) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, SplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notificationId, builder.build());
    }

    private void dismissPreviousNotification() {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(notificationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
