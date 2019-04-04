package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.StateObservation;
import tracks.singlePlayer.advanced.tkomisarczyk.Utilities;

import java.util.ArrayList;

public class TreeNodeWithState extends TreeNode{
    StateObservation state;
    
    TreeNodeWithState(TreeNodeWithState parent, StateObservation obs){
        super(parent, obs);
        state = obs;
    }
    
    @Override
    public void Expand(StateObservation obs) {
        var stCopy = obs.copy();
        var actions = stCopy.getAvailableActions();
    
        if (children == null) {             // Pierwszy raz rozwijamy to dziecko
            if (actions.size() == 0) {      // Z tego dziecka nie da się wykonać akcji (gra się zakończyła)
                var result = Utilities.EvaluateState(stCopy);
                UpdateScoreUpwards(result);
            }
            else {
                stCopy.advance(actions.get(0));
                children = new ArrayList<>();
                children.add(new TreeNodeWithState(this, stCopy));
                children.get(0).name = actions.get(0).name();
            }
        }
        else if (children.size() < actions.size()) {    // To dziecko jest rozwijane poraz kolejny
            int newIndex = children.size();
            stCopy.advance(actions.get(children.size() - 1));
            children.add(new TreeNodeWithState(this, stCopy));
            children.get(newIndex).name = actions.get(newIndex).name();
        }
        else {
            int choice = ChooseChildToExpandUct(stCopy);
            stCopy.advance(actions.get(choice));
            children.get(choice).Expand(state);
        }
    }
}
