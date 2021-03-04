package nl.hannahsten.texifyidea.algorithm;

import java.util.*;
import java.util.function.Function;

/**
 * A generic implementation of the breadth first search algorithm.
 * <p>
 * If you want to...
 * <ul>
 * <li>Find the shortest path, use {@linkplain BFS#BFS(Object, Object, Function)}.</li>
 * <li>Execute something at every visited node, use
 * {@linkplain BFS#BFS(Object, Function)} in
 * combination with {@linkplain BFS#setIterationAction(Function)}.
 * </li>
 * </ul>
 *
 * @param <N>
 *         The node type.
 * @author Hannah Schellekens
 */
@SuppressWarnings("unchecked")
public class BFS<N> implements PathAlgorithm<N> {

    /**
     * Iteration action that literally does nothing and lets the algorithm continue.
     */
    public static final Function NO_ITERATION_ACTION = n -> BFSAction.CONTINUE;

    /**
     * The node to start branching from.
     */
    private final BFSNode start;

    /**
     * The node where the pathfinding should end.
     * <p>
     * Or {@code null} when the algorithm should branch over all nodes.
     */
    private final N end;

    /**
     * Set containing all the nodes that have been visited by the algorithm.
     */
    private Set<N> visited;

    /**
     * A shortest path from {@link BFS#start} to {@link BFS#end}.
     * <p>
     * {@code null} if no shortest path is found.
     */
    private List<N> shortestPath;

    /**
     * The function that fetches all adjacent nodes of a given node.
     * <p>
     * Should not return itself in the result list.
     */
    private Function<N, List<N>> adjacencyFunction;

    /**
     * The action that will be executed for every node that the algorithm visits.
     * <p>
     * The function returns how the algorithm should continue.
     */
    private Function<N, BFSAction> iterationAction;

    /**
     * Creates a new BFS that will branch until it finds the end node.
     * <p>
     * By default, the algorithm will not fire an action for each visited node (including the
     * starting node). You can alter this by setting an iteration action
     * ({@link BFS#setIterationAction(Function)}).
     *
     * @param startNode
     *         The node where the BFS should start.
     * @param endNode
     *         The node where the BFS should stop.
     * @param adjacencyFunction
     *         Function that fetches all adjacent nodes of a given node with the given node
     *         <em>excluded</em>.
     */
    public BFS(N startNode, N endNode, Function<N, List<N>> adjacencyFunction) {
        this.start = new BFSNode(startNode, 0);
        this.end = endNode;
        this.adjacencyFunction = adjacencyFunction;
        this.iterationAction = NO_ITERATION_ACTION;
    }

    /**
     * Creates a new BFS that will keep on branching.
     * <p>
     * By default, the algorithm will not fire an action for each visited node (including the
     * starting node). You can alter
     * this by setting an iteration action ({@link BFS#setIterationAction(Function)}).
     *
     * @param startNode
     *         The node where the BFS should start.
     * @param adjacencyFunction
     *         Function that fetches all adjacent nodes of a given node with the given node
     *         <em>excluded</em>.
     */
    public BFS(N startNode, Function<N, List<N>> adjacencyFunction) {
        this(startNode, null, adjacencyFunction);
    }

    @Override
    public void execute() {
        // Terminate when the end is also the start node, but only if the algorithm
        // has a goal (aka end-node) in mind.
        if (!hasGoal()) {
            if (start.getNode().equals(end)) {
                return;
            }
        }

        // Queue that contains all the nodes that have to be covered next.
        Deque<BFSNode> toCover = new ArrayDeque<>();

        // Initialise state: mark only the start node as visited.
        visited = new HashSet<>();
        visited.add(start.getNode());
        toCover.add(start);

        // Execute iteration action for the starting node. Abort algorithm if needed.
        BFSAction startAction = iterationAction.apply(start.getNode());
        if (startAction == BFSAction.ABORT) {
            return;
        }

        // Keep on keepin' on until all nodes are covered.
        while (toCover.size() > 0) {
            // The node that is currently being processed.
            BFSNode current = toCover.remove();

            // Process all adjacent nodes.
            List<N> adjacencyList = getAdjacencyList(current);
            for (N adjacentNode : adjacencyList) {
                // Don't process a node that has already been visited.
                if (isVisited(adjacentNode)) {
                    continue;
                }

                // Setup distance & predecessor for discovered node.
                BFSNode newNode = new BFSNode(adjacentNode, current.getDistance() + 1);
                newNode.setPredecessor(current);

                // Execute iteration action for every node visited. Abort algorithm if needed.
                BFSAction action = iterationAction.apply(adjacentNode);
                if (action == BFSAction.ABORT) {
                    return;
                }

                // When looking for a shortest path, terminate when it has been found.
                if (!hasGoal() && newNode.getNode().equals(end)) {
                    shortestPath = constructShortestPath(newNode);
                    return;
                }

                // Make sure the adjacent node gets processed during the next iteration.
                toCover.add(newNode);
                markVisited(newNode);
            }
        }
    }

