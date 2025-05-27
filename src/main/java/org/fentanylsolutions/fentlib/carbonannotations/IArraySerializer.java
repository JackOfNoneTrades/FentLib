package org.fentanylsolutions.fentlib.carbonannotations;

public interface IArraySerializer<T> {

    String serialize(T value);

    T deserialize(String value);
}
