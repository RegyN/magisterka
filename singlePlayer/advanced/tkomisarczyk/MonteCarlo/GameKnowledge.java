package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import ontology.Types;

enum GameType{
    Planar2D,
    Other
}

public class GameKnowledge {
    GameType Type;
    // Movement actions
    Types.ACTIONS Up = null;
    Types.ACTIONS Down = null;
    Types.ACTIONS Left = null;
    Types.ACTIONS Right = null;
}
