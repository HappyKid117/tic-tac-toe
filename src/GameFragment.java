package androidsamples.java.tictactoe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameFragment extends Fragment {
  private String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  private final Button[] mButtons = new Button[GRID_SIZE];
  private TextView tvPlayer1;
  private TextView tvPlayer2;
  private TextView tvGameId;
  private NavController mNavController;
  public GameModel mGameModel;
  private SharedViewModel mSharedViewModel;
  FirebaseDatabase firebaseDatabase;
  DatabaseReference matchRef;
  private GameFragmentArgs mArgs;
  private boolean ignoreStatusUpdate;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment

    mSharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    mSharedViewModel.inGameFragment = true;
    TAG += " "+mSharedViewModel.username;
    // Extract the argument passed with the action in a type-safe way
    mArgs = GameFragmentArgs.fromBundle(getArguments());
    ignoreStatusUpdate = false;
    Log.d(TAG, "New game type = " + mArgs.getGameType());

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        // TODO show dialog only when the game is still in progress
        AlertDialog dialog;
        if(mGameModel.matchStatus == App.ongoing) {
          dialog = new AlertDialog.Builder(requireActivity())
                  .setTitle(R.string.confirm)
                  .setMessage(R.string.forfeit_game_dialog_message)
                  .setPositiveButton(R.string.yes, (d, which) -> {
                    // TODO update loss count
                    endGame(true);
                    //              mNavController.popBackStack();
                  })
                  .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                  .create();
        }else{
          dialog = new AlertDialog.Builder(requireActivity())
                  .setTitle(R.string.confirm)
                  .setMessage(R.string.close_game)
                  .setPositiveButton(R.string.yes, (d, which) -> {
                    matchRef.child(mGameModel.matchId).child("status").setValue(App.finished);
                    mNavController.popBackStack();
                  })
                  .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                  .create();
        }
        dialog.show();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    firebaseDatabase = FirebaseDatabase.getInstance();
    matchRef = firebaseDatabase.getReference("Match");
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if(mSharedViewModel.isLoggedIn == false) {
      NavController mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
      mNavController.navigate(R.id.loginFragment);
    }

    mNavController = Navigation.findNavController(view);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    tvPlayer1 = view.findViewById(R.id.player1);
    tvPlayer2 = view.findViewById(R.id.player2);
    tvGameId = view.findViewById(R.id.gameId);

    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        Log.d(TAG, "Button " + finalI + " clicked");
        makeMove(finalI);
      });
    }

    // Convert to gameMode
    int gameMode;
    if(mArgs.getGameType().equals(getString(R.string.one_player))){
      gameMode = 0;
    }else{
      gameMode = 1;
    }
    // Set up the board

    mGameModel = new GameModel(gameMode);
    if(mSharedViewModel.newGameLaunched == 1){
      Log.d(TAG, "Setting new game launched to 0");
//      mGameModel.init(gameMode);
    }

    Log.d(TAG, "Game mode set to " + gameMode);
    mGameModel.setSymbols(mSharedViewModel.player1Symbol, mSharedViewModel.player2Symbol);
    if(gameMode == 0){
      mGameModel.setPlayers(mSharedViewModel.username, "Computer");
      mGameModel.setBoard();
      mGameModel.goOn = 1;
      mGameModel.turn = 1;
      mGameModel.matchStatus = App.ongoing;

    }
    if(gameMode == 1 && mSharedViewModel.newGameLaunched == 1){
      Log.d(TAG, "Game mode is 1 so going here");
      if(mSharedViewModel.new2PlayerGame == 1){
        Match match = new Match(mSharedViewModel.username);
        String key = matchRef.push().getKey();
        mGameModel.matchId = key;
        mGameModel.player1 = mSharedViewModel.username;
        mGameModel.player2 = "Waiting ...";
        mGameModel.matchStatus = App.waiting;
        mGameModel.goOn = 1;
        mGameModel.board = match.board;
        mGameModel.turn = 1;
        matchRef.child(key).setValue(match);
        updateUI();
      }else{
        mGameModel.matchId = mArgs.getMatchId();
        setMatch(mGameModel.matchId);
        mGameModel.matchStatus = App.ongoing;
        matchRef.child(mGameModel.matchId).child("status").setValue(App.ongoing);
        mGameModel.goOn = 2;
      }
    }

    if(mGameModel.gameMode == 0) updateUI();
    setChildEventListeners();
    mSharedViewModel.newGameLaunched = 0;
  }

  public void setChildEventListeners(){
    ValueEventListener newPlayerJoinedListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.getValue() == null) return;
        Log.d(TAG, "New player joined");
        mGameModel.player2 = snapshot.getValue().toString();
        mGameModel.matchStatus = App.ongoing;

        updateUI();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    };

    if(mGameModel.goOn == 1 && mGameModel.gameMode == 1) {
      Log.d(TAG, "Setting listener for " + mGameModel.matchId);
      matchRef.child(mGameModel.matchId).child("player2").addValueEventListener(newPlayerJoinedListener);
    }

    ValueEventListener boardChanged = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.getValue() == null) return;
        Log.d(TAG, "Board has been updated");
        mGameModel.board = (List<String>) snapshot.getValue();
        if(Objects.equals(mGameModel.matchStatus, App.ongoing)){
          mGameModel.turn = ((mGameModel.turn)%2) + 1;
          Log.d(TAG, "Current turn = " + mGameModel.turn);


          if(mGameModel.checkWin() != 0 || mGameModel.checkTie() == 1){
            endGame(false);
          }
        }
        updateUI();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    };

    ValueEventListener checkStatus = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "Status has been updated" + snapshot.getValue());
        if(ignoreStatusUpdate) return;
        if(mGameModel.matchStatus == App.ongoing){
          mGameModel.matchStatus = (String )snapshot.getValue();
          if(mGameModel.matchStatus != App.ongoing){
            endGame(false);
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    };

    if(mGameModel.goOn == 1 && mGameModel.gameMode == 1) {
      Log.d(TAG, "Setting listener for new player " + mGameModel.matchId);
      matchRef.child(mGameModel.matchId).child("player2").addValueEventListener(newPlayerJoinedListener);
    }

    if(mGameModel.gameMode == 1){
      Log.d(TAG, "Setting listener for board changes : " + mGameModel.matchId);
      matchRef.child(mGameModel.matchId).child("board").addValueEventListener(boardChanged);
    }

    if(mGameModel.gameMode == 1){
      Log.d(TAG, "Setting listener for retreat " + mGameModel.matchStatus);
      matchRef.child(mGameModel.matchId).child("status").addValueEventListener(checkStatus);
    }
  }

  public void setMatch(String id){
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference matchRef = firebaseDatabase.getReference("Match");
    matchRef.child(id).addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                mGameModel.matchStatus = (String )map.get("status");
                mGameModel.matchStatus = App.ongoing;
                mGameModel.player1 = (String) map.get("player1");
                mGameModel.player2 = mSharedViewModel.username;
                matchRef.child(id).child("player2").setValue(mSharedViewModel.username);
                mGameModel.board = (List<String>) map.get("board");
                Log.d(TAG, "Player1 = " + map.get("player1") + " turn = " + mGameModel.turn);
                updateUI();
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {
                //handle databaseError
              }
            });
  }

  public void makeMove(final int finalI){
    Log.d(TAG, "Turn = " + mGameModel.turn + " Go On : " + mGameModel.goOn);
    if(Objects.equals(mGameModel.matchStatus, App.waiting)){
      Toast.makeText(requireContext(), "Please wait for a player to join", Toast.LENGTH_SHORT).show();
      return;
    }
    if(mGameModel.turn != mGameModel.goOn){
      Toast.makeText(requireContext(), "Please wait for your turn", Toast.LENGTH_SHORT).show();
      return;
    }
    if(mGameModel.makeMove(finalI)){
      if(mGameModel.gameMode == 1) matchRef.child(mGameModel.matchId).child("board").setValue(mGameModel.board);
      updateUI();

      if(mGameModel.gameMode == 0){

        if(mGameModel.checkTie() == 1 || mGameModel.checkWin() == 1){
          endGame(false);
        }else{
          mGameModel.getMove();
          updateUI();
          if(mGameModel.checkWin() == 2){
            endGame(false);
          }
        }
      }
    }else{

    }
  }

  @SuppressLint("SetTextI18n")
  public void updateUI(){
    for(int i=0; i<GRID_SIZE; i++){
      String symbol = "";
      if(Objects.equals(mGameModel.board.get(i), mGameModel.player1)){
        symbol = mSharedViewModel.player1Symbol;
      }
      if(Objects.equals(mGameModel.board.get(i), mGameModel.player2)){
        symbol = mSharedViewModel.player2Symbol;
      }
      mButtons[i].setText(symbol);
    }

    if(mGameModel.goOn == 1){
      tvPlayer1.setText("You\n" + mGameModel.player1);
      tvPlayer2.setText("Opponent\n" + mGameModel.player2);
    }else{
      tvPlayer1.setText("Opponent\n" + mGameModel.player1);
      tvPlayer2.setText("You\n" + mGameModel.player2);
    }

    if(mGameModel.gameMode == 1){
      tvGameId.setText("Game Code : " + Math.abs((mGameModel.matchId.hashCode() % 8999) + 1001));
    }
    quirks();
  }

  public void endGame(boolean backButtonPressed){

    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    String status = App.finished;
    // Set the message show for the Alert time
    mSharedViewModel.new2PlayerGame = 0;
    if(mGameModel.checkTie() == 1){
      builder.setTitle("Tie!");
      builder.setMessage("Wins\n" + mSharedViewModel.wins + " -> " + (mSharedViewModel.wins) + "\n" +
              "Losses\n" + mSharedViewModel.losses + " -> " + (mSharedViewModel.losses));
      status = App.tie;
      Toast.makeText(requireContext(), "Tie!", Toast.LENGTH_SHORT).show();
    }

    Log.d(TAG, "Game is ending : Status = " + mGameModel.matchStatus + " goOn = " + mGameModel.goOn + " Win? = " + mGameModel.checkWin());
    if(mGameModel.goOn == mGameModel.checkWin() || ((Objects.equals(mGameModel.matchStatus, App.player1won)) && mGameModel.goOn==1) || ((Objects.equals(mGameModel.matchStatus, App.player2won)) && mGameModel.goOn==2)){
      Log.d(TAG, "Winning Dialog");
      builder.setTitle("Victory");
      builder.setMessage("Wins\n" + mSharedViewModel.wins + " -> " + (mSharedViewModel.wins+1));
      mSharedViewModel.wins += 1;
      if(mGameModel.goOn == 1){
        status = App.player1won;
      }else{
        status = App.player2won;
      }
      Toast.makeText(requireContext(), "You Win!", Toast.LENGTH_SHORT).show();
    }

    if(backButtonPressed || ((mGameModel.goOn != mGameModel.checkWin()) && mGameModel.checkWin()!=0) || ((Objects.equals(mGameModel.matchStatus, App.player1won)) && mGameModel.goOn==2) || ((Objects.equals(mGameModel.matchStatus, App.player2won)) && mGameModel.goOn==1)){
      builder.setTitle("Defeat");
      builder.setMessage("Losses\n" + mSharedViewModel.losses + " -> " + (mSharedViewModel.losses+1));
      mSharedViewModel.losses += 1;
      if(mGameModel.goOn == 1){
        status = App.player2won;
      }else{
        status = App.player1won;
      }
      Toast.makeText(requireContext(), "You Lose!", Toast.LENGTH_SHORT).show();
    }

    // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
    builder.setCancelable(false);

    builder.setPositiveButton("Ok", (DialogInterface.OnClickListener) (dialog, which) -> {
      if(backButtonPressed){
        mNavController.popBackStack();
      }else{
        mNavController.navigate(R.id.dashboardFragment);
      }
    });

    if(mGameModel.gameMode == 1){
      ignoreStatusUpdate = true;
      matchRef.child(mGameModel.matchId).child("status").setValue(status);
    }
    firebaseDatabase.getReference().child("User").child(mSharedViewModel.username).child("wins").setValue(mSharedViewModel.wins);
    firebaseDatabase.getReference().child("User").child(mSharedViewModel.username).child("losses").setValue(mSharedViewModel.losses);

    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  @Override
  public void onStop() {
    mSharedViewModel.inGameFragment = false;
    super.onStop();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  // Quirks
  private void quirks() {
    shake();
  }

  private void shake() {
    Animation shake = AnimationUtils.loadAnimation(requireActivity(), R.anim.shake);
    for(int i=0; i<GRID_SIZE; i++){
      mButtons[i].startAnimation(shake);
    }
  }
}