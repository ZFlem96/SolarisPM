package zfleming.solarispm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;

public class TaskPage extends AppCompatActivity {

    private Task mainTask;
    private Project mainProject;
    private User mainUser;
    private TextView taskname, taskProgress;
    private ListView subTaskList;
    private ArrayAdapter adapter;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private Button addNewTask;
    private String projectID, taskID;
    private boolean isAssignedUser;
    private ProgressBar taskProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskpage);
        taskProgress = (TextView) findViewById(R.id.taskProgress);
        taskProgressBar = (ProgressBar) findViewById(R.id.taskProgressBar);
        isAssignedUser = false;
        projectID = getIntent().getStringExtra("Project ID");
        taskID = "" + getIntent().getIntExtra("Task ID", 0);
        addNewTask = (Button) findViewById(R.id.addNewTaskBttn);
        taskname = (TextView) findViewById(R.id.taskProgress);
        subTaskList = (ListView) findViewById(R.id.subtaskList);
        mainUser = getIntent().getParcelableExtra("Main User");
        mainProject = getIntent().getParcelableExtra("Main Project");
        mainProject.setProjectTasks((ArrayList<Task>) getIntent().getSerializableExtra("Project Tasks"));
        mainTask = getIntent().getParcelableExtra("Main Task");
       ArrayList<String> users = (ArrayList<String>) getIntent().getStringArrayListExtra("Assigned Task Users");
        mainTask.setAssignedUsers(users);
        mainTask.setSubTask((ArrayList<SubTask>) getIntent().getSerializableExtra("SubTasks"));
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
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query query = root.child("Projects");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Project p = postSnapshot.getValue(Project.class);
                    if (p.getProjectName().equalsIgnoreCase(mainProject.getProjectName())) {
                        p.setProjectTasks(new ArrayList<Task>());
                        mainProject = p;
                        projectID = postSnapshot.getKey().toString();
                        for (DataSnapshot taskShot : dataSnapshot.child(projectID).child("tasks").getChildren()) {
                            zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
                            if (t != null) {
                                mainProject.getProjectTasks().add(t);
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        query = root.child("Projects").child(projectID).child("tasks").child(taskID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Task t = dataSnapshot.getValue(Task.class);
                mainTask = t;
                int completed = 0, completedResult = 0;
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    SubTask s = mainTask.getSubTask().get(x);
                    if (s.isSubTaskComplete()) {
                        completed++;
                    }
                }
                completedResult = (int) ((completed / mainTask.getSubTask().size()) * 100);
                if (completedResult == 100) {
                    mainTask.setComplete(true);
                } else {
                    mainTask.setComplete(false);
                }
                mainTask.setPercentage(completedResult);
                taskProgressBar.setProgress(mainTask.getPercentage());
                taskProgress.setText("Task Progress:" + mainTask.getPercentage() + "%");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        for (int x = 0; x < mainTask.getAssignedUsers().size(); x++) {
            String username = mainTask.getAssignedUsers().get(x);
            if (username.equalsIgnoreCase(mainUser.getUserName())) {
                isAssignedUser = true;
            }
        }
        if (!isAssignedUser) {
            addNewTask.setVisibility(View.INVISIBLE);
        }

        if (mainTask.getSubTask() != null) {
            if (!mainTask.getSubTask().isEmpty()) {
                int completed = 0, completedResult = 0;
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    SubTask s = mainTask.getSubTask().get(x);
                    if (s.isSubTaskComplete()) {
                        completed++;
                    }
                }
                completedResult = (int) ((completed / mainTask.getSubTask().size()) * 100);
                mainTask.setPercentage(completedResult);
                if (completedResult == 100) {
                    mainTask.setComplete(true);
                    completed = 0;
                    completedResult = 0;
                    float prev = mainProject.getPercentage();
                    for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                        Task t = mainProject.getProjectTasks().get(x);
                        if (t.isComplete()) {
                            completed++;
                        }
                    }
                    completedResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                    mainProject.setPercentage(completedResult);
                    if (completedResult == 100) {
                        mainProject.setComplete(true);
                    } else {
                        mainProject.setComplete(false);
                    }
                    root.child("Projects").child(projectID).child("isComplete").setValue(mainProject.isComplete());
                    root.child("Projects").child(projectID).child("percentage").setValue(mainProject.getPercentage());
                } else {
                    mainTask.setPercentage(completedResult);
                }
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    SubTask s = mainTask.getSubTask().get(x);
                    root.child("Projects").child(projectID).child("tasks").child(taskID).child("subTask").child("" + x).child("subTaskComplete").setValue(s.isSubTaskComplete());
                    root.child("Projects").child(projectID).child("tasks").child(taskID).child("subTask").child("" + x).child("subTaskName").setValue(s.getSubTaskName());
                }
                ArrayList<String> subTaskListNames = new ArrayList<>();
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    if (!mainTask.getSubTask().get(x).getSubTaskName().equalsIgnoreCase("dummy")) {
                        subTaskListNames.add(mainTask.getSubTask().get(x).getSubTaskName());
                    }

                }
                adapter = new ArrayAdapter(TaskPage.this, android.R.layout.simple_list_item_1, subTaskListNames);
                subTaskList.setAdapter(adapter);
                subTaskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (isAssignedUser) {
                            int index = -1;
                            String taskName = parent.getItemAtPosition(position).toString();
                            for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                                if (taskName.equalsIgnoreCase(mainTask.getSubTask().get(x).getSubTaskName())) {
                                    index = x;
                                }
                            }
                            Intent intent = new Intent(TaskPage.this, SubtaskPopup.class);
                            SubTask s = mainTask.getSubTask().get(index);
                            String result = "Incomplete";
                            if (s.isSubTaskComplete()) {
                                result = "Complete";
                            }
                            intent.putExtra("SubTask", (Parcelable) s);
                            intent.putExtra("Result", result);
                            startActivityForResult(intent, 0);
                        }

                    }
                });
            }
        }

        taskProgressBar.setProgress(mainTask.getPercentage());
        taskProgress.setText("Task Progress:" + mainTask.getPercentage() + "%");

    }

    public void goToTeammates(View view) {
        Intent intent = new Intent(TaskPage.this, TeammatePage.class);
        intent.putExtra("Assigned Users", mainTask.getAssignedUsers());
        intent.putExtra("Main User", (Parcelable) mainUser);
        startActivity(intent);
    }

    public void addNewSubtask(View view) {
        Intent intent = new Intent(TaskPage.this, AddSubtaskPopup.class);
        startActivityForResult(intent, 999);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 999 & resultCode == RESULT_OK) {
            SubTask s = data.getParcelableExtra("New Subtask");
            s.setSubTaskComplete(false);
            mainTask.getSubTask().add(s);
        } else if (requestCode == 0 & resultCode == RESULT_OK) {
            SubTask s = data.getParcelableExtra("Subtask");
            String result = data.getStringExtra("Result");
            if (result.equalsIgnoreCase("complete")) {
                s.setSubTaskComplete(true);
            } else {
                s.setSubTaskComplete(false);
            }
            int index = -1;
            ArrayList<SubTask> st = new ArrayList<>();
            for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                SubTask t = mainTask.getSubTask().get(x);
                if (t != null) {
                    if (!t.getSubTaskName().equalsIgnoreCase("dummy")) {
                        if (t.getSubTaskName().equalsIgnoreCase(s.getSubTaskName())) {
                            st.add(s);
                        } else {
                            st.add(t);
                        }
                    }
                }


            }
            mainTask.setSubTask(st);
        }
        for (int x = 0; x < mainTask.getSubTask().size(); x++) {
            SubTask s = mainTask.getSubTask().get(x);
            if (s != null) {
                if (s.getSubTaskName().equalsIgnoreCase("dummy")) {
                    mainTask.getSubTask().remove(x);
                }
            }

        }

        int completed = 0, completedResult = 0;
        for (int x = 0; x < mainTask.getSubTask().size(); x++) {
            SubTask s = mainTask.getSubTask().get(x);
            if (s.isSubTaskComplete()) {
                completed++;
            }
        }
        completedResult = (int) ((completed / mainTask.getSubTask().size()) * 100);
        mainTask.setPercentage(completedResult);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        if (completedResult == 100) {
            mainTask.setComplete(true);
            completed = 0;
            completedResult = 0;
            float prev = mainProject.getPercentage();
            for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                Task t = mainProject.getProjectTasks().get(x);
                if (t.isComplete()) {
                    completed++;
                }
            }
            completedResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
            mainProject.setPercentage(completedResult);
            if (completedResult == 100) {
                mainProject.setComplete(true);
            } else {
                mainProject.setComplete(false);
            }
            root.child("Projects").child(projectID).child("isComplete").setValue(mainProject.isComplete());
            root.child("Projects").child(projectID).child("percentage").setValue(mainProject.getPercentage());
        } else {
            mainTask.setComplete(false);
        }

        ArrayList<Task> ts = new ArrayList<>();
        int index = Integer.parseInt(taskID);
        for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {

            if (x == index) {
                ts.add(mainTask);
            } else {
                ts.add(mainProject.getProjectTasks().get(x));
            }
        }
        mainProject.setProjectTasks(ts);
        for (int x = 0; x < mainTask.getSubTask().size(); x++) {
            SubTask s = mainTask.getSubTask().get(x);
            root.child("Projects").child(projectID).child("tasks").child(taskID).child("subTask").child("" + x).child("subTaskComplete").setValue(s.isSubTaskComplete());
            root.child("Projects").child(projectID).child("tasks").child(taskID).child("subTask").child("" + x).child("subTaskName").setValue(s.getSubTaskName());
        }
        ArrayList<String> subTaskListNames = new ArrayList<>();
        for (int x = 0; x < mainTask.getSubTask().size(); x++) {
            subTaskListNames.add(mainTask.getSubTask().get(x).getSubTaskName());
        }
        adapter = new ArrayAdapter(TaskPage.this, android.R.layout.simple_list_item_1, subTaskListNames);
        subTaskList.setAdapter(adapter);
        subTaskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isAssignedUser) {
                    int index = -1;
                    String taskName = parent.getItemAtPosition(position).toString();
                    for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                        if (taskName.equalsIgnoreCase(mainTask.getSubTask().get(x).getSubTaskName())) {
                            index = x;
                        }
                    }
                    Intent intent = new Intent(TaskPage.this, SubtaskPopup.class);
                    SubTask s = mainTask.getSubTask().get(index);
                    String result = "Incomplete";
                    if (s.isSubTaskComplete()) {
                        result = "Complete";
                    }
                    intent.putExtra("SubTask", (Parcelable) s);
                    intent.putExtra("Result", result);
                    startActivityForResult(intent, 0);
                }
            }
        });
        taskProgressBar.setProgress(mainTask.getPercentage());
        taskProgress.setText("Task Progress:" + mainTask.getPercentage() + "%");

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
        reference = root.child("Projects");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Project p = postSnapshot.getValue(Project.class);
                    if (p.getProjectName().equalsIgnoreCase(mainProject.getProjectName())) {
                        p.setProjectTasks(new ArrayList<Task>());
                        mainProject = p;
                        projectID = postSnapshot.getKey().toString();
                        for (DataSnapshot taskShot : dataSnapshot.child(projectID).child("tasks").getChildren()) {
                            zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
                            mainProject.getProjectTasks().add(t);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
            Task t = mainProject.getProjectTasks().get(x);
            if (t.getTaskName().equalsIgnoreCase(mainTask.getTaskName())) {
                mainTask = t;
            }
        }
        if (mainTask.getSubTask() != null) {
            if (!mainTask.getSubTask().isEmpty()) {
                int completed = 0, completedResult = 0;
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    SubTask s = mainTask.getSubTask().get(x);
                    if (s.isSubTaskComplete()) {
                        completed++;
                    }
                }
                completedResult = (int) ((completed / mainTask.getSubTask().size()) * 100);
                mainTask.setPercentage(completedResult);
                if (completedResult == 100) {
                    mainTask.setComplete(true);
                    completed = 0;
                    completedResult = 0;
                    float prev = mainProject.getPercentage();
                    for (int x = 0; x < mainProject.getProjectTasks().size(); x++) {
                        Task t = mainProject.getProjectTasks().get(x);
                        if (t.isComplete()) {
                            completed++;
                        }
                    }
                    completedResult = (int) ((completed / mainProject.getProjectTasks().size()) * 100);
                    mainProject.setPercentage(completedResult);
                    if (completedResult == 100) {
                        mainProject.setComplete(true);
                    } else {
                        mainProject.setComplete(false);
                    }
                    root.child("Projects").child(projectID).child("isComplete").setValue(mainProject.isComplete());
                    root.child("Projects").child(projectID).child("percentage").setValue(mainProject.getPercentage());
                } else {
                    mainTask.setComplete(false);
                }
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    SubTask s = mainTask.getSubTask().get(x);
                    root.child("Projects").child(projectID).child("tasks").child(taskID).child("subTask").child("" + x).child("subTaskComplete").setValue(s.isSubTaskComplete());
                    root.child("Projects").child(projectID).child("tasks").child(taskID).child("subTask").child("" + x).child("subTaskName").setValue(s.getSubTaskName());
                }
                ArrayList<String> subTaskListNames = new ArrayList<>();
                for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                    if (!mainTask.getSubTask().get(x).getSubTaskName().equalsIgnoreCase("dummy")) {
                        subTaskListNames.add(mainTask.getSubTask().get(x).getSubTaskName());
                    }
                }
                adapter = new ArrayAdapter(TaskPage.this, android.R.layout.simple_list_item_1, subTaskListNames);
                subTaskList.setAdapter(adapter);
                subTaskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (isAssignedUser) {
                            int index = -1;
                            String taskName = parent.getItemAtPosition(position).toString();
                            for (int x = 0; x < mainTask.getSubTask().size(); x++) {
                                if (taskName.equalsIgnoreCase(mainTask.getSubTask().get(x).getSubTaskName())) {
                                    index = x;
                                }
                            }
                            Intent intent = new Intent(TaskPage.this, SubtaskPopup.class);
                            SubTask s = mainTask.getSubTask().get(index);
                            String result = "Incomplete";
                            if (s.isSubTaskComplete()) {
                                result = "Complete";
                            }
                            intent.putExtra("SubTask", (Parcelable) s);
                            intent.putExtra("Result", result);
                            startActivityForResult(intent, 0);
                        }

                    }
                });
            }
        }
        taskProgressBar.setProgress(mainTask.getPercentage());
        taskProgress.setText("Task Progress:" + mainTask.getPercentage() + "%");
    }


}
