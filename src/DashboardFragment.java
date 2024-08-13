package androidsamples.java.tictactoe;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;
  private SharedViewModel mSharedViewModel;
  TextView mUsernameTxt;
  TextView mScoreTxt;
  OpenGamesAdapter adapter;
  List<String> oldEntries;
  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
    mSharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    oldEntries = new ArrayList<String>();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);
    mUsernameTxt = view.findViewById(R.id.txt_username);
    mScoreTxt = view.findViewById(R.id.txt_score);
    // TODO if a user is not logged in, go to LoginFragment
    if(mSharedViewModel.isLoggedIn == false){
      NavDirections action = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(action);
    }
    mUsernameTxt.setText("Welcome " + mSharedViewModel.username + "!");
    mScoreTxt.setText("Wins : " + mSharedViewModel.wins + "   " + "Losses : " + mSharedViewModel.losses);
    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
          mSharedViewModel.new2PlayerGame = 1;
          Log.d(TAG, "new2PlayerGame is set to 1");
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        mSharedViewModel.newGameLaunched = 1;
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        NavDirections action = DashboardFragmentDirections.actionGame(gameType, mSharedViewModel.username);
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });

    RecyclerView entriesList = view.findViewById(R.id.list);
    entriesList.setLayoutManager(new LinearLayoutManager(getActivity()));
    adapter = new OpenGamesAdapter(getActivity(), mNavController, mSharedViewModel);
    entriesList.setAdapter(adapter);
    getAllOpenMatches(true);
  }

  void getAllOpenMatches(boolean calledOnStart){
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference matchRef = firebaseDatabase.getReference("Match");
    matchRef.addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                int count = 0;
                if(map != null) {
                  oldEntries.clear();
                  for (Match entry : adapter.mEntries){
                    oldEntries.add(entry.id);
                  }
                  adapter.clearEntries();
                  for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Map singleMatch = (Map) entry.getValue();
                    Match match = new Match((String) singleMatch.get("player1"));
                    match.status = (String )singleMatch.get("status");
                    match.turn = (long) singleMatch.get("turn");
                    match.board = (List<String>) singleMatch.get("board");
                    match.id = entry.getKey();
                    if(Objects.equals(match.status, App.waiting)){
                      Log.d(TAG, "New match found = " + singleMatch.get("player1") + " " +match.id + " "+ match.status);
                      count++;
                      adapter.addEntry(match);
                    }
                  }

                  for(Match entry : adapter.mEntries){
                    if(!oldEntries.contains(entry.id)){
                      sendNotifications(entry);
                    }
                  }

                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {
                //handle databaseError
              }
            });

    Log.d(TAG, "old entries = " + oldEntries);
    Log.d(TAG, "new entries = " + adapter.mEntries);

    if(calledOnStart){
      setChildEventListeners();
    }
  }



  public void setChildEventListeners(){

    ChildEventListener GameChangedListener = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        getAllOpenMatches(false);
      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        getAllOpenMatches(false);
      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {

      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    };
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference matchRef = firebaseDatabase.getReference("Match");
    matchRef.addChildEventListener(GameChangedListener);

  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  private void sendNotifications(Match match){
    NotificationManager mNotificationManager;

    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(requireActivity().getApplicationContext(), "notify_001");
//    Intent ii = new Intent(mContext.getApplicationContext(), RootActivity.class);
//    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0);

    NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
    bigText.bigText("Play against against " + match.player1 + " now!");
    bigText.setBigContentTitle("New game has been created");
    bigText.setSummaryText("" + match.player1 + " created a new game!");

//    mBuilder.setContentIntent(pendingIntent);
    mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
    mBuilder.setContentTitle("New game!");
    mBuilder.setContentText(""+match.player1+" created a new game!");
    mBuilder.setPriority(Notification.PRIORITY_MAX);
    mBuilder.setStyle(bigText);

    mNotificationManager =
            (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      String channelId = "Your_channel_id";
      NotificationChannel channel = new NotificationChannel(
              channelId,
              "Channel human readable title",
              NotificationManager.IMPORTANCE_HIGH);
      mNotificationManager.createNotificationChannel(channel);
      mBuilder.setChannelId(channelId);
    }

    mNotificationManager.notify(0, mBuilder.build());
    Log.d(TAG, "User has been notified");
  }
}