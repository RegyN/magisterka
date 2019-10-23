package tracks.singlePlayer.past.Normal_MCTS.GameTable;

public class GameTableRow {
	
	private int ava;
	private int spriteCategory;
	private int spriteItype;
	private int spriteOrder;
	private int spriteNumber;
	private int avatarOutput;
	private int spriteOutput;
	private int result;
	
	public GameTableRow( int _ava,int _spriteCategory, int _spriteItype, int _spriteOrder, 
						 //int _spriteNumber, 
			int _avatarOutput, int _spriteOutput,int _result){
		ava= _ava;
		spriteCategory = _spriteCategory;
		spriteItype = _spriteItype;
		spriteOrder = _spriteOrder;
		//spriteNumber = _spriteNumber;
		avatarOutput = _avatarOutput;
		spriteOutput = _spriteOutput;
		result = _result;
	}
	
	
	
	public int getAva() {
		return ava;
	}
	
	public void setAva(int ava) {
		this.ava = ava;
	}
	
	public int getSpriteCategory() {
		return spriteCategory;
	}
	
	public void setSpriteCategory(int spriteCategory) {
		this.spriteCategory = spriteCategory;
	}
	
	public int getSpriteItype() {
		return spriteItype;
	}
	
	public void setSpriteItype(int spriteItype) {
		this.spriteItype = spriteItype;
	}
	
	public int getSpriteOrder() {
		return spriteOrder;
	}
	
	public void setSpriteOrder(int spriteOrder) {
		this.spriteOrder = spriteOrder;
	}
	
	/*public int getSpriteNumber() {
		return spriteNumber;
	}
	
	public void setSpriteNumber(int spriteNumber) {
		this.spriteNumber = spriteNumber;
	}*/
	
	public int getAvatarOutput() {
		return avatarOutput;
	}
	
	public void setAvatarOutput(int avatarOutput) {
		this.avatarOutput = avatarOutput;
	}
	
	public int getSpriteOutput() {
		return spriteOutput;
	}
	
	public void setSpriteOutput(int spriteOutput) {
		this.spriteOutput = spriteOutput;
	}
	
	public int getResult() {
		return result;
	}
	
	public void setResult(int result) {
		this.result = result;
	}
	
	

	

}
