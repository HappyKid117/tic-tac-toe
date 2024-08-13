package androidsamples.java.tictactoe;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String password;
    public String player1Symbol;
    public String player2Symbol;
    public int wins;
    public int losses;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        player1Symbol = "X";
        player2Symbol = "O";
        wins = 0;
        losses = 0;
    }

}
