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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class InitialPage extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialpage);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query reference = root.child("Users");
        final ArrayList<User> existingUsers = new ArrayList<>();
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
        fAuth = FirebaseAuth.getInstance();
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = fAuth.getCurrentUser();
                try {
                    if (fUser != null) {
                        if (fUser.getDisplayName() != null) {
                            if (!fUser.getDisplayName().isEmpty()) {
                                boolean finalCheck = fUser.getDisplayName().contains("default") || fUser.getDisplayName().contains("Default") || fUser.getDisplayName().contains("DEFAULT");
                                if (!finalCheck) {
                                    FirebaseAuth.getInstance().signOut();
                                }
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        };
    }

    public void goToSignUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        fAuth = FirebaseAuth.getInstance();
        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = fAuth.getCurrentUser();
                try {
                    if (fUser != null) {
                        if (fUser.getDisplayName() != null) {
                            if (!fUser.getDisplayName().isEmpty()) {
                                boolean finalCheck = fUser.getDisplayName().contains("default") || fUser.getDisplayName().contains("Default") || fUser.getDisplayName().contains("DEFAULT");
                                if (!finalCheck) {
                                    FirebaseAuth.getInstance().signOut();
                                }
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        };
    }
}
