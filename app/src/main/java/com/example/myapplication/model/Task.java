package com.example.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {

    private int id;
    private String title;
    private String description;
    private String dueDate;
    private int priority; // 0=Low, 1=Medium, 2=High
    private int status;   // 0=Not done, 1=Done

    // No-arg constructor
    public Task() {
    }

    // Full constructor
    public Task(int id, String title, String description, String dueDate, int priority, int status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
    }

    // Constructor without id (for new tasks)
    public Task(String title, String description, String dueDate, int priority, int status) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
    }

    // ---- Parcelable ----

    protected Task(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        dueDate = in.readString();
        priority = in.readInt();
        status = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(dueDate);
        dest.writeInt(priority);
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    // ---- Getters & Setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // ---- Helpers ----

    public boolean isDone() {
        return status == 1;
    }

    public String getPriorityLabel() {
        switch (priority) {
            case 1: return "Trung bình";
            case 2: return "Cao";
            default: return "Thấp";
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
