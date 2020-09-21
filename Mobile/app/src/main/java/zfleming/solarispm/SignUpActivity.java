package zfleming.solarispm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private String fname, lname, email, uname, pword;
    private TextView firstName, lastName, emailText, userName, password;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private boolean emailExist = false, userNameExist = false;
    private ProgressBar pBar;
    private ArrayList<User> existingUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        firstName = (TextView) findViewById(R.id.firstNameText);
        lastName = (TextView) findViewById(R.id.lastNameText);
        emailText = (TextView) findViewById(R.id.emailText);
        userName = (TextView) findViewById(R.id.usernameText);
        password = (TextView) findViewById(R.id.passwordText);
        pBar = (ProgressBar) findViewById(R.id.progressBar);
        existingUsers = new ArrayList<>();
        fAuth = FirebaseAuth.getInstance();
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
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = fAuth.getCurrentUser();
                try {
                    if (fUser != null) {
                        if (fUser.getDisplayName() != null) {
                            if (!fUser.getDisplayName().isEmpty()) {
                                String regex = "/*[dD][eE][fF][aA][uU][lL][tT]*";
                                boolean result = !fUser.isAnonymous() && (!fUser.getDisplayName().matches(regex) /*|| !fUser.getDisplayName().contains("Default") || !fUser.getDisplayName().contains("DEFAULT")*/);
                                if (!result) {
                                    User user = null;
                                    for (int x = 0; x < existingUsers.size(); x++) {
                                        if (fUser.getEmail().equalsIgnoreCase(existingUsers.get(x).getEmail())) {
                                            user = existingUsers.get(x);
                                        }
                                    }
                                    if (user.getProjects() == null) {
                                        user.setProjects(new ArrayList<String>());
                                    } else if (user.getProjects().isEmpty()) {
                                        user.setProjects(new ArrayList<String>());
                                    }
                                    Intent intent = new Intent(SignUpActivity.this, MainPage.class);
                                    intent.putExtra("Main User", (Parcelable) user);
                                    intent.putExtra("User projects", user.getProjects());
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        };
    }


    public void checkCredentials(View view) {
        fname = firstName.getText().toString();
        lname = lastName.getText().toString();
        uname = userName.getText().toString();
        email = emailText.getText().toString();
        pword = password.getText().toString();
        pBar.setVisibility(View.VISIBLE);
        if (!fname.isEmpty() && !lname.isEmpty() && !uname.isEmpty() && !email.isEmpty() && !pword.isEmpty()) {
            for (int x = 0; x < existingUsers.size(); x++) {
                if (email.equalsIgnoreCase(existingUsers.get(x).getEmail())) {
                    emailExist = true;
                } else if (uname.equalsIgnoreCase(existingUsers.get(x).getUserName())) {
                    userNameExist = true;
                }
            }
            if (!emailExist) {
                if (!userNameExist) {
                    FirebaseUser fUser = fAuth.getCurrentUser();
                    String regex = "/*[dD][eE][fF][aA][uU][lL][tT]*", defaultName = "";
                    if (fUser != null) {
                        defaultName = fUser.getDisplayName();
                    }
                    final boolean result = defaultName.matches(regex) || defaultName.isEmpty();
                    if (result) {
                        if (pword.length() >= 6) {
                            fAuth.createUserWithEmailAndPassword(email, pword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    boolean result = task.isSuccessful();
                                    if (result) {
                                        fAuth.signInWithEmailAndPassword(email, pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                boolean result = task.isSuccessful();
                                                if (result) {
                                                    final User user = new User(fname, lname, pword, email, uname);
                                                    user.setProjects(new ArrayList<String>());
                                                    user.getProjects().add("Dummy");
                                                    String id = fAuth.getCurrentUser().getUid();
                                                    DatabaseReference currentDb = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
                                                    currentDb.child("email").setValue(email);
                                                    currentDb.child("firstName").setValue(fname);
                                                    currentDb.child("lastName").setValue(lname);
                                                    currentDb.child("password").setValue(pword);
                                                    currentDb.child("projects").setValue(user.getProjects());
                                                    currentDb.child("userName").setValue(uname).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            boolean result = task.isSuccessful();
                                                            if (result) {
                                                                pBar.setVisibility(View.GONE);
                                                                Intent intent = new Intent(SignUpActivity.this, MainPage.class);
                                                                intent.putExtra("Main User", (Parcelable) user);
                                                                intent.putExtra("User projects", user.getProjects());
                                                                startActivity(intent);
                                                                Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Registration failed.", Toast.LENGTH_LONG).show();
                                        pBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration failed. Password too short.", Toast.LENGTH_LONG).show();
                            pBar.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed.", Toast.LENGTH_LONG).show();
                        pBar.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Registration failed. Username already in use.", Toast.LENGTH_LONG).show();
                    pBar.setVisibility(View.INVISIBLE);
                }
            } else {
                Toast.makeText(SignUpActivity.this, "Registration failed. Email already in use.", Toast.LENGTH_LONG).show();
                pBar.setVisibility(View.INVISIBLE);
            }
        } else {
            pBar.setVisibility(View.INVISIBLE);
            Toast.makeText(SignUpActivity.this, "Don't leave any of the fields empty", Toast.LENGTH_LONG).show();
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
