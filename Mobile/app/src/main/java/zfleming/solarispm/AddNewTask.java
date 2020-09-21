package zfleming.solarispm;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class AddNewTask extends AppCompatActivity {
    private User leader;
    private Project mainProject;
    private Task newTask;
    private ArrayList<User> teammates, assignedTeammates;
    private ArrayList<String> invitedUsers;
    private TextView addTaskName, invite;
    private EditText taskNameTxt, inviteTxt;
    private Button submitName, submitTask, inviteBttn;
    private String projectID;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewtask);
        invitedUsers = new ArrayList<>();
        newTask = new Task();
        SubTask dummy = new SubTask();
        dummy.setSubTaskName("Dummy");
        newTask.setSubTask(new ArrayList<SubTask>());
        newTask.getSubTask().add(dummy);
        addTaskName = (TextView) findViewById(R.id.taskProgress);
        invite = (TextView) findViewById(R.id.inviteTeammates);
        taskNameTxt = (EditText) findViewById(R.id.taskNameText);
        inviteTxt = (EditText) findViewById(R.id.inviteText);
        submitName = (Button) findViewById(R.id.submitNameBttn);
        submitTask = (Button) findViewById(R.id.submitBttn);
        inviteBttn = (Button) findViewById(R.id.inviteBttn);
        leader = getIntent().getParcelableExtra("Main User");
        leader.setProjects((ArrayList<String>) getIntent().getSerializableExtra("User projects"));
        mainProject = getIntent().getParcelableExtra("Main Project");
        mainProject.setProjectTasks((ArrayList<Task>) getIntent().getSerializableExtra("Project Tasks"));
        mainProject.setAssignedUsers((ArrayList<String>) getIntent().getSerializableExtra("Assigned Users"));
        invitedUsers.add(leader.getUserName());
        fAuth = FirebaseAuth.getInstance();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (fUser == null) {
                    if (leader.isEmpty()) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        fAuth.signInWithEmailAndPassword(leader.getEmail(), leader.getPassword());
                        fUser = fAuth.getCurrentUser();
                    }
                }
            }
        };
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query query = root.child("Projects");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Project p = postSnapshot.getValue(Project.class);
                    if (p.getProjectName().equalsIgnoreCase(mainProject.getProjectName())) {
                        String id = postSnapshot.getKey().toString();
                        projectID = id;
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void goToProject(View view) {
        if (invitedUsers.size() > 0) {
            newTask.setAssignedUsers(invitedUsers);
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            if (mainProject.getProjectTasks() == null) {
                mainProject.setProjectTasks(new ArrayList<Task>());
            } else if (!mainProject.getProjectTasks().isEmpty()) {
                if (mainProject.getProjectTasks().get(0).getTaskName().equalsIgnoreCase("dummy")) {
                    mainProject.setProjectTasks(new ArrayList<Task>());
                }
            }

            mainProject.getProjectTasks().add(newTask);
            boolean dummyFound = false;
            for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                Task t = mainProject.getProjectTasks().get(x);
                for (int y = x + 1; y < mainProject.getProjectTasks().size(); y++) {
                    Task q = mainProject.getProjectTasks().get(y);
                    if (t.getTaskName().equalsIgnoreCase(q.getTaskName())) {
                        mainProject.getProjectTasks().remove(y);
                    }
                }
//                if (t.getTaskName().equalsIgnoreCase("dummy")) {
//                    dummyFound=true;
//                }
//                if (newTask.getTaskName().equalsIgnoreCase(t.getTaskName())) {
//                    if (dummyFound) {
//                        index = x-1;
//                    }
//                }
            }
            int index = mainProject.getProjectTasks().size() - 1;
            int completed = 0, completeResult = 0;
            for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                Task t = mainProject.getProjectTasks().get(x);
                if (t.isComplete()) {
                    completed++;
                }
            }
            completeResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
            mainProject.setPercentage(completeResult);
            mainProject.setComplete(false);
            root.child("Projects").child(projectID).child("tasks").child("" + index).child("assignedUsers").setValue(newTask.getAssignedUsers());
            root.child("Projects").child(projectID).child("tasks").child("" + index).child("complete").setValue(newTask.isComplete());
            root.child("Projects").child(projectID).child("tasks").child("" + index).child("percentage").setValue(newTask.getPercentage());
            if (newTask.getSubTask() == null) {
                newTask.setSubTask(new ArrayList<SubTask>());
                newTask.getSubTask().add(new SubTask("dummy"));
            }
            for (int y = 0; y < newTask.getSubTask().size(); y++) {
                SubTask s = newTask.getSubTask().get(y);
                root.child("Projects").child(projectID).child("tasks").child("" + index).child("subTask").child("" + y).child("subTaskName").setValue(s.getSubTaskName());
                root.child("Projects").child(projectID).child("tasks").child("" + index).child("subTask").child("" + y).child("subTaskComplete").setValue(s.isSubTaskComplete());

            }
            root.child("Projects").child(projectID).child("tasks").child("" + index).child("taskName").setValue(newTask.getTaskName());
//            for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
//                Task t = mainProject.getProjectTasks().get(x);
//                if (t!=null) {
//                    if (t.getTaskName().equalsIgnoreCase(newTask.getTaskName())) {
//                        if (!t.getTaskName().equalsIgnoreCase("dummy")) {
//                            root.child("Projects").child(projectID).child("tasks").child("" + index).child("assignedUsers").setValue(t.getAssignedUsers());
//                            root.child("Projects").child(projectID).child("tasks").child("" + index).child("complete").setValue(t.isComplete());
//                            root.child("Projects").child(projectID).child("tasks").child("" + index).child("percentage").setValue(t.getPercentage());
//                            if (t.getSubTask() == null) {
//                                t.setSubTask(new ArrayList<SubTask>());
//                                t.getSubTask().add(new SubTask("dummy"));
//                            }
//                            for (int y = 0; y < t.getSubTask().size(); y++) {
//                                SubTask s = t.getSubTask().get(y);
//                                root.child("Projects").child(projectID).child("tasks").child("" + index).child("subTask").child("" + y).child("subTaskName").setValue(s.getSubTaskName());
//                                root.child("Projects").child(projectID).child("tasks").child("" + index).child("subTask").child("" + y).child("subTaskComplete").setValue(s.isSubTaskComplete());
//
//                            }
//                            root.child("Projects").child(projectID).child("tasks").child("" + index).child("taskName").setValue(t.getTaskName());
//                        }
//                    }
//                }
//
//
//            }

            Toast.makeText(AddNewTask.this, "Task Created!", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void inviteTeammate(View view) {
        String username = inviteTxt.getText().toString();
        if (!username.isEmpty()) {
            boolean userAccepted = false;
            for (int x = 0; x < mainProject.getAssignedUsers().size(); x++) {
                String s = mainProject.getAssignedUsers().get(x);
                if (username.equalsIgnoreCase(s)) {
                    userAccepted = true;
                }
            }
            if (userAccepted) {
                invitedUsers.add(username);
                Toast.makeText(AddNewTask.this, "User added", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AddNewTask.this, "User not assigned this project", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(AddNewTask.this, "Don't leave field blank", Toast.LENGTH_LONG).show();
        }

    }


    public void submitTaskName(View view) {
        String tn = taskNameTxt.getText().toString();
        if (!tn.isEmpty()) {
            newTask.setTaskName(tn);
            addTaskName.setVisibility(View.INVISIBLE);
            taskNameTxt.setVisibility(View.INVISIBLE);
            submitName.setVisibility(View.INVISIBLE);
            invite.setVisibility(View.VISIBLE);
            inviteTxt.setVisibility(View.VISIBLE);
            inviteBttn.setVisibility(View.VISIBLE);
            submitName.setVisibility(View.VISIBLE);
            submitTask.setVisibility(View.VISIBLE);
            Toast.makeText(AddNewTask.this, "Name accepted.", Toast.LENGTH_LONG).show();
            if (mainProject.getAssignedUsers().size() == 1) {
                newTask.setAssignedUsers(invitedUsers);
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                if (mainProject.getProjectTasks() == null) {
                    mainProject.setProjectTasks(new ArrayList<Task>());
                } else if (mainProject.getProjectTasks().get(0).getTaskName().equalsIgnoreCase("dummy")) {
                    mainProject.setProjectTasks(new ArrayList<Task>());
                }
                mainProject.getProjectTasks().add(newTask);
                int completed = 0, completeResult = 0;
                for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                    Task t = mainProject.getProjectTasks().get(x);
                    if (t.isComplete()) {
                        completed++;
                    }
                }
                completeResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                mainProject.setPercentage(completeResult);
                mainProject.setComplete(false);
                root.child("Projects").child(projectID).child("tasks").getRef().setValue(mainProject.getProjectTasks());
                Toast.makeText(AddNewTask.this, "Task Created!", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(AddNewTask.this, "Don't leave field blank", Toast.LENGTH_LONG).show();
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
