package tracks.singlePlayer;

public class PlayoutResult {
    double Status;
    double Score;
    double Timesteps;
    int GameLevel;
    public PlayoutResult(double[] input, int lvl){
        if(input.length < 3){
            System.out.println("Playout result has less than 3 values. That's bad. Values padded with 0s.");
        }
        else if(input.length > 3){
            System.out.println("Playout result has more than 3 values. Strange, but OK.");
        }
        GameLevel = lvl;
        Status = input.length > 0 ? input[0] : 0;
        Score = input.length > 1 ? input[1] : 0;
        Timesteps = input.length > 2 ? input[1] : 0;
    }
}
