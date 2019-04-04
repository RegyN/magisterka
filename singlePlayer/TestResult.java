package tracks.singlePlayer;

import java.util.ArrayList;

public class TestResult {
    String ControllerName;
    ArrayList<GameResult> Results;

    TestResult(String controllerName){
        ControllerName = controllerName;
        Results = new ArrayList<>();
    }
}
