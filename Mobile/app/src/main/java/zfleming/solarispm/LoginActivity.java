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
import android.widget.EditText;
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

import org.w3c.dom.Text;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private static Button login;
    private static TextView signUp, forgot;
    private EditText userNameText, passwordText;
    private String username, password;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private ArrayList<User> existingUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (Button) findViewById(R.id.loginBttn);
        signUp = (TextView) findViewById(R.id.signUpText);
        forgot = (TextView) findViewById(R.id.forgotText);
        userNameText = (EditText) findViewById(R.id.usernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin(view);
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUp(view);
            }
        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToForget(view);
            }
        });
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
                                    Intent intent = new Intent(LoginActivity.this, MainPage.class);
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


    public void checkLogin(View view) {
        username = userNameText.getText().toString();
        password = passwordText.getText().toString();
        if (!username.isEmpty() && !password.isEmpty()) {
            boolean userExist = false;
            int index = - 1;
            for (int x = 0; x < existingUsers.size(); x++) {
               User u = existingUsers.get(x);
                if (u.getUserName().equalsIgnoreCase(username)) {
                    if (u.getPassword().equalsIgnoreCase(password)) {
                        userExist = true;
                        index = x;
                        break;
                    }
                }
            }
            if (userExist) {
                final User user = existingUsers.get(index);
                fAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        boolean result = task.isSuccessful();
                        if (result) {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainPage.class);
                            intent.putExtra("Main User", (Parcelable) user);
                            intent.putExtra("User projects", user.getProjects());
                            startActivity(intent);
                        }
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "User unrecognizable", Toast.LENGTH_LONG).show();

            }
        } else {
            Toast.makeText(LoginActivity.this, "Don't leave any of the fields empty", Toast.LENGTH_LONG).show();

        }

    }

    public void goToForget(View view) {
        Intent intent = new Intent(this, ForgotCredentials.class);
        startActivity(intent);
    }

    public void goToSignUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
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
    }
}
