package nl.rubensten.texifyidea.util;

import java.util.Optional;

/**
 * Contains exactly 1 object.
 * <p>
 * Maybe not really useful, but I wanted to make this so badly... :3
 *
 * @author Ruben Schellekens
 */
public class Container<T> {

    /**
     * The item that is contained in the container (Duh).
     */
    private T item;

    /**
     * Create an empty container.
     */
    public Container() {
        this(null);
    }

    /**
     * Create a container that contains the given item.
     */
    public Container(T item) {
        this.item = item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public Optional<T> getItem() {
        return Optional.ofNullable(item);
    }

    @Override
    public String toString() {
        return "Container[" + item + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Container)) {
            return false;
        }

        Container<?> container = (Container<?>)o;
        return item != null ? item.equals(container.item) : container.item == null;
    }

    @Override
    public int hashCode() {
        return item != null ? item.hashCode() : 0;
    }
}
