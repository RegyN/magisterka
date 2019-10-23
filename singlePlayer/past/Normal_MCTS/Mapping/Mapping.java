package tracks.singlePlayer.past.Normal_MCTS.Mapping;

import java.util.ArrayList;





import tracks.singlePlayer.past.Normal_MCTS.GameTable.GameTable;
import tracks.singlePlayer.past.Normal_MCTS.GameTable.GameTable_TYPE;
import tracks.singlePlayer.past.Normal_MCTS.Mapping.PathFinder.PathFinder;
import core.game.Observation;
import core.game.StateObservation;

public class Mapping {
	//private StateObservation curState;
	private PathFinder pathFind = new PathFinder();
	private Mob[][] mtmp = null;
	
	private GameTable gt = new GameTable();
	
	public Mapping(int sizeX, int sizeY){
		InitPathFind(sizeX, sizeY);
	}
	
	//category  0 -> Avatar itype =1
	//          1 -> Resource 
	//          2  ->  Portal In
	//			3 -> NPC 
	//			4 -> Immovable
	//          5  -> 
	//          6  -> movable
	
	public void setMap( StateObservation curState){
		ArrayList<Observation>[][]  tmp =curState.getObservationGrid();
		mtmp = new Mob[tmp[0].length][tmp.length];
		
		for(int i=0; i < tmp.length; i++){
			for(int j=0; j < tmp[i].length; j++){
				
				if(tmp[i][j].size()>0){
					int tmp2 = gt.findCategoryAndItype( tmp[i][j].get(0).category ,tmp[i][j].get(0).itype );
					
					if(tmp[i][j].get(0).category == 4 && tmp[i][j].get(0).itype == 0){
						mtmp[j][i] = new Mob(tmp[i][j].size(), tmp[i][j].get(0).category, tmp[i][j].get(0).itype, true);}
					
					else if(tmp[i][j].get(0).category == 6){
						mtmp[j][i] = new Mob(tmp[i][j].size(), tmp[i][j].get(0).category, tmp[i][j].get(0).itype, true);}
					
					else if(tmp2 == GameTable_TYPE.NOTHING || tmp2== GameTable_TYPE.NEGATIVE)
						mtmp[j][i] = new Mob(tmp[i][j].size(), tmp[i][j].get(0).category, tmp[i][j].get(0).itype,  true);
					else
						mtmp[j][i] = new Mob(tmp[i][j].size(), tmp[i][j].get(0).category, tmp[i][j].get(0).itype,  false);
					}
				
				else
					mtmp[j][i] = new Mob(-1,0,0,false);
				}
		}
		setPathFindMap();
		
	}
	
	public void Print(){
		System.out.println("------------------------------------");
		if(mtmp != null){
		for(int i=0; i < mtmp.length; i++){
			for(int j=0; j < mtmp[i].length; j++){
					//System.out.print(mtmp[i][j].getCategory() +""+ mtmp[i][j].getType() + "[" + mtmp[i][j].getNumber() + "] ");
				if(mtmp[i][j].isObstcale())
					System.out.print("1 ");
				else
					System.out.print("0 ");
			}
			System.out.println();
		}
		System.out.println();
		}
		else
			System.out.println("mtmp is null ");
	}
	
	public void InitPathFind(int sizeX, int sizeY){
		pathFind.init(sizeX, sizeY);
	}
	
	private void setPathFindMap(){
		pathFind.setiMap(mtmp);
	}
	
	public int findPath(int goalX,int goalY){
		return pathFind.findDistance(goalX, goalY);
	}

}
