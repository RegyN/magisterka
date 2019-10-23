package tracks.singlePlayer.past.JinJerry;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import ontology.Types;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;

public class MyStateHeuristic extends StateHeuristic {

	double winGameScore = 1000000;
	double loseGameScore = Double.NEGATIVE_INFINITY;
	
	int worldWidth = 0;
	int worldHeight = 0;
	int worldBlockSize = 0;
	
	double distanceFactor;
	
	private ArrayList<Integer> enemyTypeList;
	private ArrayList<Integer> resourceTypeList;
	double [][] visitedCountMap;
	
	double unvisitedScore = 0;
	double resourceDistanceScore = 0;
	double portalDistanceScore = 0;
	double npcDistanceScore = 0;
	double movableDistanceScore = 0;
	
	Vector2d positionBeforeAdvance;
	Types.ACTIONS currentAction;
	
	int movableCategory = -1;
	int immovableCategory = -1;
	
	public MyStateHeuristic(StateObservation stateObs) {
		// TODO Auto-generated constructor stub
		Dimension worldDimension = stateObs.getWorldDimension();
		worldBlockSize = stateObs.getBlockSize();
		worldWidth = worldDimension.width / worldBlockSize;
		worldHeight = worldDimension.height / worldBlockSize;
//		System.out.printf("The worldBlockSize is %d\n", worldBlockSize);
//		System.out.printf("The world size is %d x %d blocks\n", worldWidth, worldHeight);
		
		distanceFactor = worldWidth * worldHeight * worldBlockSize;
		initWorldMap(stateObs);
		
		enemyTypeList = new ArrayList<Integer>();
		resourceTypeList = new ArrayList<Integer>();
		positionBeforeAdvance = new Vector2d();
		setUpParameters(stateObs);
	}
	
	private void setUpParameters(StateObservation stateObs) {
		
		ArrayList<Observation>[] movablePositionArrayLists = stateObs.getMovablePositions();
		if (movablePositionArrayLists != null) {
			ArrayList<Observation> movableList = movablePositionArrayLists[0];
			movableCategory = movableList.get(0).category;
		}
        ArrayList<Observation>[] immovableaArrayList = stateObs.getImmovablePositions();
        if (immovableaArrayList != null) {
			ArrayList<Observation> immovableList = immovableaArrayList[0];
			immovableCategory = immovableList.get(0).category;
		}
        ArrayList<Observation>[] resourcesPositionsArrayLists = stateObs.getResourcesPositions();
        if (resourcesPositionsArrayLists != null) {
        	for (ArrayList<Observation> resources : resourcesPositionsArrayLists) {
        		if (resources.size() > 0) {
        			resourceTypeList.add(resources.get(0).itype);
        		}
        	}
        }
	}
	
	private void initWorldMap(StateObservation stateObs) {
		visitedCountMap = new double [worldWidth][worldHeight];
//		System.out.printf("map size %d %d", map.length, map[0].length);
		for (int i = 0; i < worldWidth; i++) {
			for (int j = 0; j < worldHeight; j++) {
				visitedCountMap[i][j] = 0.0;
			}
		}
	}
	
	public void updateMap(StateObservation stateObs, Types.ACTIONS action) {
		
		Vector2d avatarPosition = stateObs.getAvatarPosition();
		int x = (int) (avatarPosition.x / worldBlockSize);
        int y = (int) (avatarPosition.y / worldBlockSize);
        switch (action) {
		case ACTION_DOWN:
			y++;
			break;
		case ACTION_UP:
			y--;
			break;
		case ACTION_LEFT:
			x--;
			break;
		case ACTION_RIGHT:
			x++;
		case ACTION_USE:
			break;
		default:
			break;
		}
        
        if (x < 0 || x >= visitedCountMap.length || y < 0 || y >= visitedCountMap[0].length) {
        	return;
        }
        visitedCountMap[x][y]++;
        
//        for (int b = 0; b < visitedCountMap[0].length; b++) {
//			for (int a = 0; a < visitedCountMap.length; a++) {
//				System.out.print("  " + visitedCountMap[a][b]);
//			}
//			System.out.println();
//		}
	}
	
	public void setBeforeAdvance(StateObservation stateObs, Types.ACTIONS action) {
		Vector2d avatarPosition = stateObs.getAvatarPosition();
//		System.out.println("action:" + action);
//		System.out.println("position before advance: " + avatarPosition.x + " " + avatarPosition.y);
		positionBeforeAdvance.set(avatarPosition);
		currentAction = action;
		// reset all scores before evaluation.
		unvisitedScore = 0;
		resourceDistanceScore = 0;
		portalDistanceScore = 0;
		npcDistanceScore = 0;
		movableDistanceScore = 0;
		//the less visited position has higher score.
        int x = (int) (avatarPosition.x / worldBlockSize);
        int y = (int) (avatarPosition.y / worldBlockSize);
        switch (action) {
		case ACTION_DOWN:
			y++;
			break;
		case ACTION_UP:
			y--;
			break;
		case ACTION_LEFT:
			x--;
			break;
		case ACTION_RIGHT:
			x++;
		case ACTION_USE:
			break;
		default:
			break;
		}
//        System.out.println("position to visit: " + x + " " + y);
        // array bound check.
        if (x < 0 || x >= visitedCountMap.length || y < 0 || y >= visitedCountMap[0].length) {
        	return;
        }
        unvisitedScore = (distanceFactor -visitedCountMap[x][y]) / distanceFactor;
	}

