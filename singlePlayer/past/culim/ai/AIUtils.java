package tracks.singlePlayer.past.culim.ai;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class AIUtils
{	
	public static final boolean DEBUG = false;
	
	public static void log(String s)
	{
		if (DEBUG)
		{
			System.out.println(s);
		}
	}
	public static void write(Object obj, String filename)
	{
		try
	      {
	         FileOutputStream fileOut = new FileOutputStream(filename);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(obj);
	         out.close();
	         fileOut.close();
	         System.out.println(String.format("Serialized data is saved in %s", filename));
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T read(String filename)
	{
		T ret = null;
		  try
		  {
		     FileInputStream fileIn = new FileInputStream(filename);
		     ObjectInputStream in = new ObjectInputStream(fileIn);
		     ret = (T) in.readObject();
		     in.close();
		     fileIn.close();
		     return ret;
		  }catch(IOException i)
		  {
		     i.printStackTrace();
		     return ret;
		  }catch(ClassNotFoundException c)
		  {
		     System.out.println("Employee class not found");
		     c.printStackTrace();
		     return ret;
		  }
	}
	
	/**
	 * Calculates the average square distance of all  to the avatar.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to all NPCs.
	 */
	public static double getMeanObservationSquareDistance(ArrayList<Observation>[] observationPositionsList)
	{
		double sumSquareDistance = 0;
		int count = 0;
		
		if (observationPositionsList == null)
		{
			return 0;
		}
		
		for (ArrayList<Observation> observationPositions : observationPositionsList)
		{
			for (Observation observation : observationPositions)
			{
				sumSquareDistance += observation.sqDist;
				count++;
			}
		}
		
		if (count == 0)
		{
			return 0;
		}
		
		
		return sumSquareDistance/count;
	} 
	
	/**
	 * Calculates the mean istance of the closest NPC.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to closest NPCs.
	 */
	public static double getMeanClosestNPCDistance(StateObservation stateObs)
	{
		double sumSquareDistance = 0;
		int count = 0;
		
		ArrayList<Observation>[] observationPositionsList = stateObs.getNPCPositions(stateObs.getAvatarPosition());
		if (observationPositionsList == null)
		{
			return 0;
		}
		
		for (ArrayList<Observation> observationPositions : observationPositionsList )
		{
			if (observationPositions.size() > 0)
			{
				sumSquareDistance += observationPositions.get(0).sqDist;
				count++;
			}
		}
		
		if (count == 0)
		{
			return 0;
		}
		
		return sumSquareDistance/count;
	}
	
	/**
	 * Calculates the average square distance of all the resources to the avatar.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to all NPCs.
	 */
	public static double getMeanResourceSquareDistance(StateObservation stateObs)
	{
		ArrayList<Observation>[] resourcePositions =  stateObs.getResourcesPositions(stateObs.getAvatarPosition());
		return getMeanObservationSquareDistance(resourcePositions);
	}
	
	/**
	 * Calculates the average square distance of all the NPCs to the avatar.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to all NPCs.
	 */
	public static double getMeanNpcSquareDistance(StateObservation stateObs)
	{
		ArrayList<Observation>[] npcPositions =  stateObs.getNPCPositions(stateObs.getAvatarPosition());
		return getMeanObservationSquareDistance(npcPositions);
	}
	
	/**
	 * Calculates the average square distance of all the <code>Immovable</code> sprites to the avatar.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to all NPCs.
	 */
	public static double getMeanImmovableSquareDistance(StateObservation stateObs)
	{
		ArrayList<Observation>[] immovablePositions =  stateObs.getImmovablePositions(stateObs.getAvatarPosition());
		return getMeanObservationSquareDistance(immovablePositions);
	}
	
	/**
	 * Calculates the average square distance of all the <code>Movable</code> sprites to the avatar.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to all movable sprites.
	 */
	public static double getMeanMovableSquareDistance(StateObservation stateObs)
	{
		ArrayList<Observation>[] movablePositions =  stateObs.getMovablePositions(stateObs.getAvatarPosition());
		return getMeanObservationSquareDistance(movablePositions);
	}
	
	/**
	 * Calculates the average square distance of all the <code>Portal</code> sprites to the avatar.
	 * 
	 * @param stateObs the {@link StateObservation} object
	 * @return the calculated average square distance from the avatar to all portals.
	 */
	public static double getMeanPortalSquareDistance(StateObservation stateObs)
	{
		ArrayList<Observation>[] portalPositions =  stateObs.getPortalsPositions(stateObs.getAvatarPosition());
		return getMeanObservationSquareDistance(portalPositions);
	}

	/**
	 * Returns a random {@link Integer} between the numbers <code>low</code> (inclusive)
	 * and <code>high</code> (exclusive).
	 * 
	 * @param low lowest value (inclusive)
	 * @param high highest value (exclusive)
	 * @return random {@link Integer} between the numbers <code>low</code> (inclusive)
	 */
	public static int randomInt(int low, int high)
	{
		return (int) (low + high*Math.random()); 
	}
	
	/**
	 * Returns a random element from an {@link ArrayList}
	 * @param arrayList list of elements
	 * @return randomly selected element from the list
	 */
	public static <T> T randomElement(ArrayList<T> arrayList)
	{
		T element = null;
		
		if (arrayList != null)
		{
			if (!arrayList.isEmpty())
			{
				element = arrayList.get(randomInt(0, arrayList.size()));
			}
			else
			{
				AIUtils.log("randomElement(), arrayList is empty.");
			}
		}
		else
		{
			AIUtils.log("randomElement(), arrayList=null");
		}
		
		return element;
	}
	
	public static Vector2d getGridDimension(StateObservation stateObs)
	{
		Dimension dimension = stateObs.getWorldDimension();
		int gridWidth = dimension.width/stateObs.getBlockSize();
		int gridHeight = dimension.height/stateObs.getBlockSize();
		
		return new Vector2d(gridWidth, gridHeight);
	}
	
	public static Vector2d getGridPosition(StateObservation stateObs, Observation obs)
	{
		Dimension dimension = stateObs.getWorldDimension();
		int blockSize = stateObs.getBlockSize();
		
		int gridX = -1;
		int gridY = -1;
		
		if (obs != null)
		{
			gridX = (int) (obs.position.x / blockSize);
			gridY = (int) (obs.position.y / blockSize);
		}
		
		
		return new Vector2d(gridX, gridY);
	}
	
	public static Vector2d getGridPosition(StateObservation stateObs, Vector2d pos)
	{
		Dimension dimension = stateObs.getWorldDimension();
		int blockSize = stateObs.getBlockSize();
		int gridX = (int) (pos.x / blockSize);
		int gridY = (int) (pos.y / blockSize);
		
		
		return new Vector2d(gridX, gridY);
	}
	
	public static int getManhattanDistance(StateObservation stateObs, Observation obs1, Observation obs2)
	{
		Vector2d grid1 = getGridPosition(stateObs, obs1);
		Vector2d grid2 = getGridPosition(stateObs, obs2);
		
		return (int) (Math.abs(grid1.x - grid2.x) + Math.abs(grid1.y - grid2.y));
	}
	
	public static int getManhattanDistanceFromWorldPositions(StateObservation stateObs, Vector2d pos1, Vector2d pos2)
	{
		Vector2d grid1 = getGridPosition(stateObs, pos1);
		Vector2d grid2 = getGridPosition(stateObs, pos2);
		
		return (int) (Math.abs(grid1.x - grid2.x) + Math.abs(grid1.y - grid2.y));
	}
	
	public static int getNearestNPCManhattanDistance(StateObservation stateObs)
	{
		Observation nearest = getNearestNPC(stateObs);
		if (nearest == null)
		{
			return 999999;
		}
		return getManhattanDistanceFromWorldPositions(stateObs, stateObs.getAvatarPosition(), nearest.position );
	}
	
	public static Observation getNearestNPC(StateObservation stateObs)
	{
		ArrayList<Observation>[] positionsList = stateObs.getNPCPositions(stateObs.getAvatarPosition());
		return getNearestObervation(positionsList);
	}
	
	public static Observation getNearestMovable(StateObservation stateObs)
	{
		ArrayList<Observation>[] positionsList = stateObs.getMovablePositions(stateObs.getAvatarPosition());
		return getNearestObervation(positionsList);
	}
	
	public static Observation getNearestObervation(ArrayList<Observation>[] positionsList)
	{
		if (positionsList == null)
		{
			return null;
		}
		
		for (ArrayList<Observation> observationPositions : positionsList )
		{
			if (observationPositions.size() > 0)
			{
				return observationPositions.get(0);
			}
		}
		
		return null;
	}
	
	public static Vector2d getNearestNPCGridPosition(StateObservation stateObs)
	{
		return getGridPosition(stateObs, getNearestNPC(stateObs));
//		return getNearestNPC(stateObs).position;
	}
	
	public static int getNearestMovableId(StateObservation stateObs)
	{
		Observation movable = getNearestMovable(stateObs);
		if (movable != null)
		{
			return movable.obsID;
		}
		
		return -1;
	}
	
	public static int getNearestMovableType(StateObservation stateObs)
	{
		Observation movable = getNearestMovable(stateObs);
		if (movable != null)
		{
			return movable.itype;
		}
		
		return -1;
	}
	
	public static int getNearestNPCId(StateObservation stateObs)
	{
		Observation nearestNPC = getNearestNPC(stateObs);
		if (nearestNPC == null)
		{
			return -1;
		}
		return getNearestNPC(stateObs).obsID;
	}
	
	public static int getRemainingNPCs(StateObservation stateObs)
	{
		ArrayList<Observation>[] typePositions = stateObs.getNPCPositions(stateObs.getAvatarPosition());
		if (typePositions == null)
		{
			return 0;
		}
		
		int count = 0;
		
		for (ArrayList<Observation> npcPositions : typePositions)
		{
			count += npcPositions.size();
		}
		
		return count;
	}
	public static Object getNearestMovableGridPosition(StateObservation stateObs)
	{
		return getGridPosition(stateObs, getNearestMovable(stateObs));
	}
	
	public static boolean isUnchangingMove(StateObservation stateObs, ACTIONS action) 
	{
		if (action == ACTIONS.ACTION_USE || action == ACTIONS.ACTION_NIL) {
			return false;
		}
		StateObservation state = stateObs.copy();
		Vector2d avatarPosition = state.getAvatarPosition();
		state.advance(action);
		Vector2d postAvatarPosition = state.getAvatarPosition();
		return avatarPosition.equals(postAvatarPosition);
			
	}
}
