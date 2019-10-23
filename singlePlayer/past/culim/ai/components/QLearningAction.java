package tracks.singlePlayer.past.culim.ai.components;

import java.io.Serializable;

import ontology.Types.ACTIONS;

/**
 * The action for a q-learning system for the GVGAI competition.
 * @author culim
 *
 */
public class QLearningAction implements Serializable
{
	public ACTIONS action;
	
	public static final QLearningAction UP = new QLearningAction(ACTIONS.ACTION_UP);
	public static final QLearningAction DOWN = new QLearningAction(ACTIONS.ACTION_DOWN);
	public static final QLearningAction LEFT = new QLearningAction(ACTIONS.ACTION_LEFT);
	public static final QLearningAction RIGHT = new QLearningAction(ACTIONS.ACTION_RIGHT);
	public static final QLearningAction USE = new QLearningAction(ACTIONS.ACTION_USE);
	public static final QLearningAction ESCAPE = new QLearningAction(ACTIONS.ACTION_ESCAPE);
	public static final QLearningAction NIL = new QLearningAction(ACTIONS.ACTION_NIL);
	
	public QLearningAction(ACTIONS action)
	{
		this.action = action;
	}
	
	public ACTIONS value()
	{
		return this.action;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof QLearningAction)
		{
			QLearningAction qAction = (QLearningAction) obj; 
			return this.action == qAction.action;
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return 1;
	}
	
	@Override
	public String toString()
	{
		return action.toString();
	}

	public static QLearningAction fromAction(ACTIONS action)
	{
		if (action == ACTIONS.ACTION_UP)
		{
			return UP;
		}
		else if (action == ACTIONS.ACTION_DOWN)
		{
			return DOWN;
		}
		else if (action == ACTIONS.ACTION_LEFT)
		{
			return LEFT;
		}
		else if (action == ACTIONS.ACTION_RIGHT)
		{
			return RIGHT;
		}
		else if (action == ACTIONS.ACTION_USE)
		{
			return USE;
		}
		else if (action == ACTIONS.ACTION_NIL)
		{
			return NIL;
		}
		else if (action == ACTIONS.ACTION_ESCAPE)
		{
			return ESCAPE;
		}
		
		return NIL;
	}
}
