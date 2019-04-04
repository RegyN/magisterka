package tracks.singlePlayer;

import java.util.ArrayList;

public class GameResult {
    String GameName;
    int GameNumber;
    // {Win/Loss, Result, Timesteps}
    ArrayList<PlayoutResult> Results;

    public void AddResult(double[] result, int lvl){
        Results.add(new PlayoutResult(result, lvl));
    }

    public GameResult(String name, int number){
        GameName = name;
        GameNumber = number;
        Results = new ArrayList<>();
    }
}
