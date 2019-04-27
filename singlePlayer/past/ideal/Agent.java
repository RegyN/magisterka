package tracks.singlePlayer.past.ideal;

/*
 * This is our entry to The General Video Game AI Competition - 2014
 *
 *   Written 2014 by Andreas Heinrich
 *   andi@idsia.ch
 *
 * Istituto Dalle Molle di Studi sull'Intelligenza Artificiale (IDSIA)
 * Galleria 2
 * 6928 Manno
 * SWITZERLAND
 *
 */

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

enum SpriteResult { WIN, EVIL, PASSABLE, IMPASSABLE, NOTSURE }

public class Agent extends AbstractPlayer {

    private static final int RETEST_INTERVAL = 5;
    private static final int RETEST_LIMIT = 20;
    private static final int ACTION_USE_INTERVAL = 10;
    private static final int TEST_LIMIT = 100;
    private static final int TEST_UNREACHABLE_LIMIT = 15;
    private static final int DESTINATION_LIMIT = 100;

    public static final int INF = 9999;
    public static final boolean print_debug = false;

    protected Random rng;
    protected int block_size;
    private HashSet<Integer> t_NPCs_good, t_NPCs_evil, t_immovables, t_movables,
        t_resources, t_portals, t_avatarsprites, t_passable;
    private HashMap<Integer, Integer> retest;
    private boolean is_grid_game;
    private Observation destination;
    private int dest_counter;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        rng = new Random();
        block_size = so.getBlockSize();
        retest = new HashMap<Integer, Integer>();
        t_NPCs_good = new HashSet<Integer>();
        t_NPCs_evil = new HashSet<Integer>();
        t_immovables = new HashSet<Integer>();
        t_movables = new HashSet<Integer>();
        t_resources = new HashSet<Integer>();
        t_portals = new HashSet<Integer>();
        t_avatarsprites = new HashSet<Integer>();
        t_passable = new HashSet<Integer>();
        ArrayList<Types.ACTIONS> actions = so.getAvailableActions();
        is_grid_game = actions.contains(Types.ACTIONS.ACTION_LEFT) &&
            actions.contains(Types.ACTIONS.ACTION_RIGHT) &&
            actions.contains(Types.ACTIONS.ACTION_UP) &&
            actions.contains(Types.ACTIONS.ACTION_DOWN);
        if (is_grid_game) {
            updateTypes(so);
            chooseDestination(so);
        }
    }

    public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        ArrayList<Types.ACTIONS> actions = so.getAvailableActions();
        if (is_grid_game) {
            updateTypes(so);
            if (actions.contains(Types.ACTIONS.ACTION_USE) && so.getGameTick() % ACTION_USE_INTERVAL == 0)
                return Types.ACTIONS.ACTION_USE;
            if (destination == null) {
                chooseDestination(so);
            }
            if (destination != null) {
                Step st = towardsSingle(so, destination, t_passable);
                dest_counter++;
                if (st.dist == 0 || dest_counter >= DESTINATION_LIMIT) {
                    destination = null;
                }
                if (st.dist < INF)
                    return st.act;
            }
        }
        debug("Nothing to do...");
        Types.ACTIONS a = actions.get(rng.nextInt(actions.size()));
        return a;
    }
    
    private void chooseDestination(StateObservation so) {
        ArrayList<Observation> candidates = new ArrayList<Observation>();
        ArrayList<Double> weights = new ArrayList<Double>();
        //movables?
        for (int t : t_resources) {
            ArrayList<Observation> obs_list = getObservationsFromType(so, t);
            for (Observation obs : obs_list) {
                candidates.add(obs);
                weights.add(5.0 / t_resources.size() / obs_list.size());
            }
        }
        for (int t : t_NPCs_good) {
            ArrayList<Observation> obs_list = getObservationsFromType(so, t);
            for (int i=0; i<obs_list.size(); i++) {
                candidates.add(obs_list.get(i));
                weights.add(2.0 * (1.8 - i / obs_list.size() * 1.6) / t_NPCs_good.size() / obs_list.size());
            }
        }
        for (int t : t_immovables) {
            if (t_passable.contains(t)) {
                ArrayList<Observation> obs_list = getObservationsFromType(so, t);
                for (Observation obs : obs_list) {
                    candidates.add(obs);
                    weights.add(1.0 / obs_list.size());
                }
            }
        }
        for (int t : t_movables) {
            ArrayList<Observation> obs_list = getObservationsFromType(so, t);
            for (Observation obs : obs_list) {
                candidates.add(obs);
                weights.add(1.0 / t_movables.size() / obs_list.size());
            }
        }
        for (int t : t_portals) {
            ArrayList<Observation> obs_list = getObservationsFromType(so, t);
            for (Observation obs : obs_list) {
                candidates.add(obs);
                weights.add(4.0 / t_portals.size() / obs_list.size());
            }
        }
        if (!candidates.isEmpty()) {
            Double weightsum = 0.0;
            for (int i=0; i<weights.size(); i++) {
                weightsum += weights.get(i);
            }
            Double random = rng.nextDouble() * weightsum;
            weightsum = 0.0;
            int i;
            for (i=0; i<weights.size(); i++) {
                weightsum += weights.get(i);
                if (random < weightsum) break;
            }
            destination = candidates.get(i);
        } else {
            destination = null;
        }
        dest_counter = 0;
    }

    private void updateTypes(StateObservation so) {
        HashSet<Integer> old_good = new HashSet<Integer>(t_NPCs_good);
        HashSet<Integer> old_immovables = new HashSet<Integer>(t_immovables);
        getTypes(t_NPCs_good, so.getNPCPositions());
        getTypes(t_immovables, so.getImmovablePositions());
        getTypes(t_movables, so.getMovablePositions());
        getTypes(t_resources, so.getResourcesPositions());
        getTypes(t_portals, so.getPortalsPositions());
        for (Iterator<Integer> itr = t_NPCs_good.iterator(); itr.hasNext();) {
            int t = itr.next();
            if (t_NPCs_evil.contains(t))
                itr.remove();
            else if (old_good.contains(t))
                continue;
            else {
                if (retest.containsKey(t)) {
                    Integer rc = (Integer) retest.get(t);
                    rc++;
                    retest.put(t, rc);
                    if (rc > RETEST_LIMIT) {
                        debug("Retest limit reached for NPC type " + t + ", assuming to be good");
                        retest.remove(t);
                        continue;
                    } else if (rc % RETEST_INTERVAL != 0) {
                        itr.remove();
                        continue;
                    }
                }
                SpriteResult r = testSpriteType(so, t, 2);
                if (r == SpriteResult.EVIL) {
                    debug("New evil NPC type: " + t);
                    itr.remove();
                    t_NPCs_evil.add(t);
                    retest.remove(t);
                } else if (r == SpriteResult.NOTSURE) {
                    debug("scheduling NPC type " + t + " for re-test");
                    itr.remove();
                    if (!retest.containsKey(t)) retest.put(t, 0);
                } else {
                    debug("New good NPC type: " + t);
                    retest.remove(t);
                    //debug(r);
                }
            }
        }
        for (int t : t_NPCs_good) t_passable.add(t);
        for (int t : t_immovables)
            if (!old_immovables.contains(t)) {
                SpriteResult r = testSpriteType(so, t);
                if (r == SpriteResult.WIN || r == SpriteResult.PASSABLE) {
                    debug("New passable immovable type: " + t);
                    t_passable.add(t);
                } else debug("New impassable immovable type: " + t);
            }
        for (int t : t_resources) t_passable.add(t);
        for (int t : t_portals) t_passable.add(t);
    }

    private void getTypes(HashSet<Integer> types, ArrayList<Observation>[] obs_array) {
        if (obs_array != null)
            for (ArrayList<Observation> obs : obs_array)
                if (!obs.isEmpty()) types.add(obs.get(0).itype);
    }

    private SpriteResult testSpriteType(StateObservation so, int itype) {
        return testSpriteType(so, itype, 1);
    }
    
    private SpriteResult testSpriteType(StateObservation so, int itype, int num_checks) {
        if (so.isGameOver()) {
            debug("Game is already over. Something's fishy...");
            return SpriteResult.NOTSURE;
        }
        StateObservation localso = so.copy();
        debug("Checking type " + itype + " from position " + gridAvatarPos(localso) + ", num_checks=" + num_checks);
        ArrayList<Observation> targets = new ArrayList<Observation>();
        ArrayList<Observation> observations = getObservationsFromType(so, itype);
        if (observations.size() < num_checks) {
            debug("Warning: num_checks=" + num_checks + ", but only " + observations.size() + " sprites of type " + itype + " found.");
            num_checks = observations.size();
        }
        for (int i=0; i<num_checks; i++) {
            int random = rng.nextInt(observations.size());
            targets.add(observations.get(random));
            if (observations.size() > 1)
                observations.remove(random);
        }
        int unreachable_counter = 0;
        for (int step_counter = 1; step_counter < TEST_LIMIT && !targets.isEmpty(); step_counter++) {
            //Step st = towardsSingle(localso, targets.get(0));
            Step st = towardsSingle(localso, targets.get(0), t_passable);
            if (st.dist == INF) {
                if (++unreachable_counter >= TEST_UNREACHABLE_LIMIT) {
                    debug("Not reachable after " + step_counter + " steps");
                    //return SpriteResult.NOTSURE;
                    targets.remove(0);
                    unreachable_counter = 0;
                }
                continue;
            } else if (st.dist == 0) {
                debug("Already standing on it, seems passable.");
                if (targets.size() == 1)
                    return SpriteResult.PASSABLE;
                targets.remove(0);
                continue;
            }
            //debug(st.dist);
            //debug(st.act);
            IntVector2d ap = gridAvatarPos(localso);
            localso.advance(st.act);
            if (ap.equals(gridAvatarPos(localso))) {
                debug("Seems impassable at position " + gridAvatarPos(localso));
                return SpriteResult.IMPASSABLE;
            }
            if (localso.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                debug("Seems to lose game.");
                return SpriteResult.EVIL;
            } else if (localso.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                debug("Seems to win game.");
                return SpriteResult.WIN;
            }
            if (st.dist == 1) {
                debug("Reached at position " + gridAvatarPos(localso) + " after " + step_counter + " moves.");
                for (int i=0; i<5; i++) localso.advance(Types.ACTIONS.ACTION_NIL); // rest a bit
                if (localso.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                    debug("Seems to lose game.");
                    return SpriteResult.EVIL;
                } else if (localso.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                    debug("Seems to win game.");
                    return SpriteResult.WIN;
                } else {
                    debug("Seems passable.");
                    if (targets.size() == 1)
                        return SpriteResult.PASSABLE;
                    targets.remove(0);
                    continue;
                }
            }
            /*for(Observation obs : localso.getObservationGrid()[ap.x][ap.y]) {
                debug("Stepped on type " + obs.itype + " at " + ap);
                if (obs.itype == itype) {
                }
            }
            if (localso.isGameOver()) {
                debug("Game over while checking.");
                break;
            }*/
        }
        return SpriteResult.NOTSURE;
    }
    
    private ArrayList<Observation> getObservationsFromType(StateObservation so, int itype) {
        for (int i=0; i<5; i++) {
            ArrayList<Observation>[] list = null;
            switch(i) {
                case 0: list = so.getNPCPositions(so.getAvatarPosition()); break;
                case 1: list = so.getImmovablePositions(so.getAvatarPosition()); break;
                case 2: list = so.getMovablePositions(so.getAvatarPosition()); break;
                case 3: list = so.getResourcesPositions(so.getAvatarPosition()); break;
                case 4: list = so.getPortalsPositions(so.getAvatarPosition()); break;
            }
            if (list == null) continue;
            for (ArrayList<Observation> obs_list : list) {
                if (!obs_list.isEmpty() && obs_list.get(0).itype==itype) {
                    return new ArrayList<Observation>(obs_list);
                }
            }
        }
        return new ArrayList<Observation>();
    }

    private Step towardsClosest(StateObservation so, int itype) {
        return towardsClosest(so, itype, new HashSet<Integer>());
    }
    
    private Step towardsClosest(StateObservation so, int itype,
            HashSet<Integer> allowedTypes) {
        ArrayList<Observation>[][] grid = so.getObservationGrid();
        ArrayList<IntVector2d> targetList = new ArrayList<IntVector2d>();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                for (Observation obs : grid[x][y]) {
                    if (obs.itype == itype) {
                        targetList.add(new IntVector2d(x, y));
                        break;
                    }
                }
            }
        }
        IntVector2d gridpos = gridAvatarPos(so);
        HashSet<Integer> allowedCopy = new HashSet<Integer>(allowedTypes);
        allowedCopy.add(itype);
        ArrayList<IntVector2d> path = findPath(so, gridpos, targetList, allowedCopy);
        return new Step(path, gridpos);
    }
    
    private Step towardsSingle(StateObservation so, Observation obs) {
        return towardsSingle(so, obs, new HashSet<Integer>());
    }
    
    private Step towardsSingle(StateObservation so, Observation obs,
            HashSet<Integer> allowedTypes) {
        ArrayList<IntVector2d> targetList = new ArrayList<IntVector2d>();
        targetList.add(gridPos(obs.position.x, obs.position.y));
        //debug(targetList.get(0));
        IntVector2d gridpos = gridAvatarPos(so);
        HashSet<Integer> allowedCopy = new HashSet<Integer>(allowedTypes);
        allowedCopy.add(obs.itype);
        ArrayList<IntVector2d> path = findPath(so, gridpos, targetList, allowedCopy);
        return new Step(path, gridpos);
    }

    private ArrayList<IntVector2d> findPath(StateObservation so, IntVector2d start,
            ArrayList<IntVector2d> destList, HashSet<Integer> allowedTypes) {
        ArrayList<Observation>[][] grid = so.getObservationGrid();
        int w = grid.length;
        int h = grid[0].length;
        int[][] dist = new int[w][h];
        IntVector2d[][] prev = new IntVector2d[w][h];
        boolean[][] visited = new boolean[w][h];
        for (int[] row : dist) Arrays.fill(row, INF);
        IntVector2d empty = new IntVector2d(-1, -1);
        for (IntVector2d[] row : prev) Arrays.fill(row, empty);
        dist[start.x][start.y] = 0;
        while (!areAllTrue(visited)) {
            IntVector2d current = findMin(dist, visited, INF - 1);
            if (current.equals(-1, -1)) break;
            if (isInList(current, destList)) {
                ArrayList<IntVector2d> path = new ArrayList<IntVector2d>();
                while (!prev[current.x][current.y].equals(-1, -1)) {
                    //debug(current);
                    path.add(0, current.copy());
                    current = prev[current.x][current.y];
                }
                return path;
            }
            visited[current.x][current.y] = true;
            for (Vector2d dir : Types.BASEDIRS) {
                IntVector2d next = current.copy().add(dir);
                if (next.x < 0 || next.x >= w || next.y < 0 || next.y >= h)
                    continue;
                if (!grid[next.x][next.y].isEmpty() &&
                        !allowedTypes.contains(grid[next.x][next.y].get(0).itype))
                    continue;
                if (dist[next.x][next.y] > dist[current.x][current.y] + 1) {
                    dist[next.x][next.y] = dist[current.x][current.y] + 1;
                    prev[next.x][next.y] = current.copy();
                }
            }
        }
        return null;
    }

    private IntVector2d gridAvatarPos(StateObservation so) {
        Vector2d pos = so.getAvatarPosition();
        return gridPos(pos.x, pos.y);
    }
    
    private IntVector2d gridPos(double x, double y) {
        return new IntVector2d((int) (x / block_size), (int) (y / block_size));
    }

    private static boolean areAllTrue(boolean[][] array) {
        for(boolean[] bb : array)
            for(boolean b : bb)
                if(!b) return false;
        return true;
    }

    private static IntVector2d findMin(int[][] array, boolean[][] mask, int maxval) {
        int minval = maxval;
        IntVector2d min = new IntVector2d(-1, -1);
        for(int x = 0; x < array.length; x++) {
            for(int y = 0; y < array[0].length; y++) {
                if(!mask[x][y] && array[x][y] < minval) {
                    minval = array[x][y];
                    min.x = x;
                    min.y = y;
                }
            }
        }
        return min;
    }

    private boolean isInList(IntVector2d v, ArrayList<IntVector2d> list) {
        for (IntVector2d item : list) {
            if (item.equals(v)) return true;
        }
        return false;
    }

    private void debug(Object msg) {
        if (print_debug) System.out.println(msg);
    }
}