	@Override
	public double evaluateState(StateObservation stateObs) {
        Vector2d avatarPosition = stateObs.getAvatarPosition();
        ArrayList<Observation>[] npcPositionsArrayLists = stateObs.getNPCPositions(avatarPosition);
        ArrayList<Observation>[] movablePositionArrayLists = stateObs.getMovablePositions(avatarPosition);
        ArrayList<Observation>[] portalPositionsArrayLists = stateObs.getPortalsPositions(avatarPosition);
        ArrayList<Observation>[] resourcesPositionsArrayLists = stateObs.getResourcesPositions(avatarPosition);
        HashMap<Integer, Integer> avatarResources = stateObs.getAvatarResources();
        TreeSet<Event> history = stateObs.getEventsHistory();
        ArrayList<Observation> [][] observationGrid = stateObs.getObservationGrid();
        
        // check win or lose.
        double winScore = 0;
        if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
        	winScore =  winGameScore;
        } else if (stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
        	// record the sprite type which caused agent dead.
        	if (history.size() > 0) {
        		Event deathEvent = history.last();
//            	System.out.printf("enemy type id:%d\n", deathEvent.passiveTypeId);
            	if (!enemyTypeList.contains(deathEvent.passiveTypeId)) {
            		enemyTypeList.add(deathEvent.passiveTypeId);
            	}
        	}
            return loseGameScore;
        }
        
        // try to deal with wall collision problem.
//        System.out.println("position after advance: " + avatarPosition.x + " " + avatarPosition.y);
        if (avatarPosition.equals(positionBeforeAdvance) && currentAction != Types.ACTIONS.ACTION_USE) {

        	int x = (int) (avatarPosition.x / worldBlockSize);
            int y = (int) (avatarPosition.y / worldBlockSize);
            
            Vector2d orientation = stateObs.getAvatarOrientation();
            x += orientation.x;
            y += orientation.y;
          
            if (x < 0) {
            	x = 0;
            }
            if (x >= worldWidth) {
            	x = worldWidth -1;
            }
            if (y < 0) {
            	y = 0;
            }
            if (y >= worldHeight) {
            	y = worldHeight -1;
            }
            
            ArrayList<Observation> observationList = observationGrid[x][y];
            
            if (observationList != null) {
            	for (Observation observation : observationList) {
                	if (observation.category == movableCategory || observation.category == immovableCategory) {
                		return loseGameScore;
                	}
            	}
            }
        }
        
        // check avatar's resources.
        double avatarResourceScore = 0;
        if (currentAction != Types.ACTIONS.ACTION_USE) {
        	if (avatarResources != null) {
            	for (int i = 0; i < resourceTypeList.size(); i++) {
            		Integer res = avatarResources.get(resourceTypeList.get(i));
           		 	if (res != null) {
           		 		avatarResourceScore += res.doubleValue();
           		 	}
            	}
            }
        }
        
        // calculate resource score.
        if (resourcesPositionsArrayLists != null) {
        	double collectDistance = worldHeight * worldWidth;
        	for (ArrayList<Observation> resources : resourcesPositionsArrayLists) {
        		if (resources.size() > 0) {
        			double dis = Math.sqrt(resources.get(0).sqDist);
        			if (dis < collectDistance) {
        				collectDistance = dis;
        			}
        		}
        	}
        	resourceDistanceScore = (distanceFactor -collectDistance) / distanceFactor;
        }
        else {
        	// portal has score if no resources.
			if (portalPositionsArrayLists != null) {
				double portalDistance = worldHeight * worldWidth;
	        	for (ArrayList<Observation> portals : portalPositionsArrayLists) {
	        		if (portals.size() > 0) {
	        			double dis = Math.sqrt(portals.get(0).sqDist);
	        			if (dis < portalDistance) {
	        				portalDistance = dis;
	        			}
	        		}
	        	}
	        	portalDistanceScore = portalDistance / distanceFactor;
			}
		}
       
        // calculate NPC score.
        if (npcPositionsArrayLists != null) {
            double awayDistance = worldHeight * worldWidth;
            double chaseDistance = awayDistance;
            for (ArrayList<Observation> npcs : npcPositionsArrayLists) {
            	if (npcs.size() > 0) {
            		int npcTypeId = npcs.get(0).itype;
//                	System.out.printf("npc type id:%d\n", npcTypeId);
            		// if NPC is harmful, run away. if not, chase.
            		if (enemyTypeList.contains(npcTypeId)) {
            			double dis = Math.sqrt(npcs.get(0).sqDist);
                		if (dis < awayDistance) {
                			awayDistance = dis;
                		}
            		}
            		else {
            			double dis = Math.sqrt(npcs.get(0).sqDist);
                		if (dis < chaseDistance) {
                			chaseDistance = dis;
                		}
            		}
            	}
            }
            npcDistanceScore = (awayDistance *3 - chaseDistance) / distanceFactor;
        }
        
        // calculate movable score.
        if (movablePositionArrayLists != null) {
            double cloestDistance = worldHeight * worldWidth;
            for (ArrayList<Observation> movables : movablePositionArrayLists) {
            	// if movable sprite is harmful, run away
            	if (movables.size() > 0) {
            		int movableTypeId = movables.get(0).itype;
//            		System.out.printf("movable type id:%d\n", movableTypeId);
            		if (enemyTypeList.contains(movableTypeId)) {
            			double dis = Math.sqrt(movables.get(0).sqDist);
                		if (dis < cloestDistance) {
                			cloestDistance = dis;
                		}
            		}
            		else {
            			cloestDistance = 0;
					}
            	}
            }
            movableDistanceScore = cloestDistance / distanceFactor;
        }
        
        // calculate final score.
        double score = winScore + stateObs.getGameScore() + avatarResourceScore + unvisitedScore * 5 
        		+ resourceDistanceScore * 2 - portalDistanceScore + npcDistanceScore + movableDistanceScore;

        return score;
    }
}
