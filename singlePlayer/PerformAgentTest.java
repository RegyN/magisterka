package tracks.singlePlayer;

import tools.Utils;
import tools.com.google.gson.Gson;
import tracks.ArcadeMachine;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class PerformAgentTest {
    public static void main(String [] args){
        String Controller = "tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo.Agent";

        String spGamesCollection =  "examples/all_games_sp.csv";
        String[][] games = Utils.readGames(spGamesCollection);
        Random generator = new Random();

        // Game and level to play
        int[] gamesToPlay = {63};
        int numTests = 2;
        TestResult res = new TestResult(Controller);

        for(int i=0; i< gamesToPlay.length; i++){
            int gameIdx = gamesToPlay[i];
            String gameName = games[gameIdx][1];
            String game = games[gameIdx][0];
            GameResult gRes = new GameResult(gameName, gameIdx);
            for(int levelIdx=0; levelIdx < 2; levelIdx++){
                int seed = generator.nextInt();
                String level = game.replace(gameName, gameName + "_lvl" + 0);
                double[] score = ArcadeMachine.runOneGame(game, level, true, Controller, null, seed, 0);
                gRes.AddResult(score, levelIdx);
            }
            res.Results.add(gRes);
        }
        Gson gson = new Gson();
        try(FileWriter writer = new FileWriter("C:\\temp\\file.txt")) {
            String json = gson.toJson(res);
            writer.write(json);
            writer.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
