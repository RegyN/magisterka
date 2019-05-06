package tracks.singlePlayer;

import tools.Utils;
import tools.com.google.gson.Gson;
import tracks.ArcadeMachine;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class PerformAgentTest {
    private static String FileToExtend;
    private static String FileToSave;
    private static String Controller;
    private static int[] gamesToPlay;
    private static int[] levelsToPlay;
    private static int numTests;
    private static Random generator;
    private static Gson gson;

    private static void setUpParameters(String[] args){
        FileToExtend = "";
        FileToSave = "cleverRollDumbExpand.txt";
        FileToSave = "cleverRollDumbExpand.txt";
        //Controller = "tracks.singlePlayer.past.Return42.Agent";
        Controller = "tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo.Agent";
        gamesToPlay = new int[] {63};//, 18, 42, 60, 68, 80, 84, 100};
        numTests = 10;
        levelsToPlay = new int[] {0};
        generator = new Random();
        gson = new Gson();
    }

    private static String[][] readGames(){
        String spGamesCollection =  "examples/all_games_sp.csv";
        return Utils.readGames(spGamesCollection);
    }

    private static TestResult prepareOldResults(){
        TestResult res = new TestResult(Controller);
        if(FileToExtend != null && !FileToExtend.equals("")){
            try(FileReader reader = new FileReader(FileToExtend)) {
                res = gson.fromJson(reader, TestResult.class);
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        return res;
    }

    private static void saveResults(TestResult res, String name){
        try(FileWriter writer = new FileWriter(name)) {
            String json = gson.toJson(res);
            writer.write(json);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static String getLevelName(String game, String gameName, int lvlNum){
        return game.replace(gameName, gameName + "_lvl" + lvlNum);
    }

    public static void main(String [] args){
        setUpParameters(args);
        String[][] games = readGames();
        var res = prepareOldResults();

        for (int gameIdx : gamesToPlay) {
            String gameName = games[gameIdx][1];
            String game = games[gameIdx][0];
            for (int levelIdx : levelsToPlay) {
                for (int j = 0; j < numTests; j++) {
                    System.out.println();

                    int seed = generator.nextInt();
                    String level = getLevelName(game, gameName, levelIdx);

                    double[] score = ArcadeMachine.runOneGame(game, level, false, Controller, null, seed, 0);
                    res.addResult(new PlayoutResult(score, levelIdx), gameIdx, gameName);

                    System.out.println("Wykonano test " + j + " w grze " + gameIdx + " poziom " + levelIdx + ". Wynik to: " + score[0] + ", " + score[1] + ", " + score[2] + ".");
                }
            }
        }
        saveResults(res, FileToSave);
    }
}
