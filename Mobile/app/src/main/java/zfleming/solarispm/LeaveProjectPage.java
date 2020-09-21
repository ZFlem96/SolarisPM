package zfleming.solarispm;

import android.app.Notification;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LeaveProjectPage extends AppCompatActivity {
    private User mainUser;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private ListView projectList;
    private ArrayList<String> selectedProjects, projects, mainUserProjectIDs;
    private ArrayList<Project> mainUserProjects;
    private ListAdapter arrayAdapter;
    private String mainUserID;
    private HashMap<String, Project> allUserProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaveproject);
        selectedProjects = new ArrayList<String>();
        mainUser = getIntent().getParcelableExtra("Main User");
        mainUserProjectIDs = new ArrayList<>();
        mainUserProjects = new ArrayList<>();
        allUserProject = new HashMap<>();
        if (mainUser != null) {
            ArrayList<Project> projects = (ArrayList<Project>) getIntent().getSerializableExtra("User projects");
            if (projects != null) {
                if (!projects.isEmpty()) {
                    mainUser.setProjects((ArrayList<String>) getIntent().getSerializableExtra("User projects"));
                }
            } else {
                mainUser.setProjects(new ArrayList<String>());
            }
        }
        fAuth = FirebaseAuth.getInstance();
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                if (fUser == null) {
                    fAuth.signInWithEmailAndPassword(mainUser.getEmail(), mainUser.getPassword());
                    if (mainUser == null) {
                        fUser = FirebaseAuth.getInstance().getCurrentUser();
                        final ArrayList<User> existingUsers = new ArrayList<>();
                        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                        Query reference = root.child("Users");
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    existingUsers.add(postSnapshot.getValue(User.class));
                                    User u = postSnapshot.getValue(User.class);
                                    if (u.getUserName().equalsIgnoreCase(mainUser.getUserName())) {
                                        mainUserID = postSnapshot.getKey().toString();
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        User user = null;
                        for (int x = 0; x < existingUsers.size(); x++) {
                            if (fUser.getEmail().equalsIgnoreCase(existingUsers.get(x).getEmail())) {
                                user = existingUsers.get(x);
                            }
                        }
                        mainUser = user;
                    }
//                        Intent i = getBaseContext().getPackageManager()
//                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);
//                    }
                } else {
                    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                    Query reference = root.child("Users");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                User u = postSnapshot.getValue(User.class);
                                if (u.getUserName().equalsIgnoreCase(mainUser.getUserName())) {
                                    mainUserID = postSnapshot.getKey().toString();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    reference = root.child("Projects");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Project p = postSnapshot.getValue(Project.class);
                                String id = postSnapshot.getKey().toString();
                                for (int x = 0; x < mainUser.getProjects().size(); x++) {
                                    if (mainUser.getProjects().get(x).equalsIgnoreCase(p.getProjectName())) {
                                        mainUserProjectIDs.add(id);
                                        mainUserProjects.add(p);
                                        allUserProject.put(id, p);

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

        };
        projects = new ArrayList<String>();
        for (int x = 0; x < mainUser.getProjects().size(); x++) {
            String p = mainUser.getProjects().get(x);
            projects.add(p);
        }
        arrayAdapter = new ArrayAdapter<String>(
                LeaveProjectPage.this,
                android.R.layout.simple_list_item_1,
                projects);
        projectList = (ListView) findViewById(R.id.projectList);
        projectList.setAdapter(arrayAdapter);
        projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean value = view.isSelected();
                String projectName = parent.getItemAtPosition(position).toString();
                if (value) {
                    value = false;
                    int index = -1;
                    for (int x = 0; x < selectedProjects.size(); x++) {
                        String s = selectedProjects.get(x);
                        if (projectName.equalsIgnoreCase(s)) {
                            index = x;
                        }
                    }
                    selectedProjects.remove(index);
                } else {
                    value = true;
                    selectedProjects.add(projectName);
                }
                view.setSelected(value);
            }
        });
    }

    public void LeaveProjects(View view) {
        if (selectedProjects.size() > 0) {
            ArrayList<String> savedProjects = new ArrayList<>();
            boolean isSelected = false;
            for (int x = 0; x < mainUser.getProjects().size(); x++) {
                String pname = mainUser.getProjects().get(x);
                for (int y = 0; y < selectedProjects.size(); y++) {
                    if (selectedProjects.get(y).equalsIgnoreCase(pname)) {
                        isSelected = true;
                    }
                }
                if (!isSelected) {
                    savedProjects.add(pname);
                } else {
                    isSelected = false;
                }
            }
            mainUser.setProjects(savedProjects);
            if (mainUser.getProjects().isEmpty()) {
                mainUser.getProjects().add("Dummy");
            }
            HashMap<String, Project> alterSelectedProjects = new HashMap<>();
            final ArrayList<String> projectIDs = new ArrayList<>();
            final ArrayList<Project> alteredProjects = new ArrayList<>();
            ArrayList<Integer> indexes = new ArrayList<>();
            for (int x = 0; x < selectedProjects.size(); x++) {
                for (int y = 0; y < mainUserProjects.size(); y++) {
                    if (selectedProjects.get(x).equalsIgnoreCase(mainUserProjects.get(y).getProjectName())) {
                        indexes.add(y);
                    }
                }
            }
            for (int x = 0; x < selectedProjects.size(); x++) {
                projectIDs.add(mainUserProjectIDs.get(indexes.get(x)));
//                alteredProjects.add(mainUserProjects.get(x));
            }
            for (int x = 0; x < projectIDs.size(); x++) {
                Project p = allUserProject.get(projectIDs.get(x));
                for (int y = 0; y < p.getAssignedUsers().size(); y++) {
                    String u = p.getAssignedUsers().get(y);
                    if (u.equalsIgnoreCase(mainUser.getUserName())) {
                        p.getAssignedUsers().remove(y);
                    }
                }
                alterSelectedProjects.put(projectIDs.get(x), p);
            }

            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference;
            for (int x = 0; x < projectIDs.size(); x++) {
                reference = root.child("Projects");
                Project p = alterSelectedProjects.get(projectIDs.get(x));
                if (p.getAssignedUsers().isEmpty()) {
                    reference.getRef().child(projectIDs.get(x)).setValue(null);
                } else {
                    reference.getRef().child(projectIDs.get(x)).child("assignedUsers").setValue(p.getAssignedUsers());
                }
            }

            reference = root.child("Users").child(mainUserID);
            reference.getRef().child("projects").setValue(mainUser.getProjects());
            Toast.makeText(LeaveProjectPage.this, "Left Selected Projects.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        fAuth.removeAuthStateListener(fAuthListener);
    }
}
