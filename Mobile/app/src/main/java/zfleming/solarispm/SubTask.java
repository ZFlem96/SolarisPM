package zfleming.solarispm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class SubTask implements Parcelable, Serializable {
    private String subTaskName;
    private boolean isSubTaskComplete = false;

    public String getSubTaskName() {
        return subTaskName;
    }

    public void setSubTaskName(String subtaskName) {
        this.subTaskName = subtaskName;
    }

    public boolean isSubTaskComplete() {
        return isSubTaskComplete;
    }

    public void setSubTaskComplete(boolean subTaskComplete) {
        isSubTaskComplete = subTaskComplete;
    }

    public SubTask() {
        isSubTaskComplete = false;
    }


    public SubTask(String name) {
        subTaskName = name;
        isSubTaskComplete = false;
    }

    public boolean isEmpty() {
        boolean result = false;
        if (subTaskName == null) {
            result = true;
        } else if (subTaskName.isEmpty()) {
            result = true;
        }
        return result;
    }

    protected SubTask(Parcel source) {
        subTaskName = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subTaskName);
    }

    public static final Parcelable.Creator<SubTask> CREATOR = new Parcelable.Creator<SubTask>() {
        @Override
        public SubTask createFromParcel(Parcel source) {
            return new SubTask(source);
        }

        @Override
        public SubTask[] newArray(int size) {
            return new SubTask[size];
        }
    };
}