package zfleming.solarispm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

public class AddANewProject extends AppCompatActivity {

    private User mainUser;
    private Project newProject;
    private ArrayList<String> invitedUsers, allUserIDs;
    private Button invite, submit, submitName;
    private EditText projectNameText, requestedUser;
    private String projectName, givenUsername;
    private int numOfInvite = 0;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private ArrayList<User> existingUsers;
    private CheckBox leaderSet;
    private boolean hasANewLeaderBeenSet = false;
    private TextView projectNameTxt, inviteTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewproject);
        projectNameTxt = (TextView) findViewById(R.id.projectName);
        inviteTxt = (TextView) findViewById(R.id.invite);
        invite = (Button) findViewById(R.id.inviteBttn);
        submit = (Button) findViewById(R.id.submitBttn);
        submitName = (Button) findViewById(R.id.submitNameBttn);
        projectNameText = (EditText) findViewById(R.id.projectNameText);
        requestedUser = (EditText) findViewById(R.id.inviteText);
        invitedUsers = new ArrayList<String>();
        leaderSet = (CheckBox) findViewById(R.id.leaderBox);
        mainUser = getIntent().getParcelableExtra("Main User");
        newProject = new Project();
        ArrayList<String> projects = (ArrayList<String>) getIntent().getSerializableExtra("User projects");
        if (projects != null) {
            if (!projects.isEmpty()) {
                mainUser.setProjects(projects);
            } else {
                mainUser.setProjects(new ArrayList<String>());
            }
        } else {
            mainUser.setProjects(new ArrayList<String>());
        }
        fAuth = FirebaseAuth.getInstance();
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                if (fUser != null) {
                    fAuth.signInWithEmailAndPassword(mainUser.getEmail(), mainUser.getPassword());
                }
                existingUsers = new ArrayList<>();
                allUserIDs = new ArrayList<>();
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                Query reference = root.child("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            existingUsers.add(postSnapshot.getValue(User.class));
                            allUserIDs.add(postSnapshot.getKey().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if (mainUser == null) {
                    User user = null;
                    for (int x = 0; x < existingUsers.size(); x++) {
                        if (fUser.getEmail().equalsIgnoreCase(existingUsers.get(x).getEmail())) {
                            user = existingUsers.get(x);
                        }
                    }
                    if (user == null) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        mainUser = user;
                    }
                }
            }
        };
        invitedUsers.add(mainUser.getUserName());
        newProject.setLeader(mainUser.getUserName());
        requestedUser.setVisibility(View.INVISIBLE);
        invite.setVisibility(View.INVISIBLE);
        leaderSet.setVisibility(View.INVISIBLE);
        inviteTxt.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);
    }

    public void goToMain(View view) {
        mainUser.getProjects().add(newProject.getProjectName());
//        final Boolean[] usersInvited = new Boolean[invitedUsers.size()];
        final DatabaseReference assignedUsersDb = FirebaseDatabase.getInstance().getReference();
//        final boolean allUsersInvited = false;
//           final int index = x;
        final ArrayList<String> userIDs = new ArrayList<>();
        final ArrayList<User> users = new ArrayList<>();
        for (int x = 0; x < invitedUsers.size(); x++) {
            String username = invitedUsers.get(x);
            for (int y = 0; y < existingUsers.size(); y++) {
                User u = existingUsers.get(y);
                if (u.getUserName().equalsIgnoreCase(username)) {
                    for (int z = 0; z < allUserIDs.size(); z++) {
                        if (z == y) {
                            userIDs.add(allUserIDs.get(z));
                            users.add(u);
                        }
                    }
                }
            }
        }
        for (int x = 0; x < users.size(); x++) {
            User u = users.get(x);
            if (u.getProjects() != null) {
                for (int y = 0; y < u.getProjects().size(); y++) {
                    String a = u.getProjects().get(y);
                    if (a!=null) {
                        if (a.equalsIgnoreCase("dummy")) {
                            u.getProjects().remove(y);
                        }
                    }

                }
            } else {
                u.setProjects(new ArrayList<String>());
            }
            u.getProjects().add(newProject.getProjectName());
            Query q = assignedUsersDb.child("Users").child(userIDs.get(x)).child("projects");
            q.getRef().setValue(u.getProjects());
        }

        DatabaseReference currentDb = FirebaseDatabase.getInstance().getReference().child("Projects").child(UUID.randomUUID().toString());
        /*
        name
        leader
        assignedusers
        task
        isComplete
        percentage
         */
        currentDb.child("projectName").setValue(newProject.getProjectName());
        currentDb.child("leader").setValue(newProject.getLeader());
//        for (int x = 0; x < invitedUsers.size(); x++) {
        currentDb.child("assignedUsers").setValue(invitedUsers);
//        }
        currentDb.child("isComplete").setValue(false);
        currentDb.child("percentage").setValue(0);
        newProject.setProjectTasks(new ArrayList<Task>());
        Task t = new Task();
        t.setTaskName("Dummy");
        t.setComplete(false);
        t.setAssignedUsers(new ArrayList<String>());
        t.getAssignedUsers().add("Dummy");
        t.setSubTask(new ArrayList<SubTask>());
        t.getSubTask().add(new SubTask("Dummy"));
        newProject.getProjectTasks().add(t);
        String sname = newProject.getProjectTasks().get(0).getSubTask().get(0).getSubTaskName();
        boolean tf = newProject.getProjectTasks().get(0).getSubTask().get(0).isSubTaskComplete();
        currentDb.child("tasks").child("0").child("subTask").child("0").child("subTaskName").setValue(sname);
        currentDb.child("tasks").child("0").child("subTask").child("0").child("subTaskComplete").setValue(tf);
        currentDb.child("tasks").child("0").child("assignedUsers").setValue(t.getAssignedUsers());
        currentDb.child("tasks").child("0").child("complete").setValue(false);
        currentDb.child("tasks").child("0").child("percentage").setValue(0);
        currentDb.child("tasks").child("0").child("taskName").setValue("Dummy");

//        Intent intent = new Intent(AddANewProject.this, MainPage.class);
//        intent.putExtra("Main User", (Parcelable) mainUser);
//        intent.putExtra("User projects", mainUser.getProjects());
//        startActivity(intent);
        finish();
    }

    public void inviteUsers(View view) {
        String username = requestedUser.getText().toString();
        if (!username.isEmpty()) {
            boolean foundUser = false;
            User u = null;
            for (int x = 0; x < existingUsers.size(); x++) {
                if (username.equalsIgnoreCase(existingUsers.get(x).getUserName())) {
                    u = existingUsers.get(x);
                    foundUser = true;
                    break;
                }
            }
            if (foundUser) {
                invitedUsers.add(u.getUserName());
                if (leaderSet.isChecked() && !hasANewLeaderBeenSet) {
                    newProject.setLeader(u.getUserName());
                    hasANewLeaderBeenSet = true;
                    leaderSet.setVisibility(View.INVISIBLE);
                }
                numOfInvite++;
                Toast.makeText(AddANewProject.this, "User invited.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AddANewProject.this, "Username not recognized.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void submitProjectName(View view) {
        String pn = projectNameText.getText().toString();
        if (!pn.isEmpty()) {
            newProject.setProjectName(pn);
            projectNameText.setVisibility(View.INVISIBLE);
            projectNameTxt.setVisibility(View.INVISIBLE);
            submitName.setVisibility(View.INVISIBLE);
            invite.setVisibility(View.VISIBLE);
            leaderSet.setVisibility(View.VISIBLE);
            inviteTxt.setVisibility(View.VISIBLE);
            submit.setVisibility(View.VISIBLE);
            requestedUser.setVisibility(View.VISIBLE);
            Toast.makeText(AddANewProject.this, "Name accepted.", Toast.LENGTH_LONG).show();
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
