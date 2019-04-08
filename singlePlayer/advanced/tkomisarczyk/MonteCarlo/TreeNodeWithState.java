package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.StateObservation;
import tracks.singlePlayer.advanced.tkomisarczyk.Utilities;

import java.util.ArrayList;
import java.util.Random;

public class TreeNodeWithState extends TreeNode{
    StateObservation state;
    
    TreeNodeWithState(TreeNodeWithState parent, StateObservation obs){
        super(parent, obs);
        state = obs;
    }

    TreeNodeWithState(int depth, StateObservation obs){
        super(depth);
        state = obs;
    }
    
    @Override
    public void Expand(StateObservation obs) {
        var actions = state.getAvailableActions();
//        Logika wygląda tak:
//            Jeśli children to null, to znaczy że dany węzeł jest rozwijany pierwszy raz (0). Sprawdzamy czy są
//            z niego dostępne jakieś akcje. Jeśli tak (0.1), no to będzie miał listę dzieci, inaczej (0.2) wracamy z wynikiem
//            na górę (gra zakończona).
//            W przeciwnym wypadku są dwie opcje, albo ten węzeł ma jakieś dzieci do rozwinięcia (1.1), i wtedy wybieramy
//            losowo jedno z nich, albo ma rozwinięte już wszystkie (1.2) i wtedy wybieramy korzystając z odpowiednich
//            wzorów dziecko do dalszej pracy.
        if (children == null) {
            if (actions.size() == 0) {//(0.2)
                var result = Utilities.EvaluateState(state);
                UpdateScoreUpwards(result);
            }
            else {//(0.1)
                int choice = generator.nextInt(actions.size());
                var stCopy = state.copy();
                stCopy.advance(actions.get(choice));
                children = new ArrayList<>();
                for(int i=0; i< actions.size(); i++){
                    children.add(null);
                }
                children.set(choice, new TreeNodeWithState(this, stCopy));
                children.get(choice).name = actions.get(choice).name();
                uninitiatedChildren = actions.size() - 1;
            }
        }
        else if (UninitiatedLeft() > 0) {//(1.1)
            int choice = GetNthUninitialized(generator.nextInt(UninitiatedLeft()));
            var stCopy = state.copy();
            stCopy.advance(actions.get(choice));
            children.set(choice, new TreeNodeWithState(this, stCopy));
            children.get(choice).name = actions.get(choice).name();
            uninitiatedChildren--;
        }
        else {//(1.2)
            int choice = ChooseChildToExpandUct(state);
            children.get(choice).Expand(state);
        }
    }
}