    /**
     * Set what action has to be executed each time a node gets visited (including the start node).
     *
     * @param iterationAction
     *         The action to execute each node or {@link BFS#NO_ITERATION_ACTION} if you want to
     *         execute nothing. The result of the function (a {@link BFSAction}) determines if the
     *         algorithm should continue or not.
     */
    public void setIterationAction(Function<N, BFSAction> iterationAction) {
        this.iterationAction = iterationAction;
    }

    /**
     * Get the shortest path from the starting node to the ending node.
     * <p>
     * This method requires that the BFS ran in goal-oriented mode. This means that the BFS will
     * actively search for a path ({@link BFS#BFS(Object, Object, Function)} constructor used).
     * <p>
     * Also, {@link BFS#execute()} must have been called, otherwise the algorithm didn't even
     * have a chance of calculating it.
     *
     * @return The shortest path from the start node to the end node.
     * @throws IllegalStateException
     *         When the BFS is not set to find a shortest path, i.e. covers all nodes, or when
     *         {@link BFS#execute()} hasn't been called.
     */
    @Override
    public List<N> getPath() throws IllegalStateException {
        if (shortestPath == null) {
            throw new IllegalStateException("The BFS has no found shortest path.");
        }

        return shortestPath;
    }

    /**
     * Marks that the given node is visited.
     *
     * @param node
     *         The node to mark as visited.
     */
    private void markVisited(BFSNode node) {
        visited.add(node.getNode());
    }

    /**
     * Checks if the given node has already been visited by the algorithm.
     *
     * @param node
     *         The node to check for if it's been visited already.
     * @return {@code true} if the node has been visited, {@code false} when the node hasn't been
     * visited.
     */
    private boolean isVisited(N node) {
        return visited.contains(node);
    }

    /**
     * Get all the adjacent nodes of a given BFSNode.
     *
     * @param node
     *         The node to get all adjacent nodes of.
     * @return A list containing all adjacent nodes relative to {@code node}.
     */
    private List<N> getAdjacencyList(BFSNode node) {
        return adjacencyFunction.apply(node.getNode());
    }

    /**
     * Reconstructs the shortest path from the end node towards the node with distance 0.
     *
     * @param end
     *         The end node from which to backtrack to the starting node.
     * @return The shortest path from start to end nodes.
     */
    private List<N> constructShortestPath(BFSNode end) {
        List<N> shortestPath = new ArrayList<>();
        BFSNode node = end;

        while (node.getDistance() != 0) {
            shortestPath.add(0, node.getNode());
            node = node.getPredecessor();
        }

        return shortestPath;
    }

    /**
     * Checks if the BFS algorithm will visit all nodes.
     * <p>
     * In other words: if the BFS will stop at a target node or not.
     *
     * @return {@code true} if the algorithm stop only when all nodes are covered: the algorithm
     * executes without a goal in mind, {@code false} if the algorithm stops when the target has
     * been found.
     */
    public boolean hasGoal() {
        return end == null;
    }

    /**
     * Class used by the BFS algorithm to keep track of augmented values.
     *
     * @author Hannah Schellekens, Dylan ter Veen
     */
    @SuppressWarnings("unchecked")
    private class BFSNode {

        /**
         * The original version of the node.
         */
        private final N node;

        /**
         * The distance from the starting node to this node.
         */
        private int distance;

        /**
         * The predecessing node from the start node to this one.
         */
        private BFSNode predecessor;

        private BFSNode(N node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        /**
         * Get the original (wrapped) node.
         *
         * @return The wrapped node object.
         */
        private N getNode() {
            return node;
        }

        /**
         * Get the predecessing node from the start node to this one.
         */
        private BFSNode getPredecessor() {
            return predecessor;
        }

        /**
         * Set the predecessing node from the start node to this one.
         *
         * @param predecessor
         *         The new predecessing node.
         */
        private void setPredecessor(BFSNode predecessor) {
            this.predecessor = predecessor;
        }

        /**
         * Get the distance from the starting node to this node.
         */
        private int getDistance() {
            return distance;
        }

        /**
         * Set the distance from the starting node to this node.
         *
         * @param distance
         *         The new distance from the start node to this node.
         */
        private void setDistance(int distance) {
            this.distance = distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!o.getClass().equals(getClass())) {
                return false;
            }

            BFSNode bfsNode = (BFSNode)o;
            return node.equals(bfsNode.node);
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }
    }

    /**
     * Actions that can be executed after an iteration action.
     *
     * @author Hannah Schellekens
     */
    public enum BFSAction {

        CONTINUE,
        ABORT;

        /**
         * @param abort
         *         {@code true} if the BFS must abort, {@code false} if the BFS must continue.
         * @return {@link BFSAction#ABORT} when {@code abort == true} and {@link BFSAction#CONTINUE}
         * when {@code abort == false}.
         */
        public static BFSAction valueOf(boolean abort) {
            return abort ? ABORT : CONTINUE;
        }
    }
}