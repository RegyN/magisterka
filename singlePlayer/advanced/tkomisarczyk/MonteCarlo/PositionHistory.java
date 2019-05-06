package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import java.util.ArrayDeque;
import java.util.Queue;

public class PositionHistory {
    int length;
    Queue<Position2D> Positions;
    private static PositionHistory instance = null;

    private PositionHistory(){
        length = 10;
        Positions = new ArrayDeque<>();
    }

    public static PositionHistory GetInstance(){
        if(instance == null){
            instance = new PositionHistory();
        }
        return instance;
    }

    public static PositionHistory GetNew(){
        instance = new PositionHistory();
        return instance;
    }

    public static void Reset(){
        instance = new PositionHistory();
    }

    public boolean Contains(Position2D position){
        for(var p : Positions){
            if(p.Equals(position)){
                return true;
            }
        }
        return false;
    }

    public void Add(Position2D position){
        Positions.add(position);
        if(Positions.size() > length){
            Positions.remove();
        }
    }
}
