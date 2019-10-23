package tracks.singlePlayer.past.Normal_MCTS.Normal_MCTS;

import java.util.ArrayList;

import tracks.singlePlayer.past.Normal_MCTS.GameTable.*;
import tracks.singlePlayer.past.Normal_MCTS.Mapping.Mapping;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class ModelizseGame {
	
	private static GameTable  gt = new GameTable();
	private static Mapping map;
	private static int gridSizeX;
	private static int gridSizeY;
	private static int gridBlockSize;
	
	public void modelizeState(StateObservation curGameState, int action){
		StateObservation  nextGameState = curGameState.copy();
		nextGameState.advance(Agent.actions[action]);
		
		boolean gameOver = nextGameState.isGameOver();
		Types.WINNER win = nextGameState.getGameWinner();

		GameObjectDistance curGame = new GameObjectDistance(curGameState);
		
		if( Agent.cnt == 1 && Agent.first){
			setGridAndMap(curGameState);
		}
		
		gt.settingTable(curGameState);
		
		if(Agent.first){
			System.out.println("new mapping is Excute");
			setGridAndMap(curGameState);
			Agent.first=false;
		}
		
		map.setMap(curGameState);
		
		//gt.Print();
		//map.Print();
		if(gameOver && win == Types.WINNER.PLAYER_LOSES){
			findLoseCondition(curGameState,nextGameState,action);
		}
		
		else if( nextGameState.getGameScore() > curGameState.getGameScore() ){
				//if( ! isUpScoreConditionModelizeFinish() )
			findUpScore(curGameState, nextGameState, action);
		}
		
		
	}
	
	private void setGridAndMap(StateObservation curGameState){
		ArrayList<Observation>[][]  tmp =curGameState.getObservationGrid();
		gridSizeY = tmp.length;
		gridSizeX = tmp[0].length;
		gridBlockSize = curGameState.getBlockSize();
		map = new Mapping(gridSizeX, gridSizeY);
	}

	private int suitableGridX (Vector2d position){
		int x = (int) position.x / gridBlockSize;
        boolean validX = x >= 0 && x < gridSizeX;
        boolean xPlus = (position.x % gridBlockSize) > 0 && (x+1 <  gridSizeX);
       

        if(validX)
        {
            return x;
        }
		
		return -1;	
	}
	
	private int suitableGridY(Vector2d position){
		 int y = (int) position.y / gridBlockSize;
	     boolean validY = y >= 0 && y < gridSizeY;
	     boolean yPlus = (position.y % gridBlockSize) > 0 && (y+1 < gridSizeY);
	     if(validY){
	    	 return y;
	     }
			
			return -1;	
		
	}

	
	private void findLoseCondition(StateObservation curGameState, StateObservation nextGameState, int action){
		Vector2d curAvatarPos = curGameState.getAvatarPosition();
		
		if(action == Agent.Right ){
			curAvatarPos.add(gridSizeX,0);
		}
		
		if(action == Agent.Left ){
			curAvatarPos.add( -gridSizeX,0);
		}
		
		if(action == Agent.Up ){
			curAvatarPos.add(0,-gridSizeY);
		}
		
		if(action == Agent.Down ){
			curAvatarPos.add(0,gridSizeY);
			
		}
		
		if(action == Agent.Attack ){
			curAvatarPos.add(0,0);
		}
		
		ArrayList<Observation>[] npc = nextGameState.getNPCPositions( curAvatarPos );
		setGameTableResult(npc,curAvatarPos,GameTable_TYPE.NPC, GameTable_TYPE.NEGATIVE);
		
		ArrayList<Observation>[] mov = nextGameState.getNPCPositions( curAvatarPos );
		setGameTableResult(mov,curAvatarPos,GameTable_TYPE.MOVABLE, GameTable_TYPE.NEGATIVE);
		
	}
	
	private void setGameTableResult(ArrayList<Observation>[] object ,Vector2d AvatarPos, int objectOb, int result){
		if(object != null){
			for(int i=0; i< object.length; i++){
				if(object[i].size() > 0){
					if( AvatarPos.dist( object[i].get(0).position ) < 10){
						gt.findSet(GameTable_TYPE.AVATAR, objectOb, i, result);
						
					}
					
				}
			}
		}
	}
	
	private void setGameTablePosResult(ArrayList<Observation>[] object ,Vector2d AvatarPos,int order,int objectOb, int result){
		if(object != null){
			if(object[order].size() > 0){
				if( AvatarPos.dist( object[order].get(0).position ) < gridSizeX){
					gt.findSet(GameTable_TYPE.AVATAR, objectOb, order, result);
				}
				
			}
		}
	}
	
	public void findUpScore(StateObservation curGameState, StateObservation nextGameState, int action){
		
		GameObjectDistance curGame = new GameObjectDistance(curGameState);
		GameObjectDistance nextGame = new GameObjectDistance(nextGameState);
		
		int tmp = 0;
		
		tmp = cntChange(curGame.NpcPositions,nextGame.NpcPositions);
		if(  tmp > -1){
			Vector2d nextAvatarPos = nextGameState.getAvatarPosition();
			ArrayList<Observation>[] npc = curGameState.getNPCPositions( nextAvatarPos );
			
			setGameTablePosResult(npc, nextAvatarPos,tmp, GameTable_TYPE.NPC, GameTable_TYPE.POSITIVE);
		}
		
		tmp = cntChange(curGame.MovPositions,nextGame.MovPositions);
		if(tmp > -1){
			Vector2d nextAvatarPos = nextGameState.getAvatarPosition();
			ArrayList<Observation>[] mov = curGameState.getMovablePositions( nextAvatarPos );
			
			setGameTablePosResult(mov, nextAvatarPos,tmp,GameTable_TYPE.MOVABLE, GameTable_TYPE.POSITIVE);
		}
		
		tmp = cntChange(curGame.ResPositions, nextGame.ResPositions);
		if( tmp >-1){
			Vector2d nextAvatarPos = nextGameState.getAvatarPosition();
			ArrayList<Observation>[] res = curGameState.getResourcesPositions( nextAvatarPos );
			setGameTablePosResult(res, nextAvatarPos, tmp, GameTable_TYPE.RESOURCE, GameTable_TYPE.POSITIVE);
		}
		
	}
	
	private int cntChange(ArrayList<Observation>[] curObject,ArrayList<Observation>[] nextObject){
		if(curObject != null){
			for(int i =0; i< curObject.length; i++){
				if(curObject[i].size() != nextObject[i].size())
					return i;
			}
				
		}
		
		return -1;
	}
	
	
	public double value(StateObservation gameState){
		boolean gameOver = gameState.isGameOver();
        Types.WINNER win = gameState.getGameWinner();
        double rawScore = gameState.getGameScore();
        Vector2d AvatarPosition = gameState.getAvatarPosition();
        ArrayList<Types.ACTIONS> action = gameState.getAvailableActions();
        
        GameObjectDistance gameOb = new GameObjectDistance(gameState);
        
        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return -10000000.0; // 

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return 10000000.0; // 
		
        int tmpDis= 0;
        
        
        
        tmpDis += getDistance(gameOb.existNpc,  gameOb.NpcPositions, GameTable_TYPE.NPC);
        tmpDis += getDistance(gameOb.existRes,  gameOb.ResPositions, GameTable_TYPE.RESOURCE);
        tmpDis += getDistance(gameOb.existMov,  gameOb.MovPositions, GameTable_TYPE.MOVABLE);

        //System.out.println("tmpDist : " + tmpDis  + "  rawScore" + rawScore);
        
        
        rawScore  *= 10;
        if(tmpDis !=0)
       	 rawScore = rawScore + (rawScore+1)/tmpDis;
        //rawScore = tmpDist + rawScore;
       // System.out.println("tmpDist : " + tmpDis  + "  rawScore" + rawScore);
		 return rawScore;
		
	}
	
	private int getDistance(boolean exist,ArrayList<Observation>[] position, int ob ){
		int tmpDis =0;
		if(exist){
        	for(int i = 0; i < position.length; i ++){
        		for(int j =0;   j < getLoopSize( position[i]) ; j++){
        			int tmpx= suitableGridX(position[i].get(j).position );
        			int tmpy= suitableGridY( position[i].get(j).position );
        			int  result = gt.findGet(1, ob, j);
        			if(result == GameTable_TYPE.POSITIVE || result == GameTable_TYPE.NEGATIVE )
        				tmpDis += result * map.findPath(tmpx,tmpy );
        		}
        	}
        	
        }
		
		return tmpDis;
			
	}
	
	private int getLoopSize(ArrayList<Observation> curObserve ){
    	if ( curObserve.size() > 3 )
    		return 4;
    	else
    		return curObserve.size();
    	
    }
	
}


