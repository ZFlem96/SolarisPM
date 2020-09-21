package zfleming.solarispm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TeammatePage extends AppCompatActivity {

    private Project mainProject;
    private Task mainTask;
    private User mainUser;
    private ListView teammateList;
    private ArrayAdapter adapter;
    private ArrayList<String> selectedTeammates, givenUsers;
    private ArrayList<User> assignedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teammatepage);
        givenUsers = new ArrayList<>();
        givenUsers = getIntent().getStringArrayListExtra("Assigned Users");
//        String type = getIntent().getStringExtra("Type");
        selectedTeammates = new ArrayList<>();
        assignedUsers = new ArrayList<>();
        mainUser = getIntent().getParcelableExtra("Main User");
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
//        mainProject = getIntent().getParcelableExtra("Main Project");
//        Query query = root.child("Projects");
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    Project p = postSnapshot.getValue(Project.class);
//                    if (p.getProjectName().equalsIgnoreCase(mainProject.getProjectName())) {
//                        p.setProjectTasks(new ArrayList<Task>());
//                        mainProject = p;
//                        String projectID = postSnapshot.getKey().toString();
//                        for (DataSnapshot taskShot : dataSnapshot.child(projectID).child("tasks").getChildren()) {
//                            zfleming.solarispm.Task t = taskShot.getValue(zfleming.solarispm.Task.class);
//                            mainProject.getProjectTasks().add(t);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//        final ArrayList<String> users;
//        if (type.equalsIgnoreCase("Project")) {
//           users = mainProject.getAssignedUsers();
//        } else {
//            mainTask = getIntent().getParcelableExtra("Main Task");
//            String taskID = getIntent().getStringExtra("Task ID");
//            for (int x = 0;x<mainProject.getProjectTasks().size();x++) {
//                Task t = mainProject.getProjectTasks().get(x);
//                if (mainTask.getTaskName().equalsIgnoreCase(t.getTaskName())) {
//                    mainTask = t;
//                }
//            }
//            users = mainTask.getAssignedUsers();
//        }

        Query reference = root.child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User u = postSnapshot.getValue(User.class);
                    for (int y = 0; y < givenUsers.size(); y++) {
                        String s = givenUsers.get(y);
                        if (u.getUserName().equalsIgnoreCase(s)) {
                            assignedUsers.add(u);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        teammateList = (ListView) findViewById(R.id.teammateList);
        adapter = new ArrayAdapter(TeammatePage.this, android.R.layout.simple_list_item_1, givenUsers);
        teammateList.setAdapter(adapter);
        teammateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean value = view.isSelected();
                String userName = parent.getItemAtPosition(position).toString();
                if (value) {
                    value = false;
                    int index = -1;
                    for (int x = 0; x < selectedTeammates.size(); x++) {
                        String s = selectedTeammates.get(x);
                        if (userName.equalsIgnoreCase(s)) {
                            index = x;
                        }
                    }
                    selectedTeammates.remove(index);
                } else {
                    value = true;
                    if (!userName.equalsIgnoreCase(mainUser.getUserName())) {
                        selectedTeammates.add(userName);
                    }
                }
                view.setSelected(value);
            }
        });

    }

    public void sendEmail(View view) {
        if (selectedTeammates.size() > 0) {
            Log.i("Send email", "");
            ArrayList<User> chosenTeammates = new ArrayList<>();
            for (int x = 0;x<assignedUsers.size();x++) {
                User u = assignedUsers.get(x);
                for (int y = 0;y<selectedTeammates.size();y++) {
                    String s = selectedTeammates.get(y);
                    if (u.getUserName().equalsIgnoreCase(s)) {
                        chosenTeammates.add(u);
                    }
                }
            }
            ArrayList<String> emails = new ArrayList<>();
            for (int x = 0;x<chosenTeammates.size();x++) {
                User u = chosenTeammates.get(x);
                emails.add(u.getEmail());
            }
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            String[] reciepinet = new String[selectedTeammates.size()];
            for (int x =0;x<selectedTeammates.size();x++) {
                reciepinet[x] = chosenTeammates.get(x).getEmail();
            }

            emailIntent.putExtra(Intent.EXTRA_EMAIL,reciepinet );
            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                finish();
                Log.i("Finished sending email.", "");
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(TeammatePage.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }

        }
    }


}
