package tracks.singlePlayer.past.Shmokin;

import java.util.Comparator;

public class Compare implements Comparator<Integer>{
 
	@Override
	public int compare(Integer o1, Integer o2) {
		return (o1>o2 ? -1 : (o1==o2 ? 0 : 1));
	}
}