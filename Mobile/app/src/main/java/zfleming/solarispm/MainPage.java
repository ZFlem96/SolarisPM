package zfleming.solarispm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MainPage extends AppCompatActivity {

    private User mainUser;
    private TextView title;
    private ListView listView;
    private ArrayList<String> projects;
    private ArrayAdapter adapter;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private ArrayList<Project> userProjects;
    private ArrayList<String> projectIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectIDs = new ArrayList<>();
        setContentView(R.layout.activity_mainpage);
        if (title == null) {
            title = (TextView) findViewById(R.id.welcome);
        }
        userProjects = new ArrayList<>();
        listView = (ListView) findViewById(R.id.projectList);
        if (mainUser == null) {
            mainUser = getIntent().getParcelableExtra("Main User");
            if (mainUser != null) {
                ArrayList<String> projects = (ArrayList<String>) getIntent().getSerializableExtra("User projects");
                if (projects != null) {
                    if (projects != null) {
                        if (!projects.isEmpty()) {
                            if (!projects.get(0).equalsIgnoreCase("dummy")) {
                                mainUser.setProjects((ArrayList<String>) getIntent().getSerializableExtra("User projects"));
                            }
                        }
                    } else {
                        mainUser.setProjects(new ArrayList<String>());
                    }
                } else {
                    mainUser.setProjects(new ArrayList<String>());
                }
            }
        }
        fAuth = FirebaseAuth.getInstance();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (fUser == null) {
                    if (mainUser.isEmpty()) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        fAuth.signInWithEmailAndPassword(mainUser.getEmail(), mainUser.getPassword());
                        fUser = fAuth.getCurrentUser();
                    }
                }
            }
        };
        if (mainUser.isEmpty()) {
            User user = null;
            final ArrayList<User> existingUsers = new ArrayList<>();
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference = root.child("Users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                                existingUsers.add(postSnapshot.getValue(User.class));
                        User u = postSnapshot.getValue(User.class);
                        if (fUser.getEmail().equalsIgnoreCase(u.getEmail())) {
                            mainUser = u;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference = root.child("Projects");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Project p = postSnapshot.getValue(Project.class);
                        String id = postSnapshot.getKey().toString();
                        for (int x = 0; x < mainUser.getProjects().size(); x++) {
                            if (mainUser.getProjects().get(x).equalsIgnoreCase(p.getProjectName())) {
                               projectIDs.add(id);
                                p.setProjectTasks(new ArrayList<zfleming.solarispm.Task>());
                                for (DataSnapshot taskShot : dataSnapshot.child(id).child("tasks").getChildren()){
                                    zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
                                    p.getProjectTasks().add(t);
                                }
                                userProjects.add(p);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        title.setText("Welcome " + mainUser.getUserName() + "!");
        projects = mainUser.getProjects();
        if (projects != null) {
            if (projects.isEmpty()) {
                listView.setVisibility(View.INVISIBLE);
            } else if (projects.get(0).equalsIgnoreCase("dummy")) {
                listView.setVisibility(View.INVISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                adapter = new ArrayAdapter(MainPage.this, android.R.layout.simple_list_item_1, projects);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int index = -1;
                        String projectName = parent.getItemAtPosition(position).toString();
//                        for (int x = 0; x < mainUser.getProjects().size(); x++) {
//                            String s = mainUser.getProjects().get(x);
//                            if (projectName.equalsIgnoreCase(s)) {
//                                index = x;
//                            }
//                        }
                        for (int x = 0; x < userProjects.size(); x++) {
                            if (projectName.equalsIgnoreCase(userProjects.get(x).getProjectName())) {
                                index = x;
                            }
                        }

                        Intent intent = new Intent(MainPage.this, ProjectPage.class);
                        intent.putExtra("Main User", (Parcelable) mainUser);
                        intent.putExtra("User projects", mainUser.getProjects());
                        intent.putExtra("Main Project", (Parcelable) userProjects.get(index));
                        intent.putExtra("Project ID", projectIDs.get(index));
                        intent.putExtra("Project Tasks", (Parcelable) userProjects.get(index).getProjectTasks());
                        intent.putExtra("Assigned Users", (Parcelable) userProjects.get(index).getAssignedUsers());
                        //  intent.putExtra("Project Users", (Serializable) mainUser.getProjects().get(index).getAssignedUsers());
                        //  intent.putExtra("Project Tasks", (Serializable) mainUser.getProjects().get(index).getProjectTasks());
                        startActivityForResult(intent,0);
                    }
                });
            }
        }


    }




    public void addNewProject(View view) {
        Intent intent = new Intent(MainPage.this, AddANewProject.class);
        intent.putExtra("Main User", (Parcelable) mainUser);
        intent.putExtra("User projects", mainUser.getProjects());
        startActivity(intent);
    }

    public void goToAccount(View view) {
        Intent intent = new Intent(MainPage.this, UserPage.class);
        intent.putExtra("Main User", (Parcelable) mainUser);
        intent.putExtra("User projects", mainUser.getProjects());
        startActivity(intent);
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
        setResult(RESULT_OK,getIntent());
    }

    @Override
    public void finish() {
        super.finish();
        setResult(RESULT_OK,getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final ArrayList<User> existingUsers = new ArrayList<>();
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query reference = root.child("Users");
        if (fUser == null) {
            fAuth.signInWithEmailAndPassword(mainUser.getEmail(), mainUser.getPassword());
//                        Intent i = getBaseContext().getPackageManager()
//                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);
//                    }
        }
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User u = postSnapshot.getValue(User.class);
                    if (u.getUserName().equalsIgnoreCase(mainUser.getUserName())) {
                        mainUser = u;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userProjects = new ArrayList<>();
        reference = root.child("Projects");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Project p = postSnapshot.getValue(Project.class);
                    String id = postSnapshot.getKey().toString();
                    for (int x = 0; x < mainUser.getProjects().size(); x++) {
                        if (mainUser.getProjects().get(x).equalsIgnoreCase(p.getProjectName())) {
                            p.setProjectTasks(new ArrayList<zfleming.solarispm.Task>());
                            for (DataSnapshot taskShot : dataSnapshot.child(id).child("tasks").getChildren()){
                                zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
                                p.getProjectTasks().add(t);
                            }
                            userProjects.add(p);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        title.setText("Welcome " + mainUser.getUserName() + "!");
        projects = mainUser.getProjects();
        if (projects != null) {
            if (projects.isEmpty()) {
                listView.setVisibility(View.INVISIBLE);
            } else if (projects.get(0).equalsIgnoreCase("dummy")) {
                listView.setVisibility(View.INVISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                adapter = new ArrayAdapter(MainPage.this, android.R.layout.simple_list_item_1, projects);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int index = -1;
                        String projectName = parent.getItemAtPosition(position).toString();
//                        for (int x = 0; x < mainUser.getProjects().size(); x++) {
//                            String s = mainUser.getProjects().get(x);
//                            if (projectName.equalsIgnoreCase(s)) {
//                                index = x;
//                            }
//                        }
                        for (int x = 0; x < userProjects.size(); x++) {
                            if (projectName.equalsIgnoreCase(userProjects.get(x).getProjectName())) {
                                index = x;
                            }
                        }
                        Project p = userProjects.get(index);
                        Intent intent = new Intent(MainPage.this, ProjectPage.class);
                        intent.putExtra("Main User", (Parcelable) mainUser);
                        intent.putExtra("User projects", mainUser.getProjects());
                        intent.putExtra("Main Project", (Parcelable)p);
                        intent.putExtra("Project Tasks", p.getProjectTasks());
                        intent.putExtra("Assigned Users", p.getAssignedUsers());
                        //  intent.putExtra("Project Users", (Serializable) mainUser.getProjects().get(index).getAssignedUsers());
                        //  intent.putExtra("Project Tasks", (Serializable) mainUser.getProjects().get(index).getProjectTasks());
                        startActivityForResult(intent,0);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 & resultCode == RESULT_OK) {
            final ArrayList<User> existingUsers = new ArrayList<>();
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference = root.child("Users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        User u = postSnapshot.getValue(User.class);
                        if (u.getUserName().equalsIgnoreCase(mainUser.getUserName())) {
                            mainUser = u;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            userProjects = new ArrayList<>();
            reference = root.child("Projects");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Project p = postSnapshot.getValue(Project.class);
                        String id = postSnapshot.getKey().toString();
                        for (int x = 0; x < mainUser.getProjects().size(); x++) {
                            if (mainUser.getProjects().get(x).equalsIgnoreCase(p.getProjectName())) {
                                p.setProjectTasks(new ArrayList<zfleming.solarispm.Task>());
                                for (DataSnapshot taskShot : dataSnapshot.child(id).child("tasks").getChildren()){
                                    zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
                                    p.getProjectTasks().add(t);
                                }
                                userProjects.add(p);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            title.setText("Welcome " + mainUser.getUserName() + "!");
            projects = mainUser.getProjects();
            if (projects != null) {
                if (projects.isEmpty()) {
                    listView.setVisibility(View.INVISIBLE);
                } else if (projects.get(0).equalsIgnoreCase("dummy")) {
                    listView.setVisibility(View.INVISIBLE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    adapter = new ArrayAdapter(MainPage.this, android.R.layout.simple_list_item_1, projects);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            int index = -1;
                            String projectName = parent.getItemAtPosition(position).toString();
                            for (int x = 0; x < userProjects.size(); x++) {
                                if (projectName.equalsIgnoreCase(userProjects.get(x).getProjectName())) {
                                    index = x;
                                }
                            }
                            Project p = userProjects.get(index);
                            Intent intent = new Intent(MainPage.this, ProjectPage.class);
                            intent.putExtra("Main User", (Parcelable) mainUser);
                            intent.putExtra("User projects", mainUser.getProjects());
                            intent.putExtra("Main Project", (Parcelable)p);
                            intent.putExtra("Project Tasks", p.getProjectTasks());
                            intent.putExtra("Assigned Users", p.getAssignedUsers());
                            //  intent.putExtra("Project Users", (Serializable) mainUser.getProjects().get(index).getAssignedUsers());
                            //  intent.putExtra("Project Tasks", (Serializable) mainUser.getProjects().get(index).getProjectTasks());
                            startActivityForResult(intent,0);
                        }
                    });
                }
            }
        }
    }
}
