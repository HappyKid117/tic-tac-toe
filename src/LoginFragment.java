package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private  SharedViewModel mSharedViewModel;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRef;
    EditText mEditUsername;
    EditText mEditPassword;
    TextInputLayout mPasswordLayout;
    TextInputLayout mUsernameLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("User");
        // TODO if a user is logged in, go to Dashboard
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mEditUsername = view.findViewById(R.id.edit_email);
        mEditPassword = view.findViewById(R.id.edit_password);
        mPasswordLayout = view.findViewById(R.id.edit_password_layout);
        mUsernameLayout = view.findViewById(R.id.edit_email_layout);
        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    // TODO implement sign in logic
                    String username = mEditUsername.getText().toString();
                    String password = mEditPassword.getText().toString();
                    mUsernameLayout.setError("");
                    mPasswordLayout.setError("");
                    if(username.equals("")){
                        mUsernameLayout.setError("Enter a valid username");
                        return;
                    }
                    User user = new User(username, password);
                    Log.d(TAG, "Created new User object with Username : " + username + " Password : " + password);

                    userRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                            if(userDataSnapshot.exists()){
                                // user exists in the database
                                Log.d(TAG, "User " + username + " exists in the database");
                                Map<String, Object> map = (Map<String, Object>) userDataSnapshot.getValue();
                                if(map.get("password").equals(password)){
                                    mSharedViewModel.isLoggedIn = true;
                                    mSharedViewModel.username = username;
                                    mSharedViewModel.wins = (long) map.get("wins");
                                    mSharedViewModel.losses = (long) map.get("losses");

                                    Log.d(TAG, "isLoggedIn set to true");
                                    NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                    Navigation.findNavController(view).navigate(action);
                                }else{
                                    mPasswordLayout.setError("Incorrect Password");
                                    mEditPassword.setText("");
                                }
                            }else{
                                // user does not exist in the database
                                Log.d(TAG, "User DOES NOT " + username + " exists in the database");
                                userRef.child(username).setValue(user);
                                mSharedViewModel.isLoggedIn = true;
                                mSharedViewModel.username = username;
                                mSharedViewModel.losses = 0;
                                mSharedViewModel.wins = 0;
                                Log.d(TAG, "isLoggedIn set to true");
                                NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                Navigation.findNavController(view).navigate(action);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                });

        return view;
    }
    // No options menu in login fragment.
}