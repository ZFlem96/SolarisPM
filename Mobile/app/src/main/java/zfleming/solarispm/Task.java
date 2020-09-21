package zfleming.solarispm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Parcelable, Serializable {
    private String taskName;
    private ArrayList<SubTask> subTask;
    private ArrayList<String> assignedUsers;
    private boolean isComplete;
    private int percentage;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public ArrayList<SubTask> getSubTask() {
        return subTask;
    }

    public void setSubTask(ArrayList<SubTask> subTask) {
        this.subTask = subTask;
    }

    public ArrayList<String> getAssignedUsers() {
        return assignedUsers;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public void setAssignedUsers(ArrayList<String> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Task() {
        isComplete = false;
        percentage = 0;
    }

    public Task(String name, ArrayList<String> assignedUsers) {
        taskName = name;
        subTask = new ArrayList<SubTask>();
        this.assignedUsers = assignedUsers;
        isComplete = false;
    }

    protected Task(Parcel source) {
        taskName = source.readString();
        percentage = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskName);
        dest.writeInt(percentage);
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public boolean isEmpty() {
        boolean result = this == null;
        if (!result) {
            if (taskName == null || assignedUsers == null) {
                result = true;
            } else if (taskName.isEmpty() || assignedUsers.isEmpty()) {
                result = true;
            }
        }
        return result;
    }


}

