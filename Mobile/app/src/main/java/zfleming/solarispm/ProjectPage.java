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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class ProjectPage extends AppCompatActivity {

    private Project mainProject;
    private User mainUser;
    private TextView projectName, projectProgressTxt;
    private ListView taskList;
    private ArrayAdapter adapter;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private Button addNewTaskBttn;
    private String projectID;
    private ProgressBar projectProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projectpage);
        projectProgressTxt = (TextView) findViewById(R.id.projectProgress);
        addNewTaskBttn = (Button) findViewById(R.id.addNewTaskBttn);
        projectProgress = (ProgressBar) findViewById(R.id.projectProgressBar);
        mainUser = getIntent().getParcelableExtra("Main User");
        mainUser.setProjects((ArrayList<String>) getIntent().getSerializableExtra("User projects"));
        mainProject = getIntent().getParcelableExtra("Main Project");
        projectID = getIntent().getStringExtra("Project ID");
        mainProject.setProjectTasks((ArrayList<Task>) getIntent().getSerializableExtra("Project Tasks"));
        mainProject.setAssignedUsers((ArrayList<String>) getIntent().getSerializableExtra("Assigned Users"));
        projectName = (TextView) findViewById(R.id.projectName);
        projectName.setText(mainProject.getProjectName());
        taskList = (ListView) findViewById(R.id.taskList);
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
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query query = root.child("Projects");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Project p = postSnapShot.getValue(Project.class);
                    if (mainProject.getProjectName().equalsIgnoreCase(p.getProjectName())) {
                        projectID = postSnapShot.getKey().toString();
                        p.setProjectTasks(new ArrayList<Task>());
                        mainProject = p;
                        for (DataSnapshot taskShot : dataSnapshot.child(projectID).child("tasks").getChildren()) {
                            zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
                            if (t != null) {
                                if (t.getTaskName() != null) {
                                    if (!t.getTaskName().equalsIgnoreCase("dummy")) {
                                        mainProject.getProjectTasks().add(t);
                                    }

                                }
                            }

                        }
                        if (mainProject.getProjectTasks() != null) {
                            if (!mainProject.getProjectTasks().isEmpty()) {
                                int completed = 0, completeResult = 0;
                                for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                                    Task t = mainProject.getProjectTasks().get(x);
                                    if (t.isComplete()) {
                                        completed++;
                                    }
                                }
                                completeResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                                mainProject.setPercentage(completeResult);
                                projectProgress.setProgress((int) mainProject.getPercentage());
                                projectProgressTxt.setText("Project Progress:" + (int) mainProject.getPercentage() + "%");

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (mainProject.getPercentage() == 100 && !mainProject.isComplete()) {
            mainProject.setComplete(true);
            root.child(projectID).child("percentage").setValue(100);
            root.child(projectID).child("isComplete").setValue(true);
        }
        ArrayList<String> taskListNames = new ArrayList<>();
        if (mainProject.getProjectTasks() != null) {
            if (!mainProject.getProjectTasks().isEmpty()) {
                int completed = 0, completeResult = 0;
                for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                    Task t = mainProject.getProjectTasks().get(x);
                    if (t.isComplete()) {
                        completed++;
                    }
                }
                completeResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                mainProject.setPercentage(completeResult);
                if (completeResult == 100) {
                    mainProject.setComplete(true);
                } else {
                    mainProject.setComplete(false);
                }
                if (projectID == null) {
                    projectID = getIntent().getStringExtra("Project ID");
                }
                if (projectID != null) {
                    root.child("Projects").child(projectID).child("isComplete").setValue(mainProject.isComplete());
                    root.child("Projects").child(projectID).child("percentage").setValue(mainProject.getPercentage());
                }
                for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                    Task t = mainProject.getProjectTasks().get(x);
                    if (!t.getTaskName().equalsIgnoreCase("dummy")) {
                        taskListNames.add(mainProject.getProjectTasks().get(x).getTaskName());

                    }
                }
                adapter = new ArrayAdapter(ProjectPage.this, android.R.layout.simple_list_item_1, taskListNames);
                taskList.setAdapter(adapter);
                taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int index = -1;
                        String taskName = parent.getItemAtPosition(position).toString();
                        for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                            if (taskName.equalsIgnoreCase(mainProject.getProjectTasks().get(x).getTaskName())) {
                                index = x;
                            }
                        }

                        Intent intent = new Intent(ProjectPage.this, TaskPage.class);
                        intent.putExtra("Main User", (Parcelable) mainUser);
                        intent.putExtra("Main Project", (Parcelable) mainProject);
                        intent.putExtra("Main Task", (Parcelable) mainProject.getProjectTasks().get(index));
                        intent.putExtra("Assigned Task Users", mainProject.getProjectTasks().get(index).getAssignedUsers());
                        intent.putExtra("SubTasks", mainProject.getProjectTasks().get(index).getSubTask());
                        intent.putExtra("Project ID", projectID);
                        intent.putExtra("Task ID", index - 1);
                        startActivityForResult(intent, 0);
                    }
                });
            }
        }


        if (!mainProject.getLeader().equalsIgnoreCase(mainUser.getUserName())) {
            addNewTaskBttn.setVisibility(View.INVISIBLE);
        }

        projectProgress.setProgress((int) mainProject.getPercentage());
        projectProgressTxt.setText("Project Progress:" + (int) mainProject.getPercentage() + "%");
    }

    public void goToTeammates(View view) {
        Intent intent = new Intent(ProjectPage.this, TeammatePage.class);
        intent.putExtra("Assigned Users", mainProject.getAssignedUsers());
        intent.putExtra("Main User", (Parcelable) mainUser);
        startActivity(intent);
    }

    public void addNewTask(View view) {
        Intent intent = new Intent(ProjectPage.this, AddNewTask.class);
        intent.putExtra("Main User", (Parcelable) mainUser);
        intent.putExtra("Main Project", (Parcelable) mainProject);
        intent.putExtra("Assigned Users", mainProject.getAssignedUsers());
        intent.putExtra("Project Tasks", mainProject.getProjectTasks());
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
        setResult(RESULT_OK, getIntent());
    }

    @Override
    public void finish() {
        super.finish();
        setResult(RESULT_OK, getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final ArrayList<User> existingUsers = new ArrayList<>();
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query reference = root.child("Users");
        if (fUser == null) {
            fAuth.signInWithEmailAndPassword(mainUser.getEmail(), mainUser.getPassword());
//                        Intent i = getBaseContext().getPackageManager()
//                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);
//                    }
        }
        if (projectID == null) {
            projectID = getIntent().getStringExtra("Project ID");
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

        ArrayList<String> taskListNames = new ArrayList<>();
        if (mainProject.getProjectTasks() != null) {
            if (!mainProject.getProjectTasks().isEmpty()) {
                int completed = 0, completeResult = 0;
                for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                    Task t = mainProject.getProjectTasks().get(x);
                    if (t.isComplete()) {
                        completed++;
                    }
                }
                completeResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                mainProject.setPercentage(completeResult);
                if (completeResult == 100) {
                    mainProject.setComplete(true);
                } else {
                    mainProject.setComplete(false);
                }
                if (projectID == null) {
                    projectID = getIntent().getStringExtra("Project ID");
                }
                if (projectID != null) {
                    root.child("Projects").child(projectID).child("isComplete").setValue(mainProject.isComplete());
                    root.child("Projects").child(projectID).child("percentage").setValue(mainProject.getPercentage());
                }
                for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                    Task t = mainProject.getProjectTasks().get(x);
                    if (!t.getTaskName().equalsIgnoreCase("dummy")) {
                        taskListNames.add(mainProject.getProjectTasks().get(x).getTaskName());

                    }
                }
                adapter = new ArrayAdapter(ProjectPage.this, android.R.layout.simple_list_item_1, taskListNames);
                taskList.setAdapter(adapter);
                taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int index = -1;
                        String taskName = parent.getItemAtPosition(position).toString();
                        for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                            if (taskName.equalsIgnoreCase(mainProject.getProjectTasks().get(x).getTaskName())) {
                                index = x;
                            }
                        }
                        int taksID = index;
                        Intent intent = new Intent(ProjectPage.this, TaskPage.class);
                        intent.putExtra("Main User", (Parcelable) mainUser);
                        intent.putExtra("Main Project", (Parcelable) mainProject);
                        intent.putExtra("Project Tasks", mainProject.getProjectTasks());
                        intent.putExtra("Main Task", (Parcelable) mainProject.getProjectTasks().get(index));
                        intent.putExtra("Assigned Task Users", mainProject.getProjectTasks().get(index).getAssignedUsers());
                        intent.putExtra("SubTasks", mainProject.getProjectTasks().get(index).getSubTask());
                        intent.putExtra("Project ID", projectID);
                        intent.putExtra("Task ID", taksID);
                        startActivityForResult(intent, 0);
                    }
                });
            }
        }
        projectProgress.setProgress((int) mainProject.getPercentage());
        projectProgressTxt.setText("Project Progress:" + (int) mainProject.getPercentage() + "%");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 & resultCode == RESULT_OK) {
            final ArrayList<User> existingUsers = new ArrayList<>();
            final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference = root.child("Users");
            if (projectID == null) {
                projectID = getIntent().getStringExtra("Project ID");
            }
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

            ArrayList<String> taskListNames = new ArrayList<>();
            if (mainProject.getProjectTasks() != null) {
                if (!mainProject.getProjectTasks().isEmpty()) {
                    int completed = 0, completeResult = 0;
                    for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                        Task t = mainProject.getProjectTasks().get(x);
                        if (t.isComplete()) {
                            completed++;
                        }
                    }
                    completeResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                    mainProject.setPercentage(completeResult);
                    if (completeResult == 100) {
                        mainProject.setComplete(true);
                    } else {
                        mainProject.setComplete(false);
                    }
                    if (projectID == null) {
                        projectID = getIntent().getStringExtra("Project ID");
                    }
                    if (projectID != null) {
                        root.child("Projects").child(projectID).child("isComplete").setValue(mainProject.isComplete());
                        root.child("Projects").child(projectID).child("percentage").setValue(mainProject.getPercentage());
                    }
                    for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                        Task t = mainProject.getProjectTasks().get(x);
                        if (!t.getTaskName().equalsIgnoreCase("dummy")) {
                            taskListNames.add(mainProject.getProjectTasks().get(x).getTaskName());

                        }
                    }
                    adapter = new ArrayAdapter(ProjectPage.this, android.R.layout.simple_list_item_1, taskListNames);
                    taskList.setAdapter(adapter);
                    taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            int index = -1;
                            String taskName = parent.getItemAtPosition(position).toString();
                            for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                                if (taskName.equalsIgnoreCase(mainProject.getProjectTasks().get(x).getTaskName())) {
                                    index = x;
                                }
                            }
                            int taksID = index;
                            Intent intent = new Intent(ProjectPage.this, TaskPage.class);
                            intent.putExtra("Main User", (Parcelable) mainUser);
                            intent.putExtra("Main Project", (Parcelable) mainProject);
                            intent.putExtra("Project Tasks", mainProject.getProjectTasks());
                            intent.putExtra("Main Task", (Parcelable) mainProject.getProjectTasks().get(index));
                            intent.putExtra("Assigned Task Users", mainProject.getProjectTasks().get(index).getAssignedUsers());
                            intent.putExtra("SubTasks", mainProject.getProjectTasks().get(index).getSubTask());
                            intent.putExtra("Project ID", projectID);
                            intent.putExtra("Task ID", taksID);
                            startActivityForResult(intent, 0);
                        }
                    });
                }
            }
            projectProgress.setProgress((int) mainProject.getPercentage());
            projectProgressTxt.setText("Project Progress:" + (int) mainProject.getPercentage() + "%");
        }
    }
}
