package tracks.singlePlayer.past.Normal_MCTS.Mapping.PathFinder;

public class AStar
{

	Node	OpenNode, ClosedNode;
	

	int		iMap[];
	int sizeX; int sizeY;
	

	static final int LIMIT_LOOP = 20;
	
	
	
	
	

	AStar(int iM[],int _sizeX, int _sizeY)
	{

		iMap = iM;
		sizeX = _sizeX;
		sizeY = _sizeY;
		
		OpenNode = null;
		ClosedNode = null;
	}



	void ResetPath()
	{
		Node tmp;
		
		
		
		while( OpenNode != null )
		{
			tmp = OpenNode.next;
			OpenNode = null;
			OpenNode = tmp;
		}

		while( ClosedNode != null )
		{
			tmp = ClosedNode.next;
			ClosedNode = null;
			ClosedNode = tmp;
		}
	}


	Node FindPath(int sx, int sy, int tx, int ty)
	{
		Node	src, best = null;
		int		count = 0;
		
		src = new Node();
		src.g = 0;
		src.h = (tx-sx)*(tx-sx) + (ty-sy)*(ty-sy);
		src.f = src.h;
		src.x = sx;
		src.y = sy;
		
		OpenNode = src;
		
		
		while( count < LIMIT_LOOP )
		{
			if( OpenNode == null )
			{
				return best;
			}
			
			
			best = OpenNode;
			OpenNode = best.next;
			
			best.next = ClosedNode;
			ClosedNode = best;
			
			
			if( best == null )
			{
				return null;
			}
			
			if( best.x == tx && best.y == ty )
			{
				return best;
			}
			
			
			if( MakeChild(best, tx, ty) == 0 && count == 0 )
			{
				return null;
			}
			
						
			
			count++;
		}
		
		
		
		return best;
	}

	
	char MakeChild(Node node, int tx, int ty)
	{
		int		x, y;
		char	flag = 0;
		char	cc[] = {0, 0, 0, 0, 0, 0, 0, 0};
		
		
		
		x = node.x;
		y = node.y;
		
		cc[0] = IsMove(x  , y+1);
		cc[1] = IsMove(x-1, y+1);
		cc[2] = IsMove(x-1, y  );
		cc[3] = IsMove(x-1, y-1);
		cc[4] = IsMove(x  , y-1);
		cc[5] = IsMove(x+1, y-1);
		cc[6] = IsMove(x+1, y  );
		cc[7] = IsMove(x+1, y+1);
		
		
		
		if( cc[2] == 1 )
		{
			MakeChildSub(node, x-1, y, tx, ty);
			flag = 1;
		}
		
		if( cc[6] == 1 )
		{
			MakeChildSub(node, x+1, y, tx, ty);
			flag = 1;
		}
		
		if( cc[4] == 1 )
		{
			MakeChildSub(node, x, y-1, tx, ty);
			flag = 1;
		}
		
		if( cc[0] == 1 )
		{
			MakeChildSub(node, x, y+1, tx, ty);
			flag = 1;
		}
		
		if( cc[7] == 1 && cc[6] == 1 && cc[0] == 1 )
		{
			MakeChildSub(node, x+1, y+1, tx, ty);
			flag = 1;
		}
		
		if( cc[3] == 1 && cc[2] == 1 && cc[4] == 1 )
		{
			MakeChildSub(node, x-1, y-1, tx, ty);
			flag = 1;
		}

		if( cc[5] == 1 && cc[4] == 1 && cc[6] == 1 )
		{
			MakeChildSub(node, x+1, y-1, tx, ty);
			flag = 1;
		}

		if( cc[1] == 1 && cc[0] == 1 && cc[2] == 1 )
		{
			MakeChildSub(node, x-1, y+1, tx, ty);
			flag = 1;
		}


		
		return flag;
	}





	char IsMove(int x, int y)
	{
		if( x < 0 || x > sizeX || y < 0 || y > sizeY || (iMap[y*sizeY + x] == 1) )
		{
			return 0;
		}
		
		
		
		return 1;
	}




	void MakeChildSub(Node node, int x, int y, int tx, int ty)
	{
		Node	old = null, child = null;
		int		i;
		int		g = node.g + 1;
		
		
		if( (old = IsOpen(x, y)) != null )
		{
			for( i = 0; i < 8; i++ )
			{
				if( node.direct[i] == null )
				{
					node.direct[i] = old;
					break;
				}
			}
			
			if( g < old.g )
			{
				old.prev = node;
				old.g = g;
				old.f = old.h + old.g;
			}
		}
		
		
		
		else if( (old = IsClosed(x, y)) != null )
		{
			for( i = 0; i < 8; i++ )
			{
				if( node.direct[i] == null )
				{
					node.direct[i] = old;
					break;
				}
			}
			
			if( g < old.g )
			{
				old.prev = node;
				old.g = g;
				old.f = old.h + old.g;
				

			}
		}
		
		
		
		else
		{
			child = new Node();
			
			child.prev = node;
			child.g = g;
			child.h = (x-tx)*(x-tx) + (y-ty)*(y-ty);
			child.f = child.h + child.g;
			child.x = x;
			child.y = y;
			
			InsertNode(child);

			for( i = 0; i < 8; i++ )
			{
				if( node.direct[i] == null )
				{
					node.direct[i] = child;
					break;
				}
			}
		}
	}





	Node IsOpen(int x, int y)
	{
		Node tmp = OpenNode;
		
		
		
		while( tmp != null )
		{
			if( tmp.x == x && tmp.y == y )
			{
				return tmp;
			}
			
			tmp = tmp.next;
		}
		
		
		
		return null;
	}




	Node IsClosed(int x, int y)
	{
		Node tmp = ClosedNode;
		
		
		
		while( tmp != null )
		{
			if( tmp.x == x && tmp.y == y )
			{
				return tmp;
			}
			
			tmp = tmp.next;
		}
		
		
		
		return null;
	}



	void InsertNode(Node src)
	{
		Node old = null, tmp = null;
		
		
		
		if( OpenNode == null )
		{
			OpenNode = src;
			return;
		}
		
		
		
		tmp = OpenNode;
		while( tmp != null && (tmp.f < src.f) )
		{
			old = tmp;
			tmp = tmp.next;
		}
		
		if( old != null )
		{
			src.next = tmp;
			old.next = src;
		}
		else
		{
			src.next = tmp;
			OpenNode = src;
		}
	}
}