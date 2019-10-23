package tracks.singlePlayer.past.Normal_MCTS.GameTable;

import java.util.ArrayList;
import java.util.HashMap;

import tracks.singlePlayer.past.Normal_MCTS.Normal_MCTS.Agent;
import core.game.Observation;
import core.game.StateObservation;

public class GameTable {
	private static HashMap<Integer, ArrayList<GameTableRow> > hashTable;
	
	public void settingTable(StateObservation gameState){
		GameObjectDistance game = new GameObjectDistance(gameState);
		
		if( Agent.cnt == 1 && Agent.first){
			newTable(gameState);
			Agent.first = false;
		}
		additionalSpriteAdd(game);
	}
	
	
	public void newTable(StateObservation gameState){
		GameObjectDistance game = new GameObjectDistance(gameState);
		hashTable = new HashMap<Integer, ArrayList<GameTableRow> > ();
		
		setRow(game);
	}
	
	private void setRow(GameObjectDistance game ){
		setRowObject(GameTable_TYPE.NPC, game.existNpc, game.NpcPositions);
		setRowObject(GameTable_TYPE.MOVABLE, game.existMov , game.MovPositions);
		setRowObject(GameTable_TYPE.IMMOVABLE, game.existImmov, game.ImmovPositions);
		setRowObject(GameTable_TYPE.RESOURCE, game.existRes, game.ResPositions);
		setRowObject(GameTable_TYPE.PORTAL, game.existPortal, game.PortalPositions);
		setRowObject(GameTable_TYPE.FROMAVATAR, game.existFAS, game.FASPositions);
	}
	
	private void setRowObject(int category, boolean isExist, ArrayList<Observation>[] ob){
		if( isExist ){		
			//make tmpTalbe
			ArrayList<GameTableRow> tmpTable = new ArrayList<GameTableRow>();
			for(int i = 0; i <ob.length; i++){
				GameTableRow row = new GameTableRow(GameTable_TYPE.AVATAR, category, ob[i].get(0).itype, i , GameTable_TYPE.INIT, GameTable_TYPE.INIT, GameTable_TYPE.INIT);
				tmpTable.add(row);
				
				if(Agent.Attacking){
					row = new GameTableRow(GameTable_TYPE.AVATAR_A, category, ob[i].get(0).itype , i , GameTable_TYPE.INIT, GameTable_TYPE.INIT, GameTable_TYPE.INIT);
					tmpTable.add(row);
					}
			}
			
			//put hashTable
			hashTable.put(category, tmpTable);
		}
	}
	
	
	
	
	public void additionalSpriteAdd(GameObjectDistance game){
		//obNum is 1~ 6
		for(int i= 1; i < 7 ; i++){
			if( isTypeCntChange(game, i) )
				addAddtional(game, i);
		}
	}
	
	private boolean isTypeCntChange(GameObjectDistance game, int category){
		if ( getHashTableOrderSize(category)  == getCurrentTypeCnt( game, category) )
			return false;
		else
			return true;
	}
	
	private int getHashTableOrderSize(int category){
		if(existHashTableCategory(category))
			return hashTable.get(category).size();
		
		return 0;
	}
	
