package tracks.singlePlayer.past.Shmokin;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.TreeSet;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;


public class AStar {

    StateObservation StateObs;
    
    Node playerLocation;
    Node goalLocation;
    
    ArrayList<Node> openLocations = new ArrayList<Node>();
    ArrayList<Node> closedLocations = new ArrayList<Node>();
    ArrayList<Node> totalClosedLocations = new ArrayList<Node>();
    
    ArrayList<Node> exploredNodes = new ArrayList<Node>();
    
    public Path shortestPath;
    ArrayList<Path> availablePaths = new ArrayList<Path>();
    
    public int playerValue;
    public int currentGoalValue;
    public int pathCounter;
    public int pathCounter2;
    
    ArrayList<Observation> grid[][];
    boolean pathFound;
    boolean pathComplete;
    
    ArrayList<Node> spriteExplore = new ArrayList<Node>();
    
    int testCounter = 0;
    double A;
    GameInformation GameInfo;
    
    
    
    public AStar(StateObservation stateObs){

        playerValue = 0;
        currentGoalValue = 2;
        this.StateObs = stateObs;
        pathCounter = 0;
        pathCounter2 = 0;
        pathFound = false;
        pathComplete = false;
        A = 0.00;
       

        playerLocation = new Node();
        goalLocation = new Node();

    }
    
