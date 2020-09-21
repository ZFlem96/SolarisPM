package zfleming.solarispm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Project implements Parcelable, Serializable {

    private String projectName;
    private String leader;
    private ArrayList<String> assignedUsers;
    private ArrayList<Task> projectTasks;
    private boolean isComplete;
    private float percentage;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public ArrayList<String> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(ArrayList<String> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public ArrayList<Task> getProjectTasks() {
        return projectTasks;
    }

    public void setProjectTasks(ArrayList<Task> projectTasks) {
        this.projectTasks = projectTasks;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        if (percentage == 100) {
            isComplete = true;
        }
    }

    public Project() {
        isComplete = false;
        percentage = 0;
    }

    public Project(String name, String leader, ArrayList<String> assignedUsers) {
        projectName = name;
        this.leader = leader;
        this.assignedUsers = assignedUsers;
        projectTasks = new ArrayList<Task>();
        isComplete = false;
        percentage = 0;
    }

    protected Project(Parcel source) {
        projectName = source.readString();
        leader = source.readString();
        percentage = source.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(projectName);
        dest.writeString(leader);
        dest.writeFloat(percentage);
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel source) {
            return new Project(source);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    public boolean isEmpty() {
        boolean result = this == null;
        if (!result) {
            if (projectName == null || leader == null || assignedUsers == null) {
                result = true;
            } else if (projectName.isEmpty() || leader.isEmpty() || assignedUsers.isEmpty()) {
                result = true;
            }
        }
        return result;
    }
}
