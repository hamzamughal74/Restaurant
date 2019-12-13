package com.digital.restaurant;

import java.util.ArrayList;

public class Node {
    private ArrayList<String> category;
    private String description;
    private String data;
    private String creationTime;
    private String extension;
    private String Type;


    public Node() {
    }

    public Node(ArrayList<String> category, String description,
                String data, String creationTime, String extension, String type) {
        this.category = category;
        this.description = description;
        this.data = data;
        this.creationTime = creationTime;
        this.extension = extension;
        Type = type;
    }

    public ArrayList<String> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<String> category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}