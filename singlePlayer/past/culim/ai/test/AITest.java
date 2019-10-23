package tracks.singlePlayer.past.culim.ai.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import tracks.singlePlayer.past.culim.ai.AIUtils;
import tracks.singlePlayer.past.culim.ai.components.LegacyQLearning;
import tracks.singlePlayer.past.culim.ai.components.LegacyQLearning.QAction;
import tracks.singlePlayer.past.culim.ai.components.LegacyQLearning.QState;
import tracks.singlePlayer.past.culim.ai.components.LegacyQLearning.QStateActionPair;

public class AITest
{
	public AITest()
	{
		LegacyQLearning qLearning = new LegacyQLearning();
		
		QState state1 = new QState(5.0f);
		QAction action1 = new QAction(2);
		QStateActionPair pair1 = new QStateActionPair(state1, action1);
		
		QState state2 = new QState(5.0f);
		QAction action2 = new QAction(2);
		QStateActionPair pair2 = new QStateActionPair(state2, action2);
		
		QState state3 = new QState(15.0f);
		QAction action3 = new QAction(2);
		QStateActionPair pair3 = new QStateActionPair(state3, action3);
		
		qLearning.qRewardMap.put(pair1, 10.0f);
		qLearning.qRewardMap.put(pair2, 15.0f);
		qLearning.qRewardMap.put(pair3, 1.0f);
		
		AIUtils.write(state1, "ser/state1.ser");
		QState state4 = AIUtils.read("ser/state1.ser");
		
		AIUtils.write(action1, "ser/action1.ser");
		QAction action4 = AIUtils.read("ser/action1.ser");
		
		AIUtils.write(pair1, "ser/pair1.ser");
		QStateActionPair pair4 = AIUtils.read("ser/pair1.ser");
		
		AIUtils.write(qLearning.qRewardMap, "ser/qRewardMap.ser");
		HashMap<QStateActionPair, Float> qRewardMap1 = AIUtils.read("ser/qRewardMap.ser");
		
		System.out.println(String.format("Test1: state1.equals(state2) | %s", boolString(state1.equals(state2))));
		System.out.println(String.format("Test2: !state1.equals(state3) | %s", boolString(!state1.equals(state3))));
		System.out.println(String.format("Test3: !state2.equals(state3) | %s", boolString(!state2.equals(state3))));
		System.out.println(String.format("Test4: pair1.equals(pair2) | %s", boolString(pair1.equals(pair2))));
		System.out.println(String.format("Test5: qLearning.qRewardMap.get(pair3) == 1.0f) | %s", boolString(qLearning.qRewardMap.get(pair3) == 1.0f)));
		System.out.println(String.format("Test6: qLearning.qRewardMap.get(pair2) == 15.0f) | %s", boolString(qLearning.qRewardMap.get(pair2) == 15.0f)));
		System.out.println(String.format("Test7: qLearning.qRewardMap.get(pair1) == 15.0f) | %s", boolString(qLearning.qRewardMap.get(pair1) == 15.0f)));
		System.out.println(String.format("Test8: state1.equals(state4) | %s", boolString(state1.equals(state4))));
		System.out.println(String.format("Test9: action1.equals(action4) | %s", boolString(action1.equals(action4))));
		System.out.println(String.format("Test10: pair1.equals(pair4) | %s", boolString(pair1.equals(pair4))));
		System.out.println(String.format("Test11: qRewardMap1.get(pair4) == 15.0f | %s", boolString(qLearning.qRewardMap.get(pair4) == 15.0f)));
		
	}
	
	public static String boolString(boolean pass)
	{
		return pass ? "Passed!" : "Failed.";
	}

	public static void main(String[] args)
	{
		new AITest();
	}

}
