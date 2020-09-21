package zfleming.solarispm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class ForgotCredentials extends AppCompatActivity {
    private EditText usernameText, passwordText, emailText1, emailText2;
    private String username, password, email1, email2;
    private FirebaseAuth fAuth;
    private ArrayList<User> existingUsers;
    private Notification notification;
    private static final int uniqueID = 1915121189;
    private static final String retrievedUsername = "Solaris PM: Retrived Username", retrievedPassword = "Solaris PM: Retrived Password";
    private NotificationChannel channel1, channel2;
    private NotificationManager manager;
    private NotificationManagerCompat mainManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotcredentials);
        usernameText = (EditText) findViewById(R.id.usernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        emailText1 = (EditText) findViewById(R.id.emailText1);
        emailText2 = (EditText) findViewById(R.id.emailText2);
        fAuth = FirebaseAuth.getInstance();
        existingUsers = new ArrayList<>();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel1 = new NotificationChannel(
                    retrievedUsername,
                    "Retrieved Username",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("Your Username");
            channel2 = new NotificationChannel(
                    retrievedPassword,
                    "Retrieved Password",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("Your Password");
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            mainManager = NotificationManagerCompat.from(this);
        }
    }

    public void checkUsername(View view) {
        username = usernameText.getText().toString();
        email1 = emailText1.getText().toString();
        User foundUser = null;
        boolean userExist = false;
        if (!username.isEmpty() && !email1.isEmpty()) {
            for (int x = 0; x < existingUsers.size(); x++) {
                User u = existingUsers.get(x);
                if (u.getUserName().equalsIgnoreCase(username)) {
                    if (u.getEmail().equalsIgnoreCase(email1)) {
                        userExist = true;
                        foundUser = u;
                    }
                }
            }
            if (userExist) {
                notification = new NotificationCompat.Builder(this, retrievedPassword)
                        .setSmallIcon(R.drawable.solarispmlogofinal)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("Password Recovery")
                        .setContentText("Your Password:" + foundUser.getPassword())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                mainManager.notify(1, notification);
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(ForgotCredentials.this, "Don't recognize creditentials.", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(ForgotCredentials.this, "Don't leave the fields blank.", Toast.LENGTH_SHORT).show();
        }

    }

    public void checkPassword(View view) {
        password = passwordText.getText().toString();
        email2 = emailText2.getText().toString();
        User foundUser = null;
        boolean userExist = false;
        if (!password.isEmpty() && !email2.isEmpty()) {
            for (int x = 0; x < existingUsers.size(); x++) {
                User u = existingUsers.get(x);
                if (u.getPassword().equalsIgnoreCase(password)) {
                    if (u.getEmail().equalsIgnoreCase(email2)) {
                        userExist = true;
                        foundUser = u;
                    }
                }
            }
            if (userExist) {
                notification = new NotificationCompat.Builder(this, retrievedUsername)
                        .setSmallIcon(R.drawable.solarispmlogofinal)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("Username Recovery")
                        .setContentText("Your Username:" + foundUser.getUserName())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                mainManager.notify(1, notification);
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(ForgotCredentials.this, "Don't recognize creditentials.", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(ForgotCredentials.this, "Don't leave the fields blank.", Toast.LENGTH_SHORT).show();
        }
    }
}
