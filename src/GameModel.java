package androidsamples.java.tictactoe;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameModel extends ViewModel {
    public int gameMode;
    public List<String> board;
    public String matchStatus;
    public String matchId;
    public String player1;
    public String player2;
    public String symbol1;
    public String symbol2;
    public long turn;
    public long goOn;
    public List<HashSet<Integer>> winConditions;

    private static final String TAG = "GameModel";

    public GameModel(int gameMode){
        Log.d(TAG, "New object created");
        init(gameMode);
    }

    public void init(int gameMode){
        Log.d(TAG, "Game mode set to " + gameMode);
        this.gameMode = gameMode;
        winConditions = new ArrayList<>();
        Collections.addAll(winConditions,
                new HashSet<>(Arrays.asList(0,1,2)),
                new HashSet<>(Arrays.asList(3,4,5)),
                new HashSet<>(Arrays.asList(6,7,8)),

                new HashSet<>(Arrays.asList(0,3,6)),
                new HashSet<>(Arrays.asList(1,4,7)),
                new HashSet<>(Arrays.asList(2,5,8)),

                new HashSet<>(Arrays.asList(0,4,8)),
                new HashSet<>(Arrays.asList(2,4,6)));
    }

    public void setPlayers(String player1, String player2){
        this.player1 = player1;
        this.player2 = player2;
    }

    public void setSymbols(String symbol1, String symbol2){
        this.symbol1 = symbol1;
        this.symbol2 = symbol2;
    }

    public void setTurn(int turn){
        this.turn = turn;
    }

    public void setBoard(){
        if(gameMode == 0){
            board = new ArrayList<>();
            for (int i = 0; i < 9; i++)  {
                board.add("");
            }
        }
    }

    public boolean isValidMove(int place){
        return Objects.equals(board.get(place), "");
    }

    public boolean makeMove(int place){
        if(isValidMove(place)){
            if(goOn == 1) {
                board.set(place, player1);
            }else{
                board.set(place, player2);
            }
                return true;
        }else{
            return false;
        }
    }

    public void getMove(){
        if(gameMode == 0){
            for(int i=0; i<9; i++){
                if(Objects.equals(board.get(i), "")){
                    board.set(i, player2);
                    return;
                }
            }
        }
    }

    public int checkTie(){
        if(checkWin() == 0){
            for(int i=0; i<9; i++){
                if(Objects.equals(board.get(i), "")) return 0;
            }
            return 1;
        }
        return 0;
    }

    public int checkWin(){
        HashSet<Integer> player1Set = new HashSet<>();
        HashSet<Integer> player2Set = new HashSet<>();
        for(int i=0; i<9; i++){
            if(Objects.equals(board.get(i), player1)){
                player1Set.add(i);
            }
            if(Objects.equals(board.get(i), player2)){
                player2Set.add(i);
            }
        }

        if(checkSubset(player1Set)){
            return 1;
        }
        if(checkSubset(player2Set)){
            return 2;
        }
        return 0;
    }

    public boolean checkSubset(HashSet<Integer> bigS){
        for(int i=0; i<winConditions.size(); i++){
            HashSet<Integer> smallS = winConditions.get(i);
            if(checkSubsetHelper(smallS, bigS)){
                return true;
            }
        }
        return false;
    }

    public boolean checkSubsetHelper(HashSet<Integer> smallS, HashSet<Integer> bigS){
        for (Integer small : smallS) {
            if (!bigS.contains(small)) {
                return false;
            }
        }
        return  true;
    }
}
