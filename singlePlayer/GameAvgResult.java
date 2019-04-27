package tracks.singlePlayer;

public class GameAvgResult{
    int Wins;
    int Points;
    long Timesteps;
    int ControllerIndex;
    
    GameAvgResult(GameResult results, int controller){
        Wins = 0;
        Points = 0;
        Timesteps = 0;
        ControllerIndex = controller;
        for(var res : results.Results){
            if(res.Status>0.5){
                Wins++;
            }
            Points += res.Score;
            Timesteps += res.Timesteps;
        }
    }
    
    int CompareTo(GameAvgResult compared){
        if(this.Wins > compared.Wins){
            return 3;
        }
        else if(this.Wins < compared.Wins){
            return -3;
        }
        if(this.Points > compared.Points){
            return 2;
        }
        else if(this.Points < compared.Points){
            return -2;
        }
        if(this.Timesteps < compared.Timesteps){
            return 1;
        }
        else if(this.Timesteps > compared.Timesteps){
            return -1;
        }
        return 0;
    }
}
