package drone.dronepathplanning_aco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Ant Colony Optimization (ACO) for a single drone on a static 8-connected grid.
 * The planner minimizes the best-found weighted path cost (not a guarantee of optimality).
 * Only this class differs from the frozen simulator baseline.
 */
public final class DronePathPlanning {
    private final Point start, finish;
    private final boolean success;
    private final double pathCost;
    private final int expandedNodes;
    private final long elapsedNanos;
    private final int steps;
    private final String terminationReason;

    private static final int[][] N8 = {
            {-1,0}, {-1,1}, {0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}
    };
    private static final double DIAGONAL_COST = Math.sqrt(2.0);

    // ACO parameters are deliberately centralized for student experiments.
    private static final int ITERATIONS = 12;
    private static final int ANTS_PER_ITERATION = 10;
    private static final double ALPHA = 1.0;      // pheromone influence
    private static final double BETA = 3.5;       // goal-directed heuristic influence
    private static final double EVAPORATION = 0.25;
    private static final double PHEROMONE_Q = 10.0;
    private static final double INITIAL_PHEROMONE = 1.0;
    private static final double MIN_PHEROMONE = 0.05;
    private static final double MAX_PHEROMONE = 20.0;
    private static final long RANDOM_SEED = 20261030L; // reproducible simulations

