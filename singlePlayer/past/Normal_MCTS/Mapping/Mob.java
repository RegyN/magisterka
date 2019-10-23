package tracks.singlePlayer.past.Normal_MCTS.Mapping;

import tools.Vector2d;

public class Mob {
	private int number;
	private int category;
	private int type;
	private boolean isObstcale;
	
	public Mob(int _number,int _category, int _type,boolean _isObstcale)
    {
        number = _number; 
        category = _category;
        type = _type;
        isObstcale = _isObstcale;
    }
	
	public void setMob(int _number,int _category, int _type, boolean _isObstcale)
    {
        number = _number; 
        category = _category;
        type = _type;
        isObstcale = _isObstcale;
    }
	
	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}


	public int getCategory() {
		return category;
	}


	public void setCategory(int category) {
		this.category = category;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}
	
	public boolean isObstcale() {
		return isObstcale;
	}

	public void setObstcale(boolean isObstcale) {
		this.isObstcale = isObstcale;
	}


	

}
