package zfleming.solarispm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.UUID;

public class ChangeCredentials extends AppCompatActivity {
    private User mainUser;
    private EditText changeUsername, changePassword;
    private Button submit1, submit2;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseAuth fAuth;
    private Notification notification;
    private static final String changedUsername = "Solaris PM: New Username", changedPassword = "Solaris PM: New Password";
    private NotificationChannel channel1, channel2;
    private NotificationManager manager;
    private NotificationManagerCompat mainManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changecredentials);
        changeUsername = (EditText) findViewById(R.id.newUsernameText);
        changePassword = (EditText) findViewById(R.id.newPasswordText);
        submit1 = (Button) findViewById(R.id.submitBttn1);
        submit2 = (Button) findViewById(R.id.submitBttn2);
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
                }
            }

        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel1 = new NotificationChannel(
                    changedUsername,
                    "New Username",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("Your New Username");
            channel2 = new NotificationChannel(
                    changedPassword,
                    "New Password",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("Your New Password");
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            mainManager = NotificationManagerCompat.from(this);
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

    public void changeUsername(View view) {
        String newUsername = changeUsername.getText().toString();
        User foundUser = null;
        boolean usernameExist = false;
        if (newUsername.isEmpty()) {
            final ArrayList<User> existingUsers = new ArrayList<>();
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            Query reference = root.child("Users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        existingUsers.add(postSnapshot.getValue(User.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            for (int x = 0; x < existingUsers.size(); x++) {
                User u = existingUsers.get(x);
                if (u.getUserName().equalsIgnoreCase(newUsername)) {
                        usernameExist = true;
                }
            }
            if (!usernameExist) {
                FirebaseUser fUser = fAuth.getCurrentUser();
                root.child(fUser.getUid()).child(mainUser.getUserName()).setValue(newUsername);
                mainUser.setUserName(newUsername);
                notification = new NotificationCompat.Builder(this, changedUsername)
                        .setSmallIcon(R.drawable.solarispmlogofinal)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("New Username")
                        .setContentText("Your New Username:" + newUsername)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                mainManager.notify(1, notification);
                Intent intent = new Intent(this, User.class);
                intent.putExtra("Main User", (Parcelable) mainUser);
                intent.putExtra("User projects", mainUser.getProjects());
                startActivity(intent);
            } else {
                Toast.makeText(ChangeCredentials.this, "Username is already in use.", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(ChangeCredentials.this, "Don't leave the fields blank.", Toast.LENGTH_SHORT).show();
        }
    }

    public void changePassword(View view) {
       String newPassword = changePassword.getText().toString();
        if (!newPassword.isEmpty()) {
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            FirebaseUser fUser = fAuth.getCurrentUser();
            root.child(fUser.getUid()).child(mainUser.getPassword()).setValue(newPassword);
            mainUser.setPassword(newPassword);
            notification = new NotificationCompat.Builder(this, changedPassword)
                    .setSmallIcon(R.drawable.solarispmlogofinal)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("New Password")
                    .setContentText("Your New Password:" + newPassword)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            mainManager.notify(1, notification);
            Intent intent = new Intent(this, User.class);
            intent.putExtra("Main User", (Parcelable) mainUser);
            intent.putExtra("User projects", mainUser.getProjects());
            startActivity(intent);
        } else {
            Toast.makeText(ChangeCredentials.this, "Don't leave the fields blank.", Toast.LENGTH_SHORT).show();
        }
    }
}
