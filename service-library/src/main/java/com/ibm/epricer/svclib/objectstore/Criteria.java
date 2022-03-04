package com.ibm.epricer.svclib.objectstore;

import static com.ibm.epricer.svclib.objectstore.Criteria.OperationKey.CONTAINS;
import static com.ibm.epricer.svclib.objectstore.Criteria.OperationKey.ENDS_WITH;
import static com.ibm.epricer.svclib.objectstore.Criteria.OperationKey.IN;
import static com.ibm.epricer.svclib.objectstore.Criteria.OperationKey.IS;
import static com.ibm.epricer.svclib.objectstore.Criteria.OperationKey.STARTS_WITH;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Persistence-technology-neutral WHERE clause criteria with fluent interface. Predicates are
 * intentionally limited to is, in, contains, startsWith and endsWith to ensure criteria can be
 * cleanly translated into persistence-technology-specific queries by the object request executors.
 * 
 * @author Kiran Chowdhury
 */

public class Criteria {
    private final Deque<Criterion> criteria;

    /**
     * Instances can be created only with static factory method below
     * 
     * @param field - field name
     */
    private Criteria(String field) {
        criteria = new LinkedList<>();
        criteria.add(new Criterion(field));
    }

    /**
     * Static factory method
     * 
     * @param field - first criterion field name
     */
    public static Criterion where(String field) {
        return new Criteria(field).criteria.getLast();
    }

    public Collection<Criterion> criteria() {
        return Collections.unmodifiableCollection(criteria);
    }

    /**
     * Currently only ANDs are supported but ORs can be implemented, too.
     */
    public Criterion and(String field) {
        Criterion criterion = new Criterion(field);
        criteria.addLast(criterion);
        return criterion;
    }

    @Override
    public String toString() {
        return String.format("Criteria %s", criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria);
    }

    @Override
    public boolean equals(Object obj) {
        // LinkedList inherits equals() from AbstractList<E>
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Criteria other = (Criteria) obj;
        return Objects.equals(criteria, other.criteria);
    }
    
    public class Criterion {
        private String field;
        private OperationKey key;
        private Object value; // can be either String or Iterable<String>

        private Criterion(String field) {
            this.field = field;
        }

        /**
         * Crates new Predicate without any wild-cards. Strings with blanks will be escaped.
         */
        public Criteria is(String value) {
            return setPredicate(IS, value);
        }

        /**
         * Crates new Predicate for multiple values, like Collection
         */
        public Criteria in(Iterable<String> values) {
            return setPredicate(IN, values);
        }

        /**
         * Crates new Predicate for multiple values (arg0 arg1 arg2 ...)
         */
        public Criteria in(String... values) {
            return setPredicate(IN, Arrays.asList(values));
        }

        /**
         * Crates new Predicate with leading and trailing wild-card
         */
        public Criteria contains(String value) {
            return setPredicate(CONTAINS, value);
        }

        /**
         * Crates new Predicate with trailing wild-card
         */
        public Criteria startsWith(String value) {
            return setPredicate(STARTS_WITH, value);
        }

        /**
         * Crates new Predicate with leading wild-card
         */
        public Criteria endsWith(String value) {
            return setPredicate(ENDS_WITH, value);
        }

        private Criteria setPredicate(OperationKey key, Object value) {
            criteria.getLast().key = key;
            criteria.getLast().value = value;
            return Criteria.this;
        }

        public String getField() {
            return field;
        }

        public OperationKey getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return field + " " + key + " " + value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, key, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Criterion other = (Criterion) obj;
            return Objects.equals(field, other.field) && key == other.key && Objects.equals(value, other.value);
        }
    }

    public static enum OperationKey {
        IS, IN, CONTAINS, STARTS_WITH, ENDS_WITH;
    }
}
