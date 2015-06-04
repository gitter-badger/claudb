/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package tonivade.db.data;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Stream;


public class DatabaseValue {

    private final DataType type;

    private Object value;

    public DatabaseValue(DataType type) {
        this(type, null);
    }

    public DatabaseValue(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * @return the type
     */
    public DataType getType() {
        return type;
    }

    /**
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DatabaseValue other = (DatabaseValue) obj;
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public static DatabaseValue string(String value) {
        return new DatabaseValue(DataType.STRING, value);
    }

    public static DatabaseValue list(Collection<String> values) {
        return new DatabaseValue(
                DataType.LIST,
                values.stream().collect(toCollection(() -> new LinkedList<>())));
    }

    public static DatabaseValue list(String ... values) {
        return new DatabaseValue(
                DataType.LIST,
                Stream.of(values).collect(toCollection(() -> new LinkedList<>())));
    }

    public static DatabaseValue set(Collection<String> values) {
        return new DatabaseValue(
                DataType.SET,
                values.stream().collect(toCollection(() -> new LinkedHashSet<>())));
    }

    public static DatabaseValue set(String ... values) {
        return new DatabaseValue(
                DataType.SET,
                Stream.of(values).collect(toCollection(() -> new LinkedHashSet<>())));
    }

    public static DatabaseValue zset(Collection<Entry<Float, String>> values) {
        return new DatabaseValue(
                DataType.ZSET,
                values.stream().collect(
                        toCollection(() ->
                                new TreeSet<>((o1, o2) -> o1.getKey().compareTo(o2.getKey())))));
    }

    @SafeVarargs
    public static DatabaseValue zset(Entry<Float, String> ... values) {
        return new DatabaseValue(
                DataType.ZSET,
                Stream.of(values).collect(
                        toCollection(() ->
                                new TreeSet<>((o1, o2) -> o1.getKey().compareTo(o2.getKey())))));
    }

    public static DatabaseValue hash(Collection<Entry<String, String>> values) {
        return new DatabaseValue(
                DataType.HASH,
                values.stream().collect(toMap(Entry::getKey, Entry::getValue)));
    }

    @SafeVarargs
    public static DatabaseValue hash(Entry<String, String> ... values) {
        return new DatabaseValue(
                DataType.HASH,
                Stream.of(values).collect(toMap(Entry::getKey, Entry::getValue)));
    }

    public static Entry<String, String> entry(String key, String value) {
        return new SimpleEntry<String, String>(key, value);
    }

    public static Entry<Float, String> score(float score, String value) {
        return new SimpleEntry<Float, String>(score, value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DatabaseValue [type=" + type + ", value=" + value + "]";
    }

}
