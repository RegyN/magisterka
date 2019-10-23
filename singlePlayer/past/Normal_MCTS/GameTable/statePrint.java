package tracks.singlePlayer.past.Normal_MCTS.GameTable;

import core.game.StateObservation;

	public class statePrint {
		GameObjectDistance ob;
		
		public statePrint(StateObservation gameState){
			ob = new GameObjectDistance(gameState);
		}
		
		public void Print(){
			System.out.println("TYPE");
			if(ob.NpcPositions != null)
				System.out.print("    NPC : " + ob.NpcPositions.length);
			if(ob.MovPositions != null)
				System.out.print("    Mov : " + ob.MovPositions.length);
			if(ob.ImmovPositions != null)
				System.out.print("    Immov : " + ob.ImmovPositions.length);
			
			System.out.println();
			
			if(ob.ResPositions != null)
				System.out.print("    Resource : " + ob.ResPositions.length);
			if(ob.PortalPositions != null)
				System.out.print("    Portal : " + ob.PortalPositions.length);
			if(ob.FASPositions != null)
				System.out.print("    FAS : " + ob.FASPositions.length);
			
			System.out.println();
			System.out.println();
			System.out.println("Size");
			
			if(ob.NpcPositions != null){
			for(int i =0; i <ob.NpcPositions.length; i++){
				System.out.print("    NPC["+ i +"] : " + ob.NpcPositions[i].size() );
				
			}}
			
			System.out.println();
			if(ob.MovPositions != null){
			for(int i =0; i <ob.MovPositions.length; i++){
				System.out.print("    Mov["+ i +"] : " + ob.MovPositions[i].size() );
				
			}}
					
			System.out.println();
			if(ob.ImmovPositions!= null){
			for(int i =0; i <ob.ImmovPositions.length; i++){
				System.out.print("    Immov["+ i +"] : " + ob.ImmovPositions[i].size() );
				
			}}
			
			System.out.println();
			if(ob.ResPositions != null){
			for(int i =0; i <ob.ResPositions.length; i++){
				System.out.print("    Res["+ i +"] : " + ob.ResPositions[i].size() );
				
			}}
			
			System.out.println();
			if(ob.PortalPositions != null){
			for(int i =0; i <ob.PortalPositions.length; i++){
				System.out.print("    Portal["+ i +"] : " + ob.PortalPositions[i].size() );
				
			}}
			
			System.out.println();
			if(ob.FASPositions != null){
			for(int i =0; i <ob.FASPositions.length; i++){
				System.out.print("    FAS["+ i +"] : " + ob.FASPositions[i].size() );
				
			}}
			System.out.println();
			System.out.println();
			
		}

}
