package org.fentanylsolutions.fentlib.carbonextension.carbonannotations;

public interface IArraySerializer<T> {

    String serialize(T value);

    T deserialize(String value);
}
