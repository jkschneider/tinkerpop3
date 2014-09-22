package com.tinkerpop.gremlin.structure.util.referenced;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.util.ElementHelper;

import java.io.Serializable;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class ReferencedElement implements Element, Serializable {

    protected Object id;
    protected String label;

    protected ReferencedElement() {

    }

    public ReferencedElement(final Element element) {
        this.id = element.id();
        this.label = element.label();
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        throw new IllegalStateException("Referenced elements do not have properties:" + this);
    }

    @Override
    public void remove() {
        throw new IllegalStateException("Referenced elements can not be removed:" + this);
    }
}