package com.digital.restaurant;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class OfflineUploadModel {

    private static final String persistenceKey = "persistenceKey";

    private ArrayList<String> selectedCategory = new ArrayList<>();
    private String description = null;
    private ArrayList<String> fileUrls = new ArrayList<>();
    private List<String> files = new ArrayList<>();

    public OfflineUploadModel() {
    }

    public OfflineUploadModel(ArrayList<String> selectedCategory, String description, ArrayList<String> fileUrls, List<String> files) {
        this.selectedCategory = selectedCategory;
        this.description = description;
        this.fileUrls = fileUrls;
        this.files = files;
    }


    public ArrayList<String> getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(ArrayList<String> selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(ArrayList<String> fileUrls) {
        this.fileUrls = fileUrls;
    }

    public List<Uri> getFiles() {
        return convertToListOfUris(files);
    }

    public void setFiles(List<Uri> files) {
        this.files = convertToListOfStrings(files);
    }

    public static void persistForUpload(Context context, OfflineUploadModel data) {
        String toPersist = null;
        if (data != null) {
            toPersist = new Gson().toJson(data);
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(persistenceKey, toPersist)
                .apply();
    }

    @Nullable
    public static OfflineUploadModel getPersisted(Context context) {
        String persisted = PreferenceManager.getDefaultSharedPreferences(context).getString(persistenceKey, null);
        if (persisted == null)
            return null;
        return new Gson().fromJson(persisted, OfflineUploadModel.class);
    }

    private List<String> convertToListOfStrings(List<Uri> uris) {
        List<String> toReturn = new ArrayList<>();
        for (Uri uri : uris) {
            toReturn.add(uri.toString());
        }
        return toReturn;
    }

    private List<Uri> convertToListOfUris(List<String> strings) {
        List<Uri> toReturn = new ArrayList<>();
        for (String string : strings) {
            toReturn.add(Uri.parse(string));
        }
        return toReturn;
    }
}
