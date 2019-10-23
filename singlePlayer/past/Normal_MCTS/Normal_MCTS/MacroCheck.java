package tracks.singlePlayer.past.Normal_MCTS.Normal_MCTS;

import java.util.ArrayList;

import ontology.Types;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class MacroCheck {
    
    
    public static int npcScore=0;
    public static int resScore=0;
    public static int movScore=0;
    public static int immovScore=0;
    public static int portalScore=0;
    public static int fasScore=0;
    
    public static int npcOper = 0;
    public static int resOper = 0;
    public static int movOper = 0;
    public static int immovOper=0;
    public static int portalOper=0;
    public static int fasOper=0;
    
    public void updateState(){
    	
        if( npcScore > 0)
        	npcOper ++;
        else if(npcScore < 0 )
        	npcOper--;
        
        if( resScore  >0)
        	resOper ++;
        else if( resScore  < 0)
        	resOper --;
        
        if( movScore  >0)
        	movOper ++;
        else if( movScore  < 0)
        	movOper --;
        
        if( immovScore  >0)
        	immovOper ++;
        else if( immovScore  < 0)
        	immovOper --;
        
        if( portalScore  >0)
        	portalOper ++;
        else if( portalScore  < 0)
        	portalOper --;
        
        if( fasScore  >0)
        	fasOper ++;
        else if( fasScore  < 0)
        	fasOper --;

    }
    
    public void resetAll(){
        npcScore=0;
        resScore=0;
        movScore=0;
        immovScore=0;
        portalScore=0;
        fasScore=0;
        
        npcOper = 0;
        resOper = 0;
        movOper = 0;
        immovOper = 0;
        portalOper = 0;
        fasOper = 0;
    }
    /* ArrayList<Observation>[] obs = this.state.getNPCPositions(this.state.getAvatarPosition());
     * obs[0].size()+"( " + this.state.getNPCPositions(this.state.getAvatarPosition())[0].get(0).position.x
     */
    public void calculScore(StateObservation curState,int nextAct){
        StateObservation nextState = curState.copy();
        nextState.advance(Agent.actions[nextAct]);
        
        double lastScore = curState.getGameScore();
        double curScore =nextState.getGameScore();
        
        if(curScore>lastScore){
//            System.out.println("Score is increased!!");
            Vector2d AvatarPosition = curState.getAvatarPosition();
            Vector2d nAvatarPosition = nextState.getAvatarPosition();
            // distance is decreased = - / distance is increased = +
            double resDist=0;
            double npcDist=0;
            double movDist=0;
            double immovDist=0;
            double portalDist=0;
            double fasDist=0;
            
            
            ArrayList<Observation>[] curRes = curState.getResourcesPositions(AvatarPosition);
            ArrayList<Observation>[] nextRes = nextState.getResourcesPositions(nAvatarPosition);
            
            if( isExist(curRes) && isExist(nextRes) )
            	resDist = subNextStateCurState(nextRes,nAvatarPosition, curRes, AvatarPosition);
            
            ArrayList<Observation>[] curNpc = curState.getNPCPositions(AvatarPosition);
            ArrayList<Observation>[] nextNpc = nextState.getNPCPositions(nAvatarPosition);
            if( isExist(curNpc) && isExist(nextNpc) )
            	npcDist = subNextStateCurState(nextNpc,nAvatarPosition, curNpc, AvatarPosition);
            
            ArrayList<Observation>[] curMov = curState.getMovablePositions(AvatarPosition);
            ArrayList<Observation>[] nextMov = nextState.getMovablePositions(nAvatarPosition);
            if( isExist(curMov) && isExist(nextMov) )
            	movDist = subNextStateCurState(nextMov,nAvatarPosition, curMov, AvatarPosition);
            
            ArrayList<Observation>[] curImmov = curState.getImmovablePositions(AvatarPosition);
            ArrayList<Observation>[] nextImmov = nextState.getImmovablePositions(nAvatarPosition);
            if( isExist(curImmov) && isExist(nextImmov) )
            	immovDist = subNextStateCurState(nextImmov,nAvatarPosition, curImmov, AvatarPosition);
            
            ArrayList<Observation>[] curPortal = curState.getPortalsPositions(AvatarPosition);
            ArrayList<Observation>[] nextPortal = nextState.getPortalsPositions(nAvatarPosition);
            if( isExist(curPortal) && isExist(nextPortal) )
            	portalDist = subNextStateCurState(nextPortal,nAvatarPosition, curPortal, AvatarPosition);
            
            ArrayList<Observation>[] curFAS = curState.getFromAvatarSpritesPositions(AvatarPosition);
            ArrayList<Observation>[] nextFAS = nextState.getFromAvatarSpritesPositions(nAvatarPosition);
            if( isExist(curFAS) && isExist(nextFAS) )
            	fasDist = subNextStateCurState(nextFAS,nAvatarPosition, curFAS, AvatarPosition);
            
            
            if(resDist>0) resScore++;
            else if(resDist<0) resScore--;
            
            if(npcDist>0) npcScore++;
            else if(npcDist<0) npcScore--;
            
            if(movDist>0) movScore++;
            else if(movDist<0) movScore--;
            
            if(immovDist>0) immovScore++;
            else if(immovDist<0) immovScore--;
            
            if(portalDist>0) portalScore++;
            else if(portalDist<0) portalScore--;
            
            if(fasDist>0) fasScore++;
            else if(fasDist<0) fasScore--;
        
        }
    }
    
    
    
    
    private boolean isExist(ArrayList<Observation>[] checkObserve){
    	
    	if(checkObserve  != null)
    		return true;   
    	
    	else
    		return  false;
    }
    
    
    
    private double subNextStateCurState( ArrayList<Observation>[] nextObserve , Vector2d navaPostion, 
    									ArrayList<Observation>[] curObserve , Vector2d avaPostion){
    	return getObjectAvatarDistance(nextObserve,navaPostion) -  getObjectAvatarDistance(curObserve,avaPostion);
    	
    }
    
    
    
    private double getObjectAvatarDistance(ArrayList<Observation>[] curObserve , Vector2d avaPostion){
    	double tmpdist = 0 ;
    	
    	for(int i=0; i<curObserve.length; i++){
                for(int j=0; j < getLoopSize(curObserve[i]); j++ )
                    tmpdist += (avaPostion.dist(curObserve[i].get(j).position) ) ;
        }
    	
    	return tmpdist;
    }
    
    
    
    
    private int getLoopSize(ArrayList<Observation> curObserve ){
    	if ( curObserve.size() > 3 )
    		return 4;
    	else
    		return curObserve.size();
    	
    }
}