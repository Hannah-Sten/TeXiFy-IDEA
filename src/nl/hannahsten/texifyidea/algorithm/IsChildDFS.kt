package nl.hannahsten.texifyidea.algorithm

import java.util.ArrayDeque
import java.util.Deque

/**
 * Depth first search that checks if a goal node is a child of the given start node.
 *
 * @author Hannah Schellekens
 */
open class IsChildDFS<Node>(

        /**
         * The starting node.
         */
        val start: Node,

        /**
         * Function that gets all the children of a certain node.
         */
        inline val children: (Node) -> Collection<Node>,

        /**
         * Function that tests of the given node is the end node.
         */
        inline val isGoal: (Node) -> Boolean
) {

    open fun execute(): Boolean {
        // Done when starting at the goal.
        if (isGoal(start)) {
            return true
        }

        // Maintain list of visited nodes, to detect loops
        val visited = mutableSetOf<Node>()

        // Iterative implementation. Yay.
        val stack: Deque<Node> = ArrayDeque()
        stack.push(start)

        while (!stack.isEmpty()) {
            val child = stack.pop()
            visited.add(child)
            if (isGoal(child)) {
                return true
            }

            // Don't visit nodes twice to avoid loops
            children(child).filter { it !in visited }.forEach(stack::push)
        }

        return false
    }
}