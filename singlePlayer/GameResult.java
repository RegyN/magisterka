package tracks.singlePlayer;

import java.util.ArrayList;

public class GameResult {
    String GameName;
    int GameNumber;
    // {Win/Loss, Result, Timesteps}
    public ArrayList<PlayoutResult> Results;

    public void addResult(double[] result, int lvl){
        Results.add(new PlayoutResult(result, lvl));
    }
    
    public void addResult(PlayoutResult res){
        Results.add(res);
    }

    public GameResult(String name, int number){
        GameName = name;
        GameNumber = number;
        Results = new ArrayList<>();
    }
    
    public int numberOfResults(){
        return Results.size();
    }
    
    public int numberOfResults(int level){
        return (int)Results.stream().filter(p -> p.GameLevel == level).count();
    }
}
