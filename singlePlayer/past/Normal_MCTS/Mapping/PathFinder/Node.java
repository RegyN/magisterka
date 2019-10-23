package tracks.singlePlayer.past.Normal_MCTS.Mapping.PathFinder;

public class Node
{
	int		f;		
	int		h;		
	int		g;		
	int		x, y;	
	Node	prev;	
	Node	direct[];	
	Node	next;		
	
	
	Node()
	{
		direct = new Node[8];
		
		for( int i = 0; i < 8; i++) direct[i] = null;
	}
}
