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
        int[] gamesToPlay = {0, 10, 13, 18, 42, 60, 68, 80, 84, 100};
        int numTests = 10;
        int numLevels = 1;

        String spGamesCollection =  "examples/all_games_sp.csv";
        String[][] games = Utils.readGames(spGamesCollection);
        Random generator = new Random();

        TestResult res = new TestResult(Controller);

        for(int i=0; i< gamesToPlay.length; i++){
            int gameIdx = gamesToPlay[i];
            String gameName = games[gameIdx][1];
            String game = games[gameIdx][0];
            GameResult gRes = new GameResult(gameName, gameIdx);
            for(int levelIdx=0; levelIdx < numLevels; levelIdx++) {
                for (int j = 0; j < numTests; j++){
                    int seed = generator.nextInt();
                    String level = game.replace(gameName, gameName + "_lvl" + 0);
                    double[] score = ArcadeMachine.runOneGame(game, level, false, Controller, null, seed, 0);
                    gRes.AddResult(score, levelIdx);
                    System.out.println("Wykonano test " + j + " w grze " + gameIdx + " poziom " + levelIdx + ". Wynik to: " + score[0] + ", " + score[1] + ", " + score[2] + ".");
                }
            }
            res.Results.add(gRes);
        }
        Gson gson = new Gson();
        try(FileWriter writer = new FileWriter("C:\\temp\\file.txt")) {
            String json = gson.toJson(res);
            writer.write(json);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
