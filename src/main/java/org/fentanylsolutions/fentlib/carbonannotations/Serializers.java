package org.fentanylsolutions.fentlib.carbonannotations;

public class Serializers {

    public static class DefaultStringSerializer implements IArraySerializer<String> {

        @Override
        public String serialize(String value) {
            return value;
        }

        @Override
        public String deserialize(String value) {
            return value;
        }
    }

    public static class IntSerializer implements IArraySerializer<Integer> {

        @Override
        public String serialize(Integer value) {
            return String.valueOf(value);
        }

        @Override
        public Integer deserialize(String value) {
            return Integer.parseInt(value);
        }
    }

    public static class DoubleSerializer implements IArraySerializer<Double> {

        @Override
        public String serialize(Double value) {
            return String.valueOf(value);
        }

        @Override
        public Double deserialize(String value) {
            return Double.parseDouble(value);
        }
    }

    public static class FloatSerializer implements IArraySerializer<Float> {

        @Override
        public String serialize(Float value) {
            return String.valueOf(value);
        }

        @Override
        public Float deserialize(String value) {
            return Float.parseFloat(value);
        }
    }

    public static class BooleanSerializer implements IArraySerializer<Boolean> {

        @Override
        public String serialize(Boolean value) {
            return String.valueOf(value);
        }

        @Override
        public Boolean deserialize(String value) {
            return Boolean.getBoolean(value);
        }
    }
}
