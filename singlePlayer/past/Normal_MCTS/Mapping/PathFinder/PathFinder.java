package tracks.singlePlayer.past.Normal_MCTS.Mapping.PathFinder;



import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.lang.*;

import tracks.singlePlayer.past.Normal_MCTS.Mapping.Mob;


public class PathFinder{
	AStar	pfAStar;
	
	Node	nodePath;
	int		iPath[][];
	int		iCurrentPath;
	int		iMaxPath;
	private int mapSizeX=0;
	private int mapSizeY=0;
	
	private static final int STREET=0;
	private static final int OBSTACLE=1;
	
	


	int	iMap[] ;
	int		iMonsterX, iMonsterY;
	int		iMonsterMove;
	
	
	
	public void init(int sizeX,int sizeY)
	{

		iMap = new int[sizeX*sizeY];
		pfAStar = new AStar(iMap, sizeX, sizeY);
		
		
		iMonsterX=0; iMonsterY = 0;
		iMonsterMove = 0;
		iPath = new int[100][2];
		iCurrentPath = -1;
		
		mapSizeX= sizeX;
		mapSizeY =sizeY;
		
		
	}
	 
	public void setiMap(Mob[][] mob){
		for(int i =0; i <mob.length; i++){
			for(int j=0; j<mob[i].length; j++){
				if(mob[i][j].getNumber() ==-1)
					iMap[i*mapSizeX + j] = STREET;
				if(mob[i][j].getNumber() > 0){
					if(mob[i][j].getCategory() == 0){
						iMonsterX=j ; iMonsterY = i;
						//System.out.println("Avatar pos : " +j + "   " + i);
						}
					else if( mob[i][j].isObstcale() )
						iMap[i*mapSizeX + j] = OBSTACLE;
					else 
						iMap[i*mapSizeX + j] = STREET;
				}
			}
		}
		
		//System.out.println();
		//System.out.println("-------------------------");
		//System.out.println("mapsizeX : " +mapSizeX + "  mapSizeY : "+ mapSizeY );
		//Print();
		
	} 

	public void Print(){
		for( int i =0; i <mapSizeY; i++){
			for(int j=0; j < mapSizeX; j++)
				System.out.print( iMap[i*mapSizeX + j]+ " ");
			System.out.println();
		}
	}


	/*ublic void paint(Graphics g)
	{
		Color	c;
		Node	nodeTemp;
		
		
		
		
				
		if( iCurrentPath != -1 )
		{
			c = new Color(130, 130, 130);
			nodeTemp = pfAStar.OpenNode;
			while( nodeTemp != null )
			{
				nodeTemp = nodeTemp.next;
			}

			c = new Color(0, 0, 255);
			nodeTemp = pfAStar.ClosedNode;
			while( nodeTemp != null )
			{
				nodeTemp = nodeTemp.next;
			}
		}

	}*/





	public int findDistance(int mobX, int mobY)
	{
		int	iX, iY;
		
		iX=mobX;
		iY=mobY;
		
		//System.out.println("find Distance Npc : " + iX + "  " + iY );
		//System.out.println("find Distance Ava : " + iMonsterX + "  " + iMonsterY );
		
		if( iX >= 0 && iX < mapSizeX && iY >=0 && iY <mapSizeY && iCurrentPath == -1 &&
			!(iX == iMonsterX && iY == iMonsterY ) && iMap[iY*mapSizeX + iX] == 0 )
		{
			nodePath = pfAStar.FindPath(iMonsterX, iMonsterY, iX, iY);
			
			iCurrentPath = 0;
			while( nodePath != null )
			{
				iPath[iCurrentPath][0] = nodePath.x;
				iPath[iCurrentPath][1] = nodePath.y;
				iCurrentPath++;
				nodePath = nodePath.prev;
			}
			iMaxPath = iCurrentPath;
		}
		
		pfAStar.ResetPath();
		iCurrentPath = -1;
		
		return iMaxPath;
	}

	
	/*class MonsterMoveTimerTask extends TimerTask
	{
		public void run()
		{

			if( iCurrentPath != -1 )
			{
				iMonsterX = iPath[iCurrentPath-1][0];
				iMonsterY = iPath[iCurrentPath-1][1];
				
				iCurrentPath--;
				

				if( iCurrentPath <= 0 )
				{
					pfAStar.ResetPath();
					iCurrentPath = -1;
				}
			}


			
			iMonsterMove++;
			if( iMonsterMove > 1 ) iMonsterMove = 0;
			
		}
	}*/
}	
	
	
	
	
