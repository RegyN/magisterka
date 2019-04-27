package tracks.singlePlayer.past.Shmokin;

import java.util.ArrayList;

public class Node {
	/** The x coordinate at the given step */
	private int x;
	/** The y coordinate at the given step */
	private int y;
	
	private float cost;
	
	private int depth;
	
	private Node parent;
	
	private boolean moveable;
	
	private int category;
	
	private int itype;
	
	private int id;
	
	private boolean explored;
	
	public enum Explore{
		avoid, collect, use, silent
	}
	
	private Explore explore;
	
	public Node(){
		neighborList = new ArrayList<Node>();
		this.moveable = true;
	}
	
	ArrayList<Node> neighborList;
	private Node up;
	private Node down;
	private Node left;
	private Node right;
	
	private float distanceFromStart;
	private int distanceFromGoal;
	
	float heuristicDistanceFromGoal;
	
	/**
	 * Create a new step
	 * 
	 * @param x The x coordinate of the new step
	 * @param y The y coordinate of the new step
	 */
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
		neighborList = new ArrayList<Node>();
		this.moveable = true;
	}
	
	public Node(int x, int y, int category, int itype, int id, int distance){
		this.x = x;
		this.y = y;
		neighborList = new ArrayList<Node>();
		this.moveable = true;
		this.category = category;
		this.itype = itype;
		this.id = id;
		this.explored = false;
		this.distanceFromGoal = distance;
	}
	
	/**
	 * Get the x coordinate of the new step
	 * 
	 * @return The x coodindate of the new step
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the y coordinate of the new step
	 * 
	 * @return The y coodindate of the new step
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return x*y;
	}
	
	public Node getParent(){
		return parent;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public float getCost(){
		return cost;
	}

	
    public ArrayList<Node> getNeighborList() {
        return neighborList;
    }
    
    public boolean getMoveable(){
    	return moveable;
    }
    public void setMoveable(boolean moveable){
    	this.moveable = moveable;
    }
    
	public float getHeuristicDistanceFromGoal() {
		return heuristicDistanceFromGoal;
	}

	public void setHeuristicDistanceFromGoal(float heuristicDistanceFromGoal) {
		this.heuristicDistanceFromGoal = heuristicDistanceFromGoal;
	}


	public int getCategory(){
		return this.category;
	}
	
	public void setCategory(int category){
		this.category = category;
	}
	
	public int getItype(){
		return this.itype;
	}
	
	public void setItype(int itype){
		this.itype = itype;
	}
	
	public int getId(){
		return this.id;
	}
	
	public boolean getExplored(){
		return this.explored;
	}
	
	public void setExplored(boolean explored){
		this.explored = explored;
	}
	
	public Explore getExploredType(){
		return this.explore;
	}
	
	public void setExploredType(Explore explore){
		this.explore = explore;
	}
	
    public void setDistanceFromStart(float f) {
        this.distanceFromStart = f;
    }
	
	public void setParent(Node parent){
		this.parent = parent;
	}
	
	public void setPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
    public void setDown(Node down) {
        //replace the old Node with the new one in the neighborList
        if (neighborList.contains(this.down))
                neighborList.remove(this.down);
        neighborList.add(down);
        
        //set the new Node
        this.down = down;
}
    
    public void setUp(Node up) {
        //replace the old Node with the new one in the neighborList
        if (neighborList.contains(this.up))
                neighborList.remove(this.up);
        neighborList.add(up);
        
        //set the new Node
        this.up = up;
}
    
    public void setLeft(Node left) {
        //replace the old Node with the new one in the neighborList
        if (neighborList.contains(this.left))
                neighborList.remove(this.left);
        neighborList.add(left);
        
        //set the new Node
        this.left = left;
}
    
    public void setRight(Node right) {
        //replace the old Node with the new one in the neighborList
        if (neighborList.contains(this.right))
                neighborList.remove(this.right);
        neighborList.add(right);
        
        //set the new Node
        this.right = right;
}

	public float getDistanceFromStart() {
		return distanceFromStart;
	}
	
	public int distanceFromGoal(){
		return this.distanceFromGoal;
	}
	
	public void distanceFromGoal(int distanceFromGoal){
		this.distanceFromGoal = distanceFromGoal;
	}
	
}