class Step {
    public final Types.ACTIONS act;
    public final int dist;

    public Step(ArrayList<IntVector2d> path, IntVector2d currentPos) {
        if (path == null) {
            act = Types.ACTIONS.ACTION_NIL;
            dist = Agent.INF;
        } else if (path.size() == 0) {
            act = Types.ACTIONS.ACTION_NIL;
            dist = 0;
        } else {
            act = path.get(0).subtract(currentPos).toAction();
            dist = path.size();
        }
    }
}

class IntVector2d {
    public int x;
    public int y;
    public IntVector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public IntVector2d(Vector2d v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
    }
    public IntVector2d copy() {
        return new IntVector2d(x,y);
    }
    public IntVector2d add(IntVector2d v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }
    public IntVector2d add(Vector2d v) {
        this.x += (int) v.x;
        this.y += (int) v.y;
        return this;
    }
    public IntVector2d subtract(IntVector2d v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }
    public IntVector2d subtract(Vector2d v) {
        this.x -= (int) v.x;
        this.y -= (int) v.y;
        return this;
    }
    public String toString() {
        return x + " : " + y;
    }
    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }
    public boolean equals(Object o) {
        if (o instanceof IntVector2d) {
            IntVector2d v = (IntVector2d) o;
            return x == v.x && y == v.y;
        } else if (o instanceof Vector2d) {
            Vector2d v = (Vector2d) o;
            return x == ((int) v.x) && y == ((int) v.y);
        } else {
            return false;
        }
    }
    public Types.ACTIONS toAction() {
        if (equals(Types.UP)) return Types.ACTIONS.ACTION_UP;
        else if (equals(Types.DOWN)) return Types.ACTIONS.ACTION_DOWN;
        else if (equals(Types.LEFT)) return Types.ACTIONS.ACTION_LEFT;
        else if (equals(Types.RIGHT)) return Types.ACTIONS.ACTION_RIGHT;
        else return Types.ACTIONS.ACTION_NIL;
    }
}
