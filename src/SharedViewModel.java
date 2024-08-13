package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public final String TAG = "SharedViewModel";

    public String username;
    public String player1Symbol;
    public String player2Symbol;
    public long wins;
    public long losses;
    public boolean isLoggedIn; // 0 = no, 1 = yes
    public int new2PlayerGame; // 0 = no, 1 = yes
    public int newGameLaunched;
    public boolean inGameFragment;


    public SharedViewModel(){
        username = "NULL";
        isLoggedIn = false;
        Log.d(TAG, "isLoggedIn set to false");
        player1Symbol = "X";
        player2Symbol = "O";
        new2PlayerGame = 0;
        Log.d(TAG, "new2PlayerGame set to false");
        newGameLaunched = 0;
        inGameFragment = false;
    }
}
