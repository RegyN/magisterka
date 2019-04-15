package tracks.singlePlayer;

import tools.com.google.gson.Gson;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CompareTestResults {
    
    public static boolean checkForCompatibility(ArrayList<TestResult> results){
        if(results.size() <= 1){
            return false;
        }
        TestResult prototype = results.get(0);
        for(int i = 1; i < results.size(); i++){
            TestResult compared = results.get(i);
            if(prototype.numberOfGames() != compared.numberOfGames()){
                System.out.println("Difference found in " + i + " results number of games.");
                System.out.println("Expected " + prototype.numberOfGames() + " found " + compared.numberOfGames());
                return false;
            }
            for(int j = 0; j < prototype.numberOfGames(); j++){
                if(prototype.Results.get(i).GameNumber != compared.Results.get(i).GameNumber){
                    System.out.println("Difference found in " + i + " results gameNumber for game " + j + ".");
                    System.out.println("Expected " + prototype.Results.get(i).GameNumber + " found " + compared.Results.get(i).GameNumber + ".");
                    return false;
                }
                for(int k = 0; k < 4; k++){
                    if(prototype.Results.get(j).numberOfResults(k) != compared.Results.get(j).numberOfResults(k)) {
                        System.out.println("Difference found in " + i + " results playouts number for game " + j + " level " + k + ".");
                        System.out.println("Expected " + prototype.Results.get(j).numberOfResults(k) + " found " + compared.Results.get(j).numberOfResults(k)+ ".");
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static void SortResults(GameAvgResult[] arr){
        for(int i=0; i< arr.length; i++){
            int maxIdx = i;
            for(int j=i; j< arr.length; j++){
                if(arr[j].CompareTo(arr[maxIdx]) >= 0){
                    maxIdx = j;
                }
            }
            GameAvgResult tmp = arr[i];
            arr[i] = arr[maxIdx];
            arr[maxIdx] = tmp;
        }
    }
    
    public static void main(String[] args){
        int[] scoring = new int[] {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
        String[] resultFiles = new String[]{
                ""
        };
        ArrayList<TestResult> results = new ArrayList<>();
        Gson gson = new Gson();
        for(var file : resultFiles) {
            try (FileReader reader = new FileReader(file)) {
                TestResult res = gson.fromJson(reader, TestResult.class);
                results.add(res);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if(!checkForCompatibility(results)){
            System.out.println("Incompatibility found. Stopping the comparison");
            return;
        }
        
        
        int numberOfGames = results.get(0).numberOfGames();
        int numberOfControllers = resultFiles.length;
        int[][] competitionResults = new int[numberOfGames][];
        for(int i = 0; i < numberOfGames; i++){
            competitionResults[i] = new int[numberOfControllers];
        }
        
        for(int i = 0; i< numberOfGames; i++){
            GameAvgResult[] gameResults = new GameAvgResult[numberOfControllers];
            for(int j=0; j<numberOfControllers; j++){
                gameResults[j] = new GameAvgResult(results.get(j).Results.get(i), j);
            }
            SortResults(gameResults);
            for(int place=0; place<gameResults.length; place++){
                competitionResults[i][gameResults[place].ControllerIndex] = scoring[place];
            }
        }
    }
}
