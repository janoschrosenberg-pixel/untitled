package terminal;

import java.util.LinkedList;
import java.util.EmptyStackException;

/**
 * Ein Stack mit begrenzter Kapazität.
 * Wenn die maximale Anzahl an Elementen überschritten wird,
 * wird das älteste (unterste) Element automatisch entfernt.
 */
public class BoundedStack<E> {
    private final LinkedList<E> elements;
    private final int maxSize;
    
    /**
     * Erstellt einen neuen BoundedStack mit der angegebenen Kapazität.
     * 
     * @param maxSize Die maximale Anzahl an Elementen
     * @throws IllegalArgumentException wenn maxSize kleiner oder gleich 0 ist
     */
    public BoundedStack(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Maximale Größe muss größer als 0 sein");
        }
        this.maxSize = maxSize;
        this.elements = new LinkedList<>();
    }
    
    /**
     * Fügt ein Element oben auf den Stack hinzu.
     * Wenn die Kapazität überschritten wird, wird das unterste Element entfernt.
     * 
     * @param element Das hinzuzufügende Element
     */
    public void push(E element) {
        elements.addLast(element);
        
        // Wenn die maximale Größe überschritten wurde, entferne das unterste Element
        if (elements.size() > maxSize) {
            elements.removeFirst();
        }
    }
    
    /**
     * Entfernt und gibt das oberste Element zurück.
     * 
     * @return Das oberste Element
     * @throws EmptyStackException wenn der Stack leer ist
     */
    public E pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return elements.removeLast();
    }
    
    /**
     * Gibt das oberste Element zurück, ohne es zu entfernen.
     * 
     * @return Das oberste Element
     * @throws EmptyStackException wenn der Stack leer ist
     */
    public E peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return elements.getLast();
    }
    
    /**
     * Prüft, ob der Stack leer ist.
     * 
     * @return true wenn der Stack leer ist, sonst false
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }
    
    /**
     * Gibt die aktuelle Anzahl der Elemente im Stack zurück.
     * 
     * @return Die Anzahl der Elemente
     */
    public int size() {
        return elements.size();
    }
    
    /**
     * Prüft, ob der Stack seine maximale Kapazität erreicht hat.
     * 
     * @return true wenn der Stack voll ist, sonst false
     */
    public boolean isFull() {
        return elements.size() == maxSize;
    }
    
    /**
     * Gibt die maximale Kapazität des Stacks zurück.
     * 
     * @return Die maximale Kapazität
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * Entfernt alle Elemente aus dem Stack.
     */
    public void clear() {
        elements.clear();
    }
    
    @Override
    public String toString() {
        return "BoundedStack[" + elements.toString() + ", max=" + maxSize + "]";
    }
    

}