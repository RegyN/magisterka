package tracks.singlePlayer;

import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;

public class TestResult {
    String ControllerName;
    public ArrayList<GameResult> Results;

    TestResult(String controllerName){
        ControllerName = controllerName;
        Results = new ArrayList<>();
    }
    
    public void addResult(GameResult res){
        Results.add(res);
    }
    
    public void addResult(PlayoutResult res, int gameNum, String gameName){
        boolean found = false;
        for(var r : Results){
            if(r.GameNumber == gameNum){
                r.addResult(res);
                found = true;
                break;
            }
        }
        if(!found){
            GameResult gRes = new GameResult(gameName, gameNum);
            gRes.addResult(res);
            this.addResult(gRes);
        }
    }
    
    public int numberOfGames(){
        return Results.size();
    }
}