	private boolean existHashTableCategory(int category){
		if(hashTable != null){
			if(hashTable.get(category) != null)
				return true;
		}
		
		return false;
	}
	
	
	private int getCurrentTypeCnt( GameObjectDistance game,int category){
		int tmp=0;
		
		if(category == GameTable_TYPE.NPC){
			
			if(game.existNpc)
				tmp = game.NpcPositions.length;
			else
				tmp = 0;
			
			return tmp;
		}
		
		else if(category == GameTable_TYPE.MOVABLE){
			
			if(game.existMov)
				tmp = game.MovPositions.length;
			else
				tmp = 0;
			
			return tmp;
		}
		
		else if(category == GameTable_TYPE.IMMOVABLE){
			
			if(game.existImmov)
				tmp = game.ImmovPositions.length;
			else
				tmp = 0;
			
			return tmp;
		}
		
		else if(category == GameTable_TYPE.RESOURCE){
			
			if(game.existRes)
				tmp = game.ResPositions.length;
			else
				tmp = 0;
			
			return tmp;
		}
		
		else if(category == GameTable_TYPE.PORTAL){
			
			if(game.existPortal)
				tmp = game.PortalPositions.length;
			else
				tmp = 0;
			
			return tmp;
		}
		
		else if(category == GameTable_TYPE.PORTAL){
			
			if(game.existFAS)
				tmp = game.FASPositions.length;
			else
				tmp = 0;
			
			return tmp;
		}
		
		return tmp;
		
	}
	
	
	
	
	private void addAddtional(GameObjectDistance game, int category){	
		
		if(category == GameTable_TYPE.NPC  && game.existNpc){
			for(int i=0; i< game.NpcPositions.length; i++){
				inputHashTable(category,game.NpcPositions, i);
			}
		}
		
		if(category == GameTable_TYPE.PORTAL  && game.existPortal){
			for(int i=0; i< game.PortalPositions.length; i++){
				inputHashTable(category,game.PortalPositions, i);
			}
		}
		
		if(category == GameTable_TYPE.RESOURCE  && game.existRes){
			for(int i=0; i< game.ResPositions.length; i++){
				inputHashTable(category,game.ResPositions, i);
			}
		}
		
		if(category == GameTable_TYPE.MOVABLE  && game.existMov){
			for(int i=0; i< game.MovPositions.length; i++){
				inputHashTable(category,game.MovPositions, i);
			}
		}
		
		if(category == GameTable_TYPE.IMMOVABLE  && game.existImmov){
			for(int i=0; i< game.ImmovPositions.length; i++){
				inputHashTable(category, game.ImmovPositions, i);
			}
		}
		
		if(category == GameTable_TYPE.FROMAVATAR  && game.existFAS){
			for(int i=0; i< game.FASPositions.length; i++){
				inputHashTable(category,game.FASPositions, i);
			}
		}
		
	}
	
	
	private void inputHashTable(int category,ArrayList<Observation>[]  ob,int i){
		if(hashTable.get(category) !=null){
			if( i >    hashTable.get(category).size() -1   ){
				if( existHashTableCategory(category) ){
					GameTableRow row = new GameTableRow(GameTable_TYPE.AVATAR, category ,ob[i].get(0).itype, i , GameTable_TYPE.INIT, GameTable_TYPE.INIT, GameTable_TYPE.INIT );
					hashTable.get(category).add(row);		
					if(Agent.Attacking){
						row = new GameTableRow(GameTable_TYPE.AVATAR_A, category,ob[i].get(0).itype, i , GameTable_TYPE.INIT, GameTable_TYPE.INIT, GameTable_TYPE.INIT);
						hashTable.get(category).add(row);
					}
				}
			}
			
		}
		else{
			ArrayList<GameTableRow> tmpTable = new ArrayList<GameTableRow>();
			GameTableRow row = new GameTableRow(GameTable_TYPE.AVATAR, category ,ob[i].get(0).itype, i, GameTable_TYPE.INIT, GameTable_TYPE.INIT, GameTable_TYPE.INIT );
			tmpTable.add(row);
			
			if(Agent.Attacking){
				row = new GameTableRow(GameTable_TYPE.AVATAR_A, category,ob[i].get(0).itype, i , GameTable_TYPE.INIT, GameTable_TYPE.INIT, GameTable_TYPE.INIT);
				tmpTable.add(row);
			}
			tmpTable.add(row);
			hashTable.put(category,tmpTable);
		}
	}
	
	
	public void findSet(int ava, int category , int order, int result){
		int tmp = -1;
		if(existHashTableCategory(category))
			tmp = findCategoryTable(ava, category, order);
		if(tmp > -1)
			hashTable.get(category).get(tmp).setResult(result);
	}
	
	public int findGet(int ava, int category , int order){
		int tmp = -1;
		if(existHashTableCategory(category))
			tmp = findCategoryTable(ava, category, order);
		if(tmp > -1)
			return hashTable.get(category).get(tmp).getResult();
		
		return 0;
	}
	
	
	private int findCategoryTable(int ava,int category, int order){
		ArrayList<GameTableRow> tmpTable = hashTable.get(category);
		for (int i =0; i< tmpTable.size(); i++){
			 if( order == tmpTable.get(i).getSpriteOrder()){
				 if(ava == tmpTable.get(i).getAva())
				return i;
			}
		}
		
		return -1;
	}
	
	
	
	
	
	public void Print(){
		ArrayList<GameTableRow> obTable;
		for(int j= 1; j<7; j ++){
			obTable = hashTable.get(j);
			System.out.println();
			System.out.println(" category is " + j);
			if(obTable != null  && obTable.size() >0){
			for(int i=0; i< obTable.size(); i ++){
				System.out.print("Ava : " );
				if( obTable.get(i).getAva() == 1)
					System.out.print("Ava" );
				else if(obTable.get(i).getAva() == 2)
					System.out.print("Ava_A" );
				System.out.print("    Other SptriteType : ");
				if( obTable.get(i).getSpriteCategory() == GameTable_TYPE.NPC)
					System.out.print("NPC" );
				else if(obTable.get(i).getSpriteCategory() == GameTable_TYPE.MOVABLE)
					System.out.print("MOV" );
				else if(obTable.get(i).getSpriteCategory() == GameTable_TYPE.IMMOVABLE)
					System.out.print("Imm" );
				else if(obTable.get(i).getSpriteCategory() == GameTable_TYPE.PORTAL)
					System.out.print("Por" );
				else if(obTable.get(i).getSpriteCategory() == GameTable_TYPE.RESOURCE)
					System.out.print("Res" );
				else if(obTable.get(i).getSpriteCategory() == GameTable_TYPE.FROMAVATAR)
					System.out.print("FAS" );
				
				
				System.out.print(" ["  +obTable.get(i).getSpriteOrder() + "]");
				System.out.print("    AvatarItype : " + obTable.get(i).getSpriteItype());
				System.out.println("    AvatarOutput : "+obTable.get(i).getAvatarOutput() +  "    OtherOutput : " + obTable.get(i).getSpriteOutput() +
						"    Result : "+ obTable.get(i).getResult());
				}
			
			}
		}
		
	}
	
	
	public int findCategoryAndItype(int category,int Itype){
		
		ArrayList<GameTableRow> tmpTable = hashTable.get(category);
		if(tmpTable != null){
		for(int i = 0 ; i< tmpTable.size(); i ++){
			if(tmpTable.get(i).getSpriteItype() == Itype)
				return tmpTable.get(i).getResult();
		}
		}
		
		return GameTable_TYPE.POSITIVE;
	}
	
}
