package tracks.singlePlayer.past.Normal_MCTS.GameTable;

import java.util.ArrayList;

import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class GameObjectDistance {
	
	private Vector2d AvatarPos;
	public ArrayList<Observation>[] ImmovPositions;
	public ArrayList<Observation>[] MovPositions;
	public ArrayList<Observation>[] PortalPositions;
	public ArrayList<Observation>[] NpcPositions ;
	public ArrayList<Observation>[] ResPositions;
	public ArrayList<Observation>[] FASPositions ;
	
	public boolean existImmov;
	public boolean existMov;
	public boolean existPortal;
	public boolean existNpc;
	public boolean existRes;
	public boolean existFAS;
	
	
	
	public GameObjectDistance(StateObservation curGameState) {
		AvatarPos = curGameState.getAvatarPosition();
		ImmovPositions = curGameState.getImmovablePositions(AvatarPos);
		MovPositions = curGameState.getMovablePositions(AvatarPos);
		PortalPositions = curGameState.getPortalsPositions(AvatarPos);
		NpcPositions = curGameState.getNPCPositions(AvatarPos);
		ResPositions = curGameState.getResourcesPositions(AvatarPos);
		FASPositions = curGameState.getFromAvatarSpritesPositions(AvatarPos);
		
		existImmov = isExist(ImmovPositions);
		existMov = isExist(MovPositions);
		existPortal= isExist(PortalPositions);
		existNpc = isExist(NpcPositions);
		existRes = isExist(ResPositions);
		existFAS = isExist(FASPositions);
	}
	
	private boolean isExist(ArrayList<Observation>[] tmp){
		if (tmp != null)
			return true;
		else
			return false;
		
	}

}

