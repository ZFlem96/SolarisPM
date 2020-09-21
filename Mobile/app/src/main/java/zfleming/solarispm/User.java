package zfleming.solarispm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Parcelable, Serializable {

    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String email;
    private ArrayList<String> projects;
    private Boolean projectView = false;


    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String fname) {
//        if (!projectView) {
        firstName = fname;
//        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lname) {
//       if (!projectView)
        lastName = lname;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String uname) {
//        if (!projectView)
        userName = uname;
    }

    public String getPassword() {
        String result = null;
//        if (!projectView) {
        result = password;
//        }
        return result;
    }

    public void setPassword(String pword) {
//        if (!projectView) {
        password = pword;
//        }

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
//        if (!projectView)
        this.email = email;
    }

    public ArrayList<String> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<String> p) {
//        if (!projectView)
        projects = p;
    }

    public Boolean getProjectView() {
        return projectView;
    }

    public void setProjectView(Boolean projectView) {
        this.projectView = projectView;
    }

    public User() {
    }

    public User(String fname, String lname, String pword, String email, String uname) {
        firstName = fname;
        lastName = lname;
        userName = uname;
        password = pword;
        this.email = email;
        projects = new ArrayList<String>();
        projectView = false;
    }

    protected User(Parcel source) {
        firstName = source.readString();
        lastName = source.readString();
        userName = source.readString();
        password = source.readString();
        this.email = source.readString();
//        int size = source.readInt();
//        projects = source.readTypedList(source.readList(new).);
//        for (int x =0;x<size;x++) {
//            projects.add(new Project(source.readParcelable(Project.class.getClassLoader())));
//        }
////        projects = source.readArrayList(Project.CREATOR.createFromParcel(source).lo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeString(password);
//        dest.writeTypedList(projects);
//        for (int x =0;x<projects.size();x++) {
//            Project p = projects.get(x);
//            dest.writeValue(p);
//        }
//        dest.writeInt(projects.size());
    }

    public boolean isEmpty() {
        boolean result = this == null;
        if (!result) {
            if (firstName == null || lastName == null || userName == null || email == null || password == null) {
                result = true;
            } else if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || email.isEmpty() || userName.isEmpty()) {
                result = true;
            }
        }
        return result;
    }

}
