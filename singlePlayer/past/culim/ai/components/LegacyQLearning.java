package tracks.singlePlayer.past.culim.ai.components;

import java.io.Serializable;
import java.util.HashMap;

import ontology.Types.WINNER;
import core.game.StateObservation;

public class LegacyQLearning
{
	public HashMap<QStateActionPair, Float> qRewardMap;
	public float discount;
	public float stepSize;
	
	public QState prevState;
	
	public LegacyQLearning()
	{
		qRewardMap = new HashMap<QStateActionPair,Float>();
		discount = 0.9f;
		stepSize = 0.2f;
	}
	
	public void init(QState state)
	{
		prevState = state;
	}
	
	public void step()
	{
		// 1. Select an action a.
		// 2. Act out the action a.
		// 3. Calculate reward r.
		// 4. Get next state s'
		// 5. Update hash-table
	}
	
	public static class QState extends Object implements Serializable
	{
		public float n;
		public StateObservation stateObservation;
		
		public QState()
		{
			
		}

		public QState(float n)
		{
			this.n = n;
		}
		
		public QState(StateObservation stateObservation)
		{
			this.stateObservation = stateObservation;
		}
		
		
		public QState act(QAction prevAction)
		{
			return new QState();
			
		}
		
		public float reward()
		{
			if (!stateObservation.isGameOver())
			{
				return (float) stateObservation.getGameScore();
			}
			else 
			{
				if (stateObservation.getGameWinner() == WINNER.PLAYER_WINS)
				{
					return 10000;
				}
				else 
				{
					return -10000;
				}
			}
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof QState)
			{
				QState qstate = (QState)(obj);
				return this.n == qstate.n;
			}
			
			return false;
		}
		

		
	}

	public static class QAction extends Object implements Serializable
	{
		public int m;
		
		public QAction(int m)
		{
			this.m = m;
		}
		
		public boolean equals(Object obj)
		{

			if (obj instanceof QAction)
			{
				QAction qAction = (QAction)(obj);
				return this.m == qAction.m;
			}
			
			return false;
		}
	}

	public static class QStateActionPair extends Object implements Serializable
	{
		public QState state;
		public QAction action;
		
		public QStateActionPair(QState state, QAction action)
		{
			this.state = state;
			this.action = action;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof QStateActionPair)
			{
				QStateActionPair qPair = (QStateActionPair)(obj);
				return this.state.equals(qPair.state) && this.action.equals(qPair.action);
			}
			
			return false;
		}
		
		@Override 
		public int hashCode()
		{
			return 0;
		}
	}

}


