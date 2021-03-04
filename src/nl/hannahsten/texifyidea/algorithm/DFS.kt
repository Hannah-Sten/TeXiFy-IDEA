package nl.hannahsten.texifyidea.algorithm

import java.util.*

/**
 * Depth first search.
 *
 * @author Hannah Schellekens
 */
open class DFS<Node>(

    /**
     * The starting node.
     */
    val start: Node,

    /**
     * Function that gets all the children of a certain node.
     */
    val children: (Node) -> Collection<Node>,
) {

    /**
     * @return All direct and indirect children of the start node, excluding the start node.
     */
    open fun execute(): Set<Node> {
        // Maintain list of visited nodes, to detect loops
        val visited = mutableSetOf<Node>()

        // Iterative implementation. Yay.
        val stack: Deque<Node> = ArrayDeque()
        stack.push(start)

        while (!stack.isEmpty()) {
            val child = stack.pop()
            visited.add(child)

            // Don't visit nodes twice to avoid loops
            children(child).filter { it !in visited }.forEach(stack::push)
        }

        visited.remove(start)
        return visited
    }
}