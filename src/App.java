package androidsamples.java.tictactoe;

import android.app.Application;
import android.content.res.Resources;

public class App extends Application {
    private static Resources resources;

    public static String ongoing = "ONGOING";
    public static String finished = "FINISHED";
    public static String player1won = "PLAYER 1 WON";
    public static String player2won = "PLAYER 2 WON";
    public static String tie = "TIE";

    public static String waiting = "WAITING";

    @Override
    public void onCreate() {
        super.onCreate();

        resources = getResources();
    }

    public static Resources getAppResources() {
        return resources;
    }
}
