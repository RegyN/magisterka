package tracks.singlePlayer.past.Return42;

import java.awt.Graphics2D;

import ontology.Types;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.past.Return42.algorithms.AlgorithmPicker;
import tracks.singlePlayer.past.Return42.knowledgebase.KnowledgeBase;
import tracks.singlePlayer.past.Return42.util.CachingStateObservation;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer {

    private final KnowledgeBase knowledge;
    private AbstractPlayer current;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        this.knowledge = new KnowledgeBase();
        AlgorithmPicker picker = new AlgorithmPicker();

        StateObservation state = decrorateState(stateObs);
        knowledge.initForGame( state );
        
        current = picker.pickAlgorithm( knowledge, state, elapsedTimer );
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	StateObservation state = decrorateState(stateObs);
        return current.act(state, elapsedTimer);
    }
    
    private StateObservation decrorateState( StateObservation state ) {
    	return new CachingStateObservation( state );
    }
    
    @Override
    public void draw(Graphics2D g) {
    	current.draw(g);
    }
}