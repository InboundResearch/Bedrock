package us.irdev.bedrock.bag;

import us.irdev.bedrock.bag.expr.BooleanExpr;
import us.irdev.bedrock.bag.formats.MimeType;

import java.util.function.Function;
import java.util.function.Supplier;

abstract public class Bag {
    Object objectify (Object value) {
        if (value != null) {
            var type = value.getClass ();
            var typeName = type.getCanonicalName ();
            switch (typeName) {
                case "java.lang.String":
                    // is this the right place to do a transformation that converts quotes to some
                    // escape character?
                    return value;

                case "java.lang.Long": case "java.lang.Integer": case "java.lang.Short": case "java.lang.Byte":
                case "java.lang.Character":
                case "java.lang.Boolean":
                case "java.lang.Double": case "java.lang.Float":
                    return value.toString ();

                //case "BagObject":
                case "us.irdev.bedrock.bag.BagObject":
                //case "BagArray":
                case "us.irdev.bedrock.bag.BagArray":
                    return value;

                default:
                    // if it's an enum, just get the string value
                    if (type.isEnum ()) {
                        return ((Enum)value).name ();
                    }
                    // no other type should be stored in the bedrock classes
                    //log.error ("Unhandled type: " + typeName);
                    throw new UnsupportedTypeException (type);
            }
        }
        return null;
    }

    /**
     *
     * @param key
     * @return
     */
    abstract public Object getObject (String key);

    /**
     * Returns true if the Selectable matches the 'match' criteria
     * @param booleanExpr a BooleanExpr containing criteria:
     * @return
     */
    public boolean match (BooleanExpr booleanExpr) {
        if (booleanExpr != null) {
            return booleanExpr.evaluateIsTrue (this);
        }
        return true;
    }

    /**
     * Retrieve a mapped element and return it as a String.
     *
     * @param key A string value used to index the element.
     * @return The element as a string, or null if the element is not found (or not a String).
     */
    public String getString (String key) {
        return getString (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a String.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new String if the requested key was not found
     * @return The element as a string, or notFound if the element is not found.
     */
    public String getString (String key, Supplier<String> notFound) {
        var object = getObject (key);
        return (object instanceof String) ? (String) object : notFound.get ();
    }

    /**
     * Retrieve a mapped element and return it as a BagObject.
     *
     * @param key A string value used to index the element.
     * @return The element as a BagObject, or null if the element is not found.
     */
    public BagObject getBagObject (String key) {
        return getBagObject (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a BagObject.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new BagObject if the requested key was not found
     * @return The element as a BagObject, or notFound if the element is not found.
     */
    public BagObject getBagObject (String key, Supplier<BagObject> notFound) {
        var object = getObject (key);
        return (object instanceof BagObject) ? (BagObject) object : notFound.get ();
    }

    /**
     * Retrieve a mapped element and return it as a BagArray.
     *
     * @param key A string value used to index the element.
     * @return The element as a BagArray, or null if the element is not found.
     */
    public BagArray getBagArray (String key) {
        return getBagArray (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a BagArray.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new BagArray if the requested key was not found
     * @return The element as a BagArray, or notFound if the element is not found.
     */
    public BagArray getBagArray (String key, Supplier<BagArray> notFound) {
        var object = getObject (key);
        return (object instanceof BagArray) ? (BagArray) object : notFound.get ();
    }

    public <EnumType extends Enum<EnumType>> EnumType getEnum (String key, Class<EnumType> type) {
        return getEnum (key, type, () -> null);
    }

    public <EnumType extends Enum<EnumType>> EnumType getEnum (String key, Class<EnumType> type, Supplier<EnumType> notFound) {
        var object = getObject (key);
        return (object instanceof String) ? Enum.valueOf (type, (String) object) : notFound.get ();
    }

    private <ParsedType> ParsedType getParsed (String key, Function<String, ParsedType> parser, Supplier<ParsedType> notFound) {
        var object = getObject (key);
        return (object instanceof String) ? parser.apply ((String) object) : notFound.get ();
    }

    /**
     * Retrieve a mapped element and return it as a Boolean.
     *
     * @param key A string value used to index the element.
     * @return The element as a Boolean, or null if the element is not found.
     */
    public Boolean getBoolean (String key) {
        return getBoolean (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a Boolean.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new Boolean if the requested key was not found
     * @return The element as a Boolean, or notFound if the element is not found.
     */
    public Boolean getBoolean (String key, Supplier<Boolean> notFound) {
        return getParsed (key, Boolean::valueOf, notFound);
    }

    /**
     * Retrieve a mapped element and return it as a Long.
     *
     * @param key A string value used to index the element.
     * @return The element as a Long, or null if the element is not found.
     */
    public Long getLong (String key) {
        return getLong (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a Long.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new Long if the requested key was not found
     * @return The element as a Long, or notFound if the element is not found.
     */
    public Long getLong (String key, Supplier<Long> notFound) {
        return getParsed (key, Long::valueOf, notFound);
    }

    /**
     * Retrieve a mapped element and return it as an Integer.
     *
     * @param key A string value used to index the element.
     * @return The element as an Integer, or null if the element is not found.
     */
    public Integer getInteger (String key) {
        return getInteger (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as an Integer.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new Integer if the requested key was not found
     * @return The element as an Integer, or notFound if the element is not found.
     */
    public Integer getInteger (String key, Supplier<Integer> notFound) {
        return getParsed (key, Integer::valueOf, notFound);
    }

    /**
     * Retrieve a mapped element and return it as a Double.
     *
     * @param key A string value used to index the element.
     * @return The element as a Double, or null if the element is not found.
     */
    public Double getDouble (String key) {
        return getDouble (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a Double.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new Double if the requested key was not found
     * @return The element as a Double, or notFound if the element is not found.
     */
    public Double getDouble (String key, Supplier<Double> notFound) {
        return getParsed (key, Double::valueOf, notFound);
    }

    /**
     * Retrieve a mapped element and return it as a Float.
     *
     * @param key A string value used to index the element.
     * @return The element as a Float, or null if the element is not found.
     */
    public Float getFloat (String key) {
        return getFloat (key, () -> null);
    }

    /**
     * Retrieve a mapped element and return it as a Float.
     *
     * @param key A string value used to index the element.
     * @param notFound A function to create a new Float if the requested key was not found
     * @return The element as a Float, or notFound if the element is not found.
     */
    public Float getFloat (String key, Supplier<Float> notFound) {
        return getParsed (key, Float::valueOf, notFound);
    }

    /**
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals (Object object) {
        return (object != null) &&
                (getClass ().equals (object.getClass ())) &&
                toString ().equals (object.toString ());
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode () {
        return toString ().hashCode ();
    }

    /**
     *
     * @param format
     * @return
     */
    abstract public String toString (String format);

    @Override
    public String toString () {
        return toString(MimeType.DEFAULT);
    }
}
