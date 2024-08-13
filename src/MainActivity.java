package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private SharedViewModel mSharedViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mSharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, "Option has been selected : " + item);
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked");
      // TODO handle log out
      if (item.getItemId() == R.id.menu_logout){
        if(mSharedViewModel.inGameFragment){
          Toast.makeText(this, "Please finish the game", Toast.LENGTH_SHORT).show();
        }else{
          mSharedViewModel.isLoggedIn = false;
          mSharedViewModel.username = "";
          NavController mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
          mNavController.navigate(R.id.loginFragment);
        }
      }
      return true;
    }

    if (item.getItemId() == R.id.menu_dereg){
      if(mSharedViewModel.inGameFragment){
        Toast.makeText(this, "Please finish the game", Toast.LENGTH_SHORT).show();
      }else {
        FirebaseDatabase.getInstance().getReference().child("User").child(mSharedViewModel.username).setValue(null);
        mSharedViewModel.isLoggedIn = false;
        mSharedViewModel.username = "";
        NavController mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mNavController.navigate(R.id.loginFragment);
      }
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}