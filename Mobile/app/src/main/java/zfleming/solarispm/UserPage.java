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
import android.widget.Button;
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
import java.util.HashMap;

public class UserPage extends AppCompatActivity {
    private User mainUser;
    private TextView title, checking;
    private Button yes, no, leave, change, delete, logout;
    private String condition;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private String mainUserID;
    private HashMap<String, Project> allUserProject;
    private ArrayList<String> userProjectIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);
        allUserProject = new HashMap<>();
        userProjectIDs = new ArrayList<>();
        title = (TextView) findViewById(R.id.user);
        checking = (TextView) findViewById(R.id.areYouSure);
        yes = (Button) findViewById(R.id.yesBttn);
        no = (Button) findViewById(R.id.noBttn);
        leave = (Button) findViewById(R.id.leaveBttn);
        change = (Button) findViewById(R.id.changeBttn);
        delete = (Button) findViewById(R.id.deleteBttn);
        logout = (Button) findViewById(R.id.logOutBttn);
        mainUser = getIntent().getParcelableExtra("Main User");
        if (mainUser != null) {
            ArrayList<String> projects = (ArrayList<String>) getIntent().getSerializableExtra("User projects");
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
                                        allUserProject.put(id, p);
                                        userProjectIDs.add(id);
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
        if (mainUser.isEmpty()) {
            User user = null;
            final FirebaseUser fUser = fAuth.getCurrentUser();
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
        }
        title.setText(mainUser.getUserName());
    }

    public void goToChangeCredentials(View view) {
        Intent intent = new Intent(UserPage.this, ChangeCredentials.class);
        intent.putExtra("Main User", (Parcelable) mainUser);
        intent.putExtra("User projects", mainUser.getProjects());
        startActivity(intent);
    }

    public void goToLeaveProject(View view) {
        Intent intent = new Intent(UserPage.this, LeaveProjectPage.class);
        intent.putExtra("Main User", (Parcelable) mainUser);
        intent.putExtra("User projects", mainUser.getProjects());
        startActivity(intent);
    }

    public void deleteAccount(View view) {
        condition = "Delete Account";
        change.setVisibility(View.INVISIBLE);
        leave.setVisibility(View.INVISIBLE);
        logout.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        yes.setVisibility(View.VISIBLE);
        no.setVisibility(View.VISIBLE);
        checking.setVisibility(View.VISIBLE);
    }


    public void logout(View view) {
        condition = "Logout";
        change.setVisibility(View.INVISIBLE);
        leave.setVisibility(View.INVISIBLE);
        logout.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        yes.setVisibility(View.VISIBLE);
        no.setVisibility(View.VISIBLE);
        checking.setVisibility(View.VISIBLE);

    }


    public void yes(View view) {
        if (condition.equalsIgnoreCase("Delete Account")) {
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference;
            for (int x = 0; x < userProjectIDs.size(); x++) {
                reference = root.child("Projects");
                Project p = allUserProject.get(userProjectIDs.get(x));
                for (int y = 0; y < p.getAssignedUsers().size(); y++) {
                    if (p.getAssignedUsers().get(y).equalsIgnoreCase(mainUser.getUserName())) {
                        p.getAssignedUsers().remove(y);
                    }
                }
                if (p.getAssignedUsers().isEmpty()) {
                    reference.getRef().child(userProjectIDs.get(x)).setValue(null);
                } else {
                    reference.getRef().child(userProjectIDs.get(x)).child("assignedUsers").setValue(p.getAssignedUsers());
                }
            }
            root.child("Users").child(mainUserID).setValue(null);
            FirebaseUser user = fAuth.getCurrentUser();
            ArrayList<String> deleteFromProject = mainUser.getProjects();
            FirebaseDatabase.getInstance().getReference().child(user.getUid()).removeValue();

//            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
//            for (int x = 0; x < deleteFromProject.size(); x++) {
//                String projectName = deleteFromProject.get(x);
//                Query reference = root.child("Projects").orderByValue().orderByChild("projectName").equalTo(projectName).
//                        orderByChild("assignedUsers");
//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                            String userName = postSnapshot.getValue(String.class);
//                            if (mainUser.getUserName().equalsIgnoreCase(userName)) {
//                                dataSnapshot.getRef().setValue(null);
//
//                                break;
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//            }
        }
        fAuth.signOut();

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void no(View view) {
        change.setVisibility(View.VISIBLE);
        leave.setVisibility(View.VISIBLE);
        logout.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        yes.setVisibility(View.INVISIBLE);
        no.setVisibility(View.INVISIBLE);
        checking.setVisibility(View.INVISIBLE);
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
        if (title == null) {
            title = (TextView) findViewById(R.id.welcome);
        }
        title.setText("Welcome " + mainUser.getUserName() + "!");
        allUserProject = new HashMap<>();
        userProjectIDs = new ArrayList<>();
        reference = root.child("Projects");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Project p = postSnapshot.getValue(Project.class);
                    String id = postSnapshot.getKey().toString();
                    for (int x = 0; x < mainUser.getProjects().size(); x++) {
                        if (mainUser.getProjects().get(x).equalsIgnoreCase(p.getProjectName())) {
                            allUserProject.put(id, p);
                            userProjectIDs.add(id);
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