    public void sensorMap()
    {
        boolean goalExists = false;
          spriteExplore.clear();
         
        grid = StateObs.getObservationGrid();
        //System.out.println("#########################");
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {

                //Create a list of all the different interactionable nodes on the map
                if(grid[i][j].size() > 0)
                {
                    if ((grid[i][j].get(0).category == 4
                            && grid[i][j].get(0).itype == 0) || grid[i][j].get(0).category == 0 ) {
                        // it is a wall or the agent
                    }else
                    {
                        // Add to the list of interactionable GameInformation                
                        if(spriteExplore.size() != 0)
                        {
                            boolean isInList = false;
                           
                            for (Node node : spriteExplore) {
                                //check if the node is already in the sprite explore list.
                                if (node.getId() == grid[i][j].get(0).obsID) {
                                    isInList = true;
                                    break;
                                }
                            }
                           
                            if(!isInList)
                            {
                                
                                boolean addToTheList = true;
                                //check if it is in the list of explored nodes before adding it again
                                for(Node node: exploredNodes)
                                {
                                    if(node.getId() == grid[i][j].get(0).obsID)
                                    {
                                        addToTheList = false;
                                        break;
                                    }
                                }
                                
                                if(addToTheList)
                                {
                                    int distance = Math.abs(goalLocation.getX() - i)
                                            + Math.abs(goalLocation.getY() - j);
                                	
                                    spriteExplore.add(new Node(i, j, grid[i][j]
                                        .get(0).category,
                                        grid[i][j].get(0).itype, grid[i][j]
                                                .get(0).obsID, distance));
                                    
                                }
                            }
                        }
//                        THIS NEEDS CHANGED TO INCLUDE TIME AT START OF THE GAME
                        
                            if(spriteExplore.size() == 0){
                            
                            boolean addToTheList = true;
                            //check if it is in the list of explored nodes before adding it again
                            for(Node node: exploredNodes)
                            {
                                if(node.getId() == grid[i][j].get(0).obsID)
                                {
                                    addToTheList = false;
                                    break;
                                }
                            }
                            
                            int distance = Math.abs(goalLocation.getX() - i)
                                    + Math.abs(goalLocation.getY() - j);
                        	
                            
                            if(addToTheList)
                                spriteExplore.add(new Node(i, j, grid[i][j]
                                    .get(0).category,
                                    grid[i][j].get(0).itype, grid[i][j]
                                            .get(0).obsID, distance));
                        }                                           
                    }
                   
                    //Update the position of the player
                    if(grid[i][j].get(0).category == 0)
                    {
                        playerLocation.setPosition(i, j);
                       
                        playerLocation.setDistanceFromStart(0);
                        playerLocation = setNeighbours(playerLocation);

                    }
                   
                    //Check if the goal still exists
//                    if(grid[i][j].get(0).obsID == goalLocation.getId())
//                    {
//                        GameInfo.setGoalExists(true);
//                    }

                   
                   
                    //Update the nodes position in sprite explore.
                    //Not needed

                    //Update the goal location
//                    if(grid[i][j].get(0).obsID == goalLocation.getId())
//                    {
//                        goalLocation.setPosition(i, j);
//                    }
                }
               
               
//                if(spriteExplore.size() == 0 && exploredNodes.size() >0)
//                {
//                    exploredNodes.clear();
//                }
                
               
//                int n = 9;                                  
//                //The following if statement prints the map and path to console
//                //Is only needed for testing
//                if(grid[i][j].size() > 0)
//                    n = grid[i][j].get(0).category;
//                if (n != 9)
//                    System.out.print(n);
//                else
//                {
//                    boolean inpath = false;
//
//                    if (shortestPath != null) {
//                        for (Node pathNodes : shortestPath.getWayPointPath()) {
//                            if (pathNodes.getX() == i && pathNodes.getY() == j) {
//                                inpath = true;
//                                continue;
//                            }
//                        }
//                    }
//
//                    if (inpath)
//                        System.out.print('x');
//                    else
//                        System.out.print(' ');
//                }

            }
//            System.out.println();
        }
    }
    
    public Types.ACTIONS explore(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, GameInformation gameInfo) {
    //Exploration is done on the game map with either AStar or Monte Carlo depending on the agent positioning
    //Need to evaluate here if the agent should follow a path or if the monte carlo exploration should be started

        GameInfo = gameInfo;
       
        Types.ACTIONS action1 = null;


            StateObs = stateObs;

            //Creating the path on each tick as opposed to a new goal being created
            //How much this effects the speed?
            //if(shortestPath == null)
                //createPath();

                //push();
                
              
            //Create a map of the environment
            //Updates the sprite positions
            sensorMap();    
            
            checkEventHistory();
            
            if(GameInfo.reset || (goalLocation.getX() == 0 && goalLocation.getY() == 0))
            {
            	goalLocation = getNextGoal();
            	
            	GameInfo.reset = false;
            }

                
            
                //CheckEventHistory
                
                // Now we check if it is worth moving down the path or not
//                if (shortestPath.getWayPoint(pathCounter2).getY() > shortestPath
//                        .getWayPoint(pathCounter2).getParent().getY()) {
//                    action1 = ACTIONS.ACTION_DOWN;
//                    // re adjust where the agent is
//                    playerLocation.setPosition(playerLocation.getX(),playerLocation.getY() + 1);
//                }
//                if (shortestPath.getWayPoint(pathCounter2).getY() < shortestPath
//                        .getWayPoint(pathCounter2).getParent().getY()) {
//                    action1 = ACTIONS.ACTION_UP;
//                    playerLocation.setPosition(playerLocation.getX(),playerLocation.getY() - 1);
//                }
//                if (shortestPath.getWayPoint(pathCounter2).getX() < shortestPath
//                        .getWayPoint(pathCounter2).getParent().getX()) {
//                    action1 = ACTIONS.ACTION_LEFT;
//                    playerLocation.setPosition(playerLocation.getX() - 1,playerLocation.getY());
//                }
//                if (shortestPath.getWayPoint(pathCounter2).getX() > shortestPath
//                        .getWayPoint(pathCounter2).getParent().getX()) {
//                    action1 = ACTIONS.ACTION_RIGHT;
//                    playerLocation.setPosition(playerLocation.getX() + 1,playerLocation.getY());
//                }
//                pathCounter2++;

            action1 = ACTIONS.ACTION_UP;
            
            if(Math.abs(playerLocation.getX()-goalLocation.getX())
            		> Math.abs(playerLocation.getY()-goalLocation.getY())) 
            {
            	//move horizontal
            	if(playerLocation.getX()-goalLocation.getX() > 0)
            	{
            		action1 = ACTIONS.ACTION_LEFT;
            		playerLocation.setPosition(playerLocation.getX() - 1,playerLocation.getY());
            	}else{
            		action1 = ACTIONS.ACTION_RIGHT;
            		playerLocation.setPosition(playerLocation.getX() + 1,playerLocation.getY());
            	}
            	
            }else{
            	//move vertical
            	if(playerLocation.getY()-goalLocation.getY() > 0)
            	{
            		action1 = ACTIONS.ACTION_UP;
            		playerLocation.setPosition(playerLocation.getX(),playerLocation.getY() - 1);
            	}else{
            		action1 = ACTIONS.ACTION_DOWN;
            		playerLocation.setPosition(playerLocation.getX(),playerLocation.getY() + 1);
            	}            	
            	
            }
            
		if (grid[playerLocation.getX()][playerLocation.getY()].size() > 0) {
			if ((grid[playerLocation.getX()][playerLocation.getY()].get(0).category == 4 && grid[playerLocation
					.getX()][playerLocation.getY()].get(0).itype == 0)
					|| grid[playerLocation.getX()][playerLocation.getY()]
							.get(0).category == 3) {
				if (action1 == ACTIONS.ACTION_UP
						|| action1 == ACTIONS.ACTION_DOWN) {
					// want to move left or right
					action1 = ACTIONS.ACTION_RIGHT;
					if (playerLocation.getX() - goalLocation.getX() > 0) {
						action1 = ACTIONS.ACTION_LEFT;
						playerLocation.setPosition(playerLocation.getX() - 1,
								playerLocation.getY());
					} else {
						playerLocation.setPosition(playerLocation.getX() + 1,
								playerLocation.getY());
					}
				} else {
					// must be wanting to move left or right so change it to up
					// or down
					action1 = ACTIONS.ACTION_DOWN;
					if (playerLocation.getY() - goalLocation.getY() > 0) {
						action1 = ACTIONS.ACTION_UP;
						playerLocation.setPosition(playerLocation.getX(),
								playerLocation.getY() - 1);
					} else {
						playerLocation.setPosition(playerLocation.getX(),
								playerLocation.getY() + 1);
					}
				}
			}
		}
            	
            
            
//                if (playerLocation.getY() < goalLocation.getY()) {
//                    action1 = ACTIONS.ACTION_DOWN;
//      
//                    // re adjust where the agent is
//                    playerLocation.setPosition(playerLocation.getX(),playerLocation.getY() + 1);
//                    return action1;
//                }
//                if (playerLocation.getY() > goalLocation.getY()) {
//                    action1 = ACTIONS.ACTION_UP;
//                    playerLocation.setPosition(playerLocation.getX(),playerLocation.getY() - 1);
//                    return action1;
//                }
//                if (playerLocation.getX() < goalLocation.getX()) {
//                    action1 = ACTIONS.ACTION_RIGHT;
//                    playerLocation.setPosition(playerLocation.getX() + 1,playerLocation.getY());
//                    return action1;
//                }
//                if (playerLocation.getX() > goalLocation.getX()) {
//                    action1 = ACTIONS.ACTION_LEFT;
//                    playerLocation.setPosition(playerLocation.getX() - 1,playerLocation.getY());
//                    return action1;
//                }
                
                pathCounter2++;
                
            modeEvaluation(gameInfo);

            return action1;
    }
    
    public void push(){


    }
    
    
    public void checkEventHistory(){

    	if(GameInfo.getPreferredSpriteCategory() == 0)
    	{
			TreeSet<Event> test = StateObs.getEventsHistory();

			//Sets the type of preferred sprite to something that made the score increase
			if (StateObs.getGameScore() > GameInfo.getOldScore()) {
				for (Node node : spriteExplore) {
//					if (test.first().passiveId == node.getId()) {
//						GameInfo.setPreferredSpriteCategory(node.getCategory());
//						break;
//					}
					
					if(goalLocation.getId() == node.getId())
					{
						GameInfo.setPreferredSpriteCategory(node.getCategory());
						break;
					}
				}
			}
    	}
    }
    
    public void createPath(){
        
            AStarHeuristic heuristicStar = new ClosestHeuristic();

            // we want to start a new path.
//            if(pathCounter2 == gameInfo.getPathLength())
//                shortestPath = null;
            
            openLocations.clear();
            closedLocations.clear();

            if(GameInfo.reset)
            {
                shortestPath = null;
                pathCounter2 = 0;

                pathFound = false;
                GameInfo.reset = false;
            }
              
            //Create a map of the environment
            //Updates the sprite positions
            sensorMap();    

           
            if(shortestPath == null)
                goalLocation = getNextGoal();
            
            if (shortestPath == null) {

//                shortestPath = null;
//                pathCounter2 = 0;
//
//                pathFound = false;
             openLocations.add(playerLocation);
                
//                for(Node node: totalClosedLocations)
//                    closedLocations.add(node);
                // While the goal has not been found yet
                while (openLocations.size() != 0 || pathFound != true) {
                    // get the first node from the open list
                    Node current = openLocations.get(0);
                    shortestPath = reconstructPath(current);

                    // check if current node is the goal node
                    if (current.getX() == goalLocation.getX()
                            && current.getY() == goalLocation.getY()
                            //|| shortestPath.getWayPointPath().size() > GameInfo.getPathLength() + GameInfo.getPathCounter()
                              //|| shortestPath.getWayPointPath().size() >= 5
                            ) {
                        shortestPath = reconstructPath(current);
                        pathFound = true;

                        for(Node node: shortestPath.getWayPointPath())
                            totalClosedLocations.add(node);

                        // path has been found
                        break;
                    }

                    // move current node to the already searched (closed) list
                    openLocations.remove(current);
                    closedLocations.add(current);

                    // set the current nodes neighbours
                    current = setNeighbours(current);

                    // Now it's time to go through all of the current nodes
                    // neighbours and see if they should be the next step
                    for (Node neighbor : current.getNeighborList()) {
                        boolean neighborIsBetter;

                        // if we have already searched this Node, don't bother and
                        // continue to the next one
                        if (closedLocations.contains(neighbor)) {
                            continue;
                        }

                        boolean found = false;
                        for (Node neighbournode : closedLocations) {
                            if (neighbournode.getX() == neighbor.getX()
                                    && neighbournode.getY() == neighbor.getY()) {
                                found = true;
                                continue;
                            }
                        }

                        if (found)
                            continue;
                       
                        Node movable = new Node(neighbor.getX(), neighbor.getY(), 
                                neighbor.getCategory(), neighbor.getItype(), neighbor.getId(), 0);

                        if (grid[movable.getX()][movable.getY()].size() > 0) {
                            // check to make sure that the square is not of category
                            // 4(immovable object) or category 3(enemy)
                            if ((grid[movable.getX()][movable.getY()].get(0).category == 4 && grid[movable
                                    .getX()][movable.getY()].get(0).itype == 0)
                                    && grid[movable.getX()][movable.getY()].get(0).obsID != goalLocation.getId()
                                    ) {
                                // You cannot move on this square
                                neighbor.setMoveable(false);
                            } else {
                                // You can move on this square. Set parent location
                                // as the players current position.
                                movable.setParent(playerLocation);
                            }
                        }

                        // also just continue if the neighbor is an obstacle
                        if (neighbor.getMoveable()) {

                            // calculate how long the path is if we choose this
                            // neighbor
                            // as the next step in the path
                            float neighborDistanceFromStart = (current
                                    .getDistanceFromStart() + getDistanceBetween(
                                    current, neighbor));

                            // add neighbor to the open list if it is not there
                            if (!openLocations.contains(neighbor)) {
                                openLocations.add(neighbor);
                                neighborIsBetter = true;
                                // if neighbor is closer to start it could also be
                                // better
                            } else if (neighborDistanceFromStart < current
                                    .getDistanceFromStart()) {
                                neighborIsBetter = true;
                            } else {
                                neighborIsBetter = false;
                            }
                            // set neighbors parameters if it is better
                            if (neighborIsBetter) {
                                neighbor.setParent(current);
                                neighbor.setDistanceFromStart(neighborDistanceFromStart);
                                neighbor.setHeuristicDistanceFromGoal(heuristicStar
                                        .getEstimatedDistanceToGoal(
                                                neighbor.getX(), neighbor.getY(),
                                                goalLocation.getX(),
                                                goalLocation.getY()));
                            }
                        }

                    }
                }

                System.out.println("====================");
            }
    }
    
    
    public Node setNeighbours(Node playerLocation){

        //Fill the open locations with the positions near the player
        playerLocation.setUp(new Node(playerLocation.getX(), playerLocation.getY() - 1)); //above location
        playerLocation.setDown(new Node(playerLocation.getX(), playerLocation.getY() + 1)); //below location
        playerLocation.setLeft(new Node(playerLocation.getX() - 1, playerLocation.getY())); //left location
        playerLocation.setRight(new Node(playerLocation.getX() + 1, playerLocation.getY())); //right location
       
        return playerLocation;
    }
    
    private Path reconstructPath(Node node) {
       
        //Recreates the best path by using the parent nodes of each node starting at the target node.
        Path path = new Path();
        while(!(node.getParent() == null)) {
                path.prependWayPoint(node);
                node = node.getParent();
        }
        return path;
}

    
    public float getDistanceBetween(Node node1, Node node2) {
        // if the nodes are on top or next to each other, return 1
        if (node1.getX() == node2.getX() || node1.getY() == node2.getY()) {
            return 1 * (grid[0].length + grid.length);
        } else { // if they are diagonal to each other return diagonal distance:
                    // sqrt(1^2+1^2)
            return (float) 1.7 * (grid[0].length + grid.length);
        }
    }
    
    
    public Node getNextGoal(){
        if(spriteExplore.size() > 0)
        {
        	boolean goalSet = false;
        	for(int z = 0; z < spriteExplore.size(); z++)
        	{
        		if(spriteExplore.get(z).getCategory() == GameInfo.getPreferredSpriteCategory())
        		{
        			goalLocation = spriteExplore.get(z);
        			goalSet = true;
        			break;
        		}	
        	}
        	
        	if(!goalSet)
        		goalLocation = spriteExplore.get(0);

        }

        //If the game has explored all the possible GameInformation then we should start from the start and begin exploring them again
        //Because of the pathing it is possible they were not fully explored to begin with
     if(spriteExplore.size() == 0 && exploredNodes.size() >0)
     {
         goalLocation = exploredNodes.get(0);
         exploredNodes.clear();
     }
        
        //Make sure this sprite can't be rechecked
        boolean addToList = true;
        for(Node node: exploredNodes)
        {
            if(node.getId() == goalLocation.getId())
            {
                addToList = false;
                break;
            }
        }

        if(addToList)// && distanceGoalPlayer <= GameInfo.getModeSwitchDistance())
            exploredNodes.add(goalLocation);
        
        return goalLocation;      
    }   
    
    public void modeEvaluation(GameInformation gameInfo)
    {

        if (gameInfo.mode == GameInformation.Mode.AStar) {

            int distanceGoalPlayer = Math.abs(goalLocation.getX()
                    - playerLocation.getX())
                    + Math.abs(goalLocation.getY() - playerLocation.getY());

//            if(shortestPath != null)
//            {
//                if(shortestPath.contains(goalLocation.getX(), goalLocation.getY()))
//                    gameInfo.setMode(Mode.MonteCarlo);
//            }
            if (distanceGoalPlayer <= gameInfo.getModeSwitchDistance()) {
                gameInfo.setMode(GameInformation.Mode.MonteCarlo);
            }
          
//            if (pathCounter2 > shortestPath.getWayPointPath().size()
//                    - gameInfo.getModeSwitchDistance()) 
//                gameInfo.setMode(Mode.MonteCarlo);
                
        } else if (gameInfo.mode == GameInformation.Mode.MonteCarlo) {
            // if the agent is on the goal location or if it has been removed
            // from the grid
            // will need some adaption

//            int test = Math.abs(goalLocation.getX() - playerLocation.getX())
//                    + Math.abs(goalLocation.getY() - playerLocation.getY());
//
//            if (Math.abs(goalLocation.getX() - playerLocation.getX())
//                    + Math.abs(goalLocation.getY() - playerLocation.getY()) > gameInfo
//                    .getModeSwitchDistance() + 2) {
//                gameInfo.setMode(Mode.AStar);
//               
//                
//            }
        }
    }
}
