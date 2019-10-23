package tracks.singlePlayer.past.Normal_MCTS.Normal_MCTS;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;

public class SingleTreeNode
{
    private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
    public static double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.07;
    public StateObservation state;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public static Random m_rnd;
    private int m_depth;
    private static double[] lastBounds = new double[]{0,1};
    private static double[] curBounds = new double[]{0,1};
    public static int LIMIT_TIME = 8;
    
    private ModelizseGame modelize = new ModelizseGame();

    public SingleTreeNode(Random rnd) {
        this(null, null, rnd);
    }

    public SingleTreeNode(StateObservation state, SingleTreeNode parent, Random rnd) {
        this.state = state;
        this.parent = parent;
        this.m_rnd = rnd;
        children = new SingleTreeNode[Agent.NUM_ACTIONS];
        totValue = 0.0;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        lastBounds[0] = curBounds[0];
        lastBounds[1] = curBounds[1];

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis(); 
        int numIters = 0;
        
        int remainingLimit = 8;
        
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy();
            double delta = selected.rollOut();
            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
        }
        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
//        if(this.state.getNPCPositions(new Vector2d(2,2))!=null){
//        	ArrayList<Observation>[] obs = this.state.getNPCPositions(this.state.getAvatarPosition());
//        	System.out.println("-- " + this.state.getGameScore() + " -- "+obs[0].size()+"( " + this.state.getNPCPositions(this.state.getAvatarPosition())[0].get(0).position.x + ","+
//        			this.state.getNPCPositions(this.state.getAvatarPosition())[0].get(0).position.y+")");
//        	System.out.println("-- " + this.state.getGameScore() + " -- "+this.state.getNPCPositions()[0].size()+"( " + this.state.getNPCPositions()[0].get(0).position.x + ","+
//        			this.state.getNPCPositions()[0].get(0).position.y+")");
//        }
        	
    }

    public SingleTreeNode treePolicy() {

        SingleTreeNode cur = this;

        while (!cur.state.isGameOver() && cur.m_depth < Agent.ROLLOUT_DEPTH)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand();

            } else {
                //SingleTreeNode next = cur.uct();
                //SingleTreeNode next = cur.egreedy();
            	SingleTreeNode next = cur.uctAndGreedy();
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand() {

        int bestAction = 0;
        double bestValue = -1;
        //*--
//        StateObservation tempState;
//        ArrayList<Observation>[] obs; //= this.state.getNPCPositions(this.state.getAvatarPosition());
//        double worstcase = getDistance(obs[0].get(0).position.x,obs[0].get(0).position.y);
    	//*--
        

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble(); 
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        } 

        StateObservation nextState = state.copy();
        nextState.advance(Agent.actions[bestAction]);
        
        

        SingleTreeNode tn = new SingleTreeNode(nextState, this, this.m_rnd);
        children[bestAction] = tn;
        return tn;

    }

    public SingleTreeNode uct() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            double uctValue = childValue +
                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon)) +
                    this.m_rnd.nextDouble() * this.epsilon;

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }

        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
        }

        return selected;
    }
    
    
    
    public SingleTreeNode uctAndGreedy() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            double uctValue = childValue +
                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon)) +
                    this.m_rnd.nextDouble() * this.epsilon + hvVal;

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }

        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
        }

        return selected;
    }
    

    public SingleTreeNode egreedy() {


        SingleTreeNode selected = null;

        if(m_rnd.nextDouble() < egreedyEpsilon)
        {
            //Choose randomly
            int selectedIdx = m_rnd.nextInt(children.length);
            selected = this.children[selectedIdx];

        }else{
            //pick the best Q.
            double bestValue = -Double.MAX_VALUE;
            for (SingleTreeNode child : this.children)
            {
                double hvVal = child.totValue;

                // small sampleRandom numbers: break ties in unexpanded nodes
                if (hvVal > bestValue) {
                    selected = child;
                    bestValue = hvVal;
                }
            }

        }


        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + this.children.length);
        }

        return selected;
    }


    public double rollOut()
    {
        StateObservation rollerState = state.copy();
        StateObservation curRollerState = state.copy();
        int thisDepth = this.m_depth;
        
    	
        while (!finishRollout(rollerState,thisDepth)) { // if not finished repeat
        	
            int action = m_rnd.nextInt(Agent.NUM_ACTIONS);
            
            if(Agent.Attacking ){
            	action =m_rnd.nextInt(Agent.NUM_ACTIONS + 1 );
            	if( action > Agent.NUM_ACTIONS-1 )
            		action = 0;
            }
            
            rollerState.advance(Agent.actions[action]);

            modelize.modelizeState(curRollerState, action);
            
            thisDepth++;
        }

        double delta = value(rollerState);
        //double delta = modelize.value(rollerState);

        if(delta < curBounds[0]) curBounds[0] = delta;
        if(delta > curBounds[1]) curBounds[1] = delta;

        double normDelta = Utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        return normDelta;
    }
    
    public double getDistance(double x,double y){
    	return Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2));
    }

    public double value(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();
        Vector2d AvatarPosition = a_gameState.getAvatarPosition();
        ArrayList<Types.ACTIONS> action = a_gameState.getAvailableActions();
        
        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return HUGE_NEGATIVE; // -10000000.0;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return HUGE_POSITIVE; // 10000000.0;
        
        
        /*ArrayList<Observation>[] resOb = a_gameState.getResourcesPositions(AvatarPosition);
        ArrayList<Observation>[] movOb = a_gameState.getMovablePositions(AvatarPosition);
        ArrayList<Observation>[] npcOb = a_gameState.getResourcesPositions(AvatarPosition);
        ArrayList<Observation>[] immovOb = a_gameState.getResourcesPositions(AvatarPosition);
        ArrayList<Observation>[] fasOb = a_gameState.getResourcesPositions(AvatarPosition);
        ArrayList<Observation>[] portalOb = a_gameState.getResourcesPositions(AvatarPosition);
        rawScore = setScore(MacroCheck.resOper, resOb, AvatarPosition, rawScore);
        rawScore = setScore(MacroCheck.movOper, movOb, AvatarPosition, rawScore);
        rawScore = setScore(MacroCheck.npcOper, npcOb, AvatarPosition, rawScore);
        rawScore = setScore(MacroCheck.immovOper, immovOb, AvatarPosition, rawScore);
        rawScore = setScore(MacroCheck.fasOper, fasOb, AvatarPosition, rawScore);
        rawScore = setScore(MacroCheck.portalOper, portalOb, AvatarPosition, rawScore);*/
        
        return rawScore;
    }
    
    public double setScore(int Oper, ArrayList<Observation>[] Observe,Vector2d avaPostion, double rawScore){
    	if(Oper != 0 && isExist(Observe) ){
    		for(int i=0; i<Observe.length; i++){
                for(int j=0; j < getLoopSize(Observe[i]); j++ )
                    rawScore += Oper *(avaPostion.dist(Observe[i].get(j).position) ) ;
    		}
    	}
    	return rawScore;
    }
    
    public boolean isExist(ArrayList<Observation>[] checkObserve){
    	if(checkObserve != null)
    		return true;
    	else
    		return false;
    }
    
    private int getLoopSize(ArrayList<Observation> curObserve ){
    	if ( curObserve.size() > 3 )
    		return 4;
    	else
    		return curObserve.size();
    	
    }
    

    // (Game is Over) return true;
    public boolean finishRollout(StateObservation rollerState, int depth)
    {
        if(depth >= Agent.ROLLOUT_DEPTH)      //rollout end condition.
            return true;

        if(rollerState.isGameOver())               //end of game
            return true;

        return false;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }


    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                if (children[i].nVisits + m_rnd.nextDouble() * epsilon > bestValue) {
                    bestValue = children[i].nVisits;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    
    
    public int mostVisitedAndBestAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits + children[i].totValue;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                if (children[i].nVisits + children[i].totValue + m_rnd.nextDouble() * epsilon > bestValue) {
                    bestValue = children[i].nVisits + children[i].totValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }
    
    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null && children[i].totValue + m_rnd.nextDouble() * epsilon > bestValue) {
                bestValue = children[i].totValue;
                selected = i;
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    // if final terminal node return true;
    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }
}