    public DronePathPlanning(int[][] environment, Drone drone, Point finish) {
        if (environment == null || environment.length == 0 || environment[0] == null
                || environment[0].length == 0 || drone == null || finish == null)
            throw new IllegalArgumentException("Grid, drone, and finish must be provided");
        this.start = new Point(drone.initialRowPosition, drone.initialColPosition);
        this.finish = finish;
        int rows = environment.length, cols = environment[0].length;
        for (int[] row : environment)
            if (row == null || row.length != cols)
                throw new IllegalArgumentException("Grid must be rectangular");
        if (!free(environment, start.row(), start.col())
                || !free(environment, finish.row(), finish.col()))
            throw new IllegalArgumentException("Start or finish is outside grid or blocked");

        long begin = System.nanoTime();

        // =====================================================================
        // BEGIN REPLACEABLE ALGORITHM: ANT COLONY OPTIMIZATION (ACO)
        // =====================================================================
        // ACO: probabilistic solution construction, pheromone evaporation and
        // reinforcement. Backtracking is used only during construction when an
        // ant reaches a dead end; the final drone route contains no backtracking.
        // Movements: 8-neighborhood; straight=1, diagonal=sqrt(2).
        // Diagonal corner cutting is forbidden.
        // =====================================================================
        double[][] pheromone = new double[rows][cols];
        for (double[] row : pheromone) Arrays.fill(row, INITIAL_PHEROMONE);
        Random random = new Random(RANDOM_SEED);
        ArrayList<Point> bestPath = new ArrayList<>();
        double bestCost = Double.POSITIVE_INFINITY;
        int expanded = 0;

        if (start.equals(finish)) {
            bestPath.add(start);
            bestCost = 0.0;
            expanded = 1;
        } else {
            int[][] visitStamp = new int[rows][cols];
            int stamp = 0;
            // Each ant can examine every free cell at most once. This permits
            // route discovery even in labyrinths with dead ends.
            int maxExplorations = rows * cols;
            for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                ArrayList<ArrayList<Point>> successfulPaths = new ArrayList<>();
                ArrayList<Double> successfulCosts = new ArrayList<>();

                for (int ant = 0; ant < ANTS_PER_ITERATION; ant++) {
                    stamp++;
                    AntResult result = constructAntPath(environment, pheromone,
                            visitStamp, stamp, maxExplorations, random);
                    expanded += result.expanded();
                    if (result.path() == null) continue;
                    ArrayList<Point> candidate = result.path();
                    double candidateCost = calculateCost(candidate);
                    successfulPaths.add(candidate);
                    successfulCosts.add(candidateCost);
                    if (candidateCost + 1e-9 < bestCost) {
                        bestCost = candidateCost;
                        bestPath = new ArrayList<>(candidate);
                    }
                }

                // Pheromone evaporation on all traversable cells.
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        pheromone[r][c] = Math.max(MIN_PHEROMONE,
                                pheromone[r][c] * (1.0 - EVAPORATION));
                    }
                }
                // Deposit pheromone from successful ants, inversely weighted
                // by the achieved path cost; also reinforce the global best.
                for (int k = 0; k < successfulPaths.size(); k++) {
                    deposit(pheromone, successfulPaths.get(k),
                            PHEROMONE_Q / Math.max(1.0, successfulCosts.get(k)));
                }
                if (!bestPath.isEmpty()) {
                    deposit(pheromone, bestPath,
                            PHEROMONE_Q / Math.max(1.0, bestCost));
                }
            }
        }
        boolean reached = !bestPath.isEmpty();
        ArrayList<Point> resultPath = bestPath;
        double cost = reached ? bestCost : Double.NaN;
        // =====================================================================
        // END REPLACEABLE ALGORITHM: ANT COLONY OPTIMIZATION (ACO)
        // =====================================================================

        elapsedNanos = System.nanoTime() - begin;
        success = reached;
        expandedNodes = expanded; // total ant node expansions across all runs
        pathCost = cost;
        terminationReason = reached ? "GOAL_REACHED" : "NO_PATH";
        drone.path = reached ? resultPath : new ArrayList<>(List.of(start));
        drone.frameIndex = 0;
        steps = drone.path.size() - 1;
    }

    /** One ant builds a simple route using probabilistic DFS with dead-end recovery. */
    private AntResult constructAntPath(int[][] environment, double[][] pheromone,
                                       int[][] visited, int stamp, int maxExplorations,
                                       Random random) {
        ArrayList<Point> route = new ArrayList<>();
        route.add(start);
        visited[start.row()][start.col()] = stamp;
        int expanded = 0;
        while (!route.isEmpty() && expanded < maxExplorations) {
            Point here = route.get(route.size() - 1);
            expanded++;
            if (here.equals(finish)) return new AntResult(route, expanded);

            int[] options = new int[8];
            double[] weights = new double[8];
            int count = 0;
            double total = 0.0;
            for (int d = 0; d < N8.length; d++) {
                int dr = N8[d][0], dc = N8[d][1];
                int nr = here.row() + dr, nc = here.col() + dc;
                if (!free(environment, nr, nc) || visited[nr][nc] == stamp) continue;
                if (dr != 0 && dc != 0 &&
                        (!free(environment, here.row(), nc)
                                || !free(environment, nr, here.col()))) continue;
                double stepCost = dr != 0 && dc != 0 ? DIAGONAL_COST : 1.0;
                double remaining = octile(new Point(nr, nc), finish);
                // Exponential goal proximity gives a meaningful direction signal
                // even when the goal is hundreds of cells away.
                double eta = Math.exp(octile(here, finish) - remaining) / stepCost;
                double weight = Math.pow(pheromone[nr][nc], ALPHA)
                        * Math.pow(eta, BETA);
                options[count] = d;
                weights[count] = weight;
                total += weight;
                count++;
            }
            if (count == 0) {
                route.remove(route.size() - 1); // backtrack dead end
                continue;
            }
            double selection = random.nextDouble() * total;
            int chosen = count - 1;
            for (int i = 0; i < count; i++) {
                selection -= weights[i];
                if (selection <= 0) { chosen = i; break; }
            }
            int direction = options[chosen];
            int nr = here.row() + N8[direction][0];
            int nc = here.col() + N8[direction][1];
            visited[nr][nc] = stamp;
            route.add(new Point(nr, nc));
        }
        return new AntResult(null, expanded);
    }

    private static void deposit(double[][] pheromone, List<Point> path, double amount) {
        for (Point point : path) {
            int row = point.row(), col = point.col();
            pheromone[row][col] = Math.min(MAX_PHEROMONE,
                    pheromone[row][col] + amount);
        }
    }

    private static double calculateCost(List<Point> path) {
        double cost = 0.0;
        for (int i = 1; i < path.size(); i++) {
            Point previous = path.get(i - 1), current = path.get(i);
            cost += previous.row() != current.row() && previous.col() != current.col()
                    ? DIAGONAL_COST : 1.0;
        }
        return cost;
    }

    private static boolean free(int[][] env, int row, int col) {
        return row >= 0 && row < env.length && col >= 0
                && col < env[0].length && env[row][col] != -1;
    }

    private static double octile(Point a, Point b) {
        int dr = Math.abs(a.row() - b.row());
        int dc = Math.abs(a.col() - b.col());
        return Math.max(dr, dc) + (DIAGONAL_COST - 1.0) * Math.min(dr, dc);
    }

    private record AntResult(ArrayList<Point> path, int expanded) {}

    public Point getStart() { return start; }
    public Point getFinish() { return finish; }
    public boolean isSuccess() { return success; }
    public double getPathCost() { return pathCost; }
    public int getExpandedNodes() { return expandedNodes; }
    public long getElapsedNanos() { return elapsedNanos; }
    public int getSteps() { return steps; }
    public String getTerminationReason() { return terminationReason; }
}
