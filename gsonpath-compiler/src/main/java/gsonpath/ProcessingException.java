package gsonpath;

import javax.lang.model.element.Element;

/**
 * Represents that something has gone wrong during annotation processing.
 */
public class ProcessingException extends Exception {
    private final Element element;

    public ProcessingException(String message) {
        this(message, null);
    }

    public ProcessingException(String message, Element element) {
        super(message);
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
