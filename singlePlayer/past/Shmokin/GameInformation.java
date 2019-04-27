package tracks.singlePlayer.past.Shmokin;

import java.util.ArrayList;


public class GameInformation {

	private int modeSwitchDistance = 3;
	
	private boolean goalExists = true;
	
	//Number of milliseconds to wait in monte carlo mode
	private long waitTime = 3000;
	
	private long waitTimePush = 500;
	
	private long timeWaited = 0;
	
	private double oldScore = 0;
	
	private int pathLength = 4;
	
	private int pathCounter = 0;
	
	public boolean reset = false;
	
	private boolean goalFound = false;
	
	private long timeLeft;
	
	private int preferredSpriteCategory = 0;
	
	Mode mode;
	private ArrayList<Node> spritesToAvoid = new ArrayList<Node>();
	//This gives the sprites additional categories of how the bot should react to them

	
	public enum Mode{
		AStar, MonteCarlo
	}
	
	
	public ArrayList<Node> getSpritesToAvoid()
	{
		return this.spritesToAvoid;
	}
	
	public void setMode(Mode mode)
	{
		this.mode = mode;
	}
	
	public int getModeSwitchDistance()
	{
		return this.modeSwitchDistance;
	}


	public void setGoalExists(boolean goalExists)
	{
		this.goalExists = goalExists;
	}
	
	public boolean getGoalExists()
	{
		return goalExists;
	}
	
	public long getWaitTime(){
		return this.waitTime;
	}
	
	public long getTimeWaited(){
		return this.timeWaited;
	}
	
	public void setTimeWaited(long timeWaited){
		this.timeWaited = timeWaited;
	}
	
	public double getOldScore(){
		return this.oldScore;
	}
	
	public void setOldScore(double oldScore){
		this.oldScore = oldScore;
	}
	
	public int getPathLength(){
		return this.pathLength;
	}
	
	public void setPathCounter(int pathCounter){
		this.pathCounter = pathCounter;
	}
	
	public int getPathCounter(){
		return this.pathCounter;
	}
	
	public boolean getGoalFound()
	{
		return this.goalFound;
	}
	
	public void setGoalFound(boolean goalFound)
	{
		this.goalFound = goalFound;
	}
	
	public long getTimeLeft(){
		return this.timeLeft;
	}
	
	public void setTimeLeft(long timeLeft){
		this.timeLeft = timeLeft;
	}
	
	public long getWaitTimePush(){
		return waitTimePush;
	}
	
	public void setPreferredSpriteCategory(int preferredSpriteCategory){
		this.preferredSpriteCategory = preferredSpriteCategory;
	}
	
	public int getPreferredSpriteCategory(){
		return this.preferredSpriteCategory;
	}
}
