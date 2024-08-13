package androidsamples.java.tictactoe;

import java.util.ArrayList;
import java.util.List;

public class Match {
    public String id;
    public String player1;
    public String player2;
    public String status;
    public long turn;
    public List<String> board;

    public Match() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Match(String username) {
        this.player1 = username;
        this.status = App.waiting;
        this.turn = 1;
        setBoard();
    }

    public void setBoard(){
        board = new ArrayList<>();
        for (int i = 0; i < 9; i++)  {
            board.add("");
        }
    }
}
