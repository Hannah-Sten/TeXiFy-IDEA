package nl.hannahsten.texifyidea.algorithm

import java.util.*

/**
 * A generic implementation of the breadth first search algorithm.
 * Creates a new BFS that will keep on branching.
 *
 * By default, the algorithm will not fire an action for each visited node (including the
 * starting node). You can alter
 * this by setting an iteration action ([BFS.iterationAction]).
 *
 * If you want to...
 *
 *  * Find the shortest path, use [BFS.shortestPath].
 *  * Execute something at every visited node, use
 * [BFS] in
 * combination with [BFS.iterationAction].
 *
 *
 *
 * @param <N>
 * The node type.
 * @param startNode
 * The node where the BFS should start.
 * @param adjacencyFunction
 * Function that fetches all adjacent nodes of a given node with the given node
 * *excluded*.
 *
 * @author Hannah Schellekens
 */
class BFS<N>(startNode: N, private val adjacencyFunction: (N) -> List<N>, endNode: N? = null) : PathAlgorithm<N> {

    /**
     * The node to start branching from.
     */
    private val start: BFSNode = BFSNode(startNode, 0)

    /**
     * The node where the pathfinding should end, or has ended.
     *
     *
     * Or `null` when the algorithm should branch over all nodes.
     */
    var end: N? = endNode

    /**
     * Set containing all the nodes that have been visited by the algorithm.
     */
    private var visited: MutableSet<N>? = null

    /**
     * A shortest path from [BFS.start] to [BFS.end].
     *
     *
     * `null` if no shortest path is found.
     */
    private var shortestPath: List<N>? = null

    /**
     * Iteration action that literally does nothing and lets the algorithm continue.
     */
    @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
    val NO_ITERATION_ACTION: (N) -> BFSAction = { BFSAction.CONTINUE }

    /**
     * The action to execute for each node or [BFS.NO_ITERATION_ACTION] if you want to
     * execute nothing. The result of the function (a [BFSAction]) determines if the
     * algorithm should continue or not.
     */
    var iterationAction: (N) -> BFSAction = NO_ITERATION_ACTION

    override fun execute() {
        // Terminate when the end is also the start node, but only if the algorithm
        // has a goal (aka end-node) in mind.
        if (!hasGoal()) {
            if (start.node == end) {
                return
            }
        }

        // Queue that contains all the nodes that have to be covered next.
        val toCover: Deque<BFSNode> = ArrayDeque()

        // Initialise state: mark only the start node as visited.
        visited = HashSet()
        (visited as HashSet<N>).add(start.node)
        toCover.add(start)

        // Execute iteration action for the starting node. Abort algorithm if needed.
        val startAction = iterationAction(start.node)
        if (startAction == BFSAction.ABORT) {
            end = start.node
            return
        }

        // Keep on keepin' on until all nodes are covered.
        while (toCover.size > 0) {
            // The node that is currently being processed.
            val current = toCover.remove()

            // Process all adjacent nodes.
            val adjacencyList = getAdjacencyList(current)
            for (adjacentNode in adjacencyList) {
                // Don't process a node that has already been visited.
                if (isVisited(adjacentNode)) {
                    continue
                }

                // Setup distance & predecessor for discovered node.
                val newNode = BFSNode(adjacentNode, current.distance + 1)
                newNode.predecessor = current

                // Execute iteration action for every node visited. Abort algorithm if needed.
                val action = iterationAction(adjacentNode)
                if (action == BFSAction.ABORT) {
                    end = adjacentNode
                    return
                }

                // When looking for a shortest path, terminate when it has been found.
                if (!hasGoal() && newNode.node == end) {
                    shortestPath = constructShortestPath(newNode)
                    return
                }

                // Make sure the adjacent node gets processed during the next iteration.
                toCover.add(newNode)
                markVisited(newNode)
            }
        }
    }

    /**
     * Get the shortest path from the starting node to the ending node.
     *
     *
     * This method requires that the BFS ran in goal-oriented mode. This means that the BFS will
     * actively search for a path.
     *
     *
     * Also, [BFS.execute] must have been called, otherwise the algorithm didn't even
     * have a chance of calculating it.
     *
     * @return The shortest path from the start node to the end node.
     * @throws IllegalStateException
     * When the BFS is not set to find a shortest path, i.e. covers all nodes, or when
     * [BFS.execute] hasn't been called.
     */
    override val path: List<N>
        get() {
            checkNotNull(shortestPath) { "The BFS has no found shortest path." }
            return shortestPath as List<N>
        }

    /**
     * Marks that the given node is visited.
     *
     * @param node
     * The node to mark as visited.
     */
    private fun markVisited(node: BFSNode) {
        visited!!.add(node.node)
    }

    /**
     * Checks if the given node has already been visited by the algorithm.
     *
     * @param node
     * The node to check for if it's been visited already.
     * @return `true` if the node has been visited, `false` when the node hasn't been
     * visited.
     */
    private fun isVisited(node: N): Boolean {
        return visited!!.contains(node)
    }

    /**
     * Get all the adjacent nodes of a given BFSNode.
     *
     * @param node
     * The node to get all adjacent nodes of.
     * @return A list containing all adjacent nodes relative to `node`.
     */
    private fun getAdjacencyList(node: BFSNode): List<N> {
        return adjacencyFunction(node.node)
    }

    /**
     * Reconstructs the shortest path from the end node towards the node with distance 0.
     *
     * @param end
     * The end node from which to backtrack to the starting node.
     * @return The shortest path from start to end nodes.
     */
    private fun constructShortestPath(end: BFSNode): List<N> {
        val shortestPath: MutableList<N> = ArrayList()
        var node = end
        while (node.distance != 0) {
            shortestPath.add(0, node.node)
            node = node.predecessor!!
        }
        return shortestPath
    }

    /**
     * Checks if the BFS algorithm will visit all nodes.
     *
     *
     * In other words: if the BFS will stop at a target node or not.
     *
     * @return `true` if the algorithm stop only when all nodes are covered: the algorithm
     * executes without a goal in mind, `false` if the algorithm stops when the target has
     * been found.
     */
    private fun hasGoal(): Boolean {
        return end == null
    }

    /**
     * Class used by the BFS algorithm to keep track of augmented values.
     *
     * @author Hannah Schellekens, Dylan ter Veen
     */
    private inner class BFSNode(
        /**
         * The original version of the node.
         */
        val node: N,
        /**
         * The distance from the starting node to this node.
         */
        var distance: Int
    ) {

        /**
         * Get the original (wrapped) node.
         *
         * @return The wrapped node object.
         */
        /**
         * Get the distance from the starting node to this node.
         */
        /**
         * Set the distance from the starting node to this node.
         *
         * @param distance
         * The new distance from the start node to this node.
         */
        /**
         * Get the predecessing node from the start node to this one.
         */
        /**
         * The predecessing node from the start node to this one.
         */
        var predecessor: BFSNode? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other!!.javaClass != javaClass) {
                return false
            }
            val bfsNode = other as? BFS<*>.BFSNode
            return node == bfsNode?.node
        }

        override fun hashCode(): Int {
            return node.hashCode()
        }
    }

    /**
     * Actions that can be executed after an iteration action.
     *
     * @author Hannah Schellekens
     */
    enum class BFSAction {

        CONTINUE,
        ABORT
    }
}