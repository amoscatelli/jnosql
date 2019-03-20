/*
 *  Copyright (c) 2019 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.reflection;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jnosql.artemis.DynamicQueryException;
import org.jnosql.artemis.Page;
import org.jnosql.artemis.Pagination;
import org.jnosql.artemis.Repository;
import org.jnosql.artemis.Sorts;
import org.jnosql.diana.api.NonUniqueResultException;
import org.jnosql.diana.api.Sort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jnosql.artemis.Sorts.sorts;
import static org.jnosql.artemis.reflection.DynamicReturn.findSorts;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicReturnTest {

    @Test
    public void shouldReturnNPEWhenThereIsPagination() {
        Method method = getMethod(PersonRepository.class, "getOptional");
        Supplier<List<?>> list = Collections::emptyList;
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        assertThrows(NullPointerException.class, () ->
                DynamicReturn.builder()
                        .withClassSource(Person.class)
                        .withMethodSource(method).withList(list)
                        .withSingleResult(singlResult)
                        .withPagination(Pagination.page(1L).size(2L)).build());

    }

    @Test
    public void shouldReturnEmptyOptional() {

        Method method = getMethod(PersonRepository.class, "getOptional");
        Supplier<List<?>> list = Collections::emptyList;
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Optional);
        Optional<Person> optional = (Optional) execute;
        Assertions.assertFalse(optional.isPresent());
    }

    @Test
    public void shouldReturnOptional() {

        Method method = getMethod(PersonRepository.class, "getOptional");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Optional);
        Optional<Person> optional = (Optional) execute;
        assertTrue(optional.isPresent());
        Assertions.assertEquals(new Person("Ada"), optional.get());
    }

    @Test
    public void shouldReturnOptionalError() {

        Method method = getMethod(PersonRepository.class, "getOptional");
        Supplier<List<?>> list = () -> Arrays.asList(new Person("Poliana"), new Person("Otavio"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();

        assertThrows(NonUniqueResultException.class, dynamicReturn::execute);

    }


    @Test
    public void shouldReturnAnInstance() {
        Method method = getMethod(PersonRepository.class, "getInstance");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Person);
        Person person = (Person) execute;
        Assertions.assertEquals(new Person("Ada"), person);
    }


    @Test
    public void shouldReturnNull() {

        Method method = getMethod(PersonRepository.class, "getInstance");
        Supplier<List<?>> list = Collections::emptyList;
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertNull(execute);
    }


    @Test
    public void shouldReturnList() {

        Method method = getMethod(PersonRepository.class, "getList");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof List);
        List<Person> persons = (List) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.get(0));
    }

    @Test
    public void shouldReturnIterable() {

        Method method = getMethod(PersonRepository.class, "getIterable");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Iterable);
        Iterable<Person> persons = (List) execute;
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }


    @Test
    public void shouldReturnCollection() {

        Method method = getMethod(PersonRepository.class, "getCollection");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Collection);
        Collection<Person> persons = (Collection) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }

    @Test
    public void shouldReturnSet() {

        Method method = getMethod(PersonRepository.class, "getSet");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Set);
        Set<Person> persons = (Set) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }

    @Test
    public void shouldReturnQueue() {

        Method method = getMethod(PersonRepository.class, "getQueue");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Queue);
        Queue<Person> persons = (Queue) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }


    @Test
    public void shouldReturnStream() {

        Method method = getMethod(PersonRepository.class, "getStream");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Stream);
        Stream<Person> persons = (Stream) execute;
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }

    @Test
    public void shouldReturnSortedSet() {

        Method method = getMethod(PersonRepository.class, "getSortedSet");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof SortedSet);
        SortedSet<Person> persons = (SortedSet) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }

    @Test
    public void shouldReturnNavigableSet() {

        Method method = getMethod(PersonRepository.class, "getNavigableSet");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof NavigableSet);
        NavigableSet<Person> persons = (NavigableSet) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }


    @Test
    public void shouldReturnDeque() {

        Method method = getMethod(PersonRepository.class, "getDeque");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();
        Object execute = dynamicReturn.execute();
        assertTrue(execute instanceof Deque);
        Deque<Person> persons = (Deque) execute;
        Assertions.assertFalse(persons.isEmpty());
        Assertions.assertEquals(new Person("Ada"), persons.iterator().next());
    }

    @Test
    public void shouldReturnErrorWhenExecutePage() {
        Method method = getMethod(PersonRepository.class, "getPage");
        Supplier<List<?>> list = () -> singletonList(new Person("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Person.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();

        assertThrows(DynamicQueryException.class, dynamicReturn::execute);
    }

    @Test
    public void shouldReturnErrorNavigableSetEntityIsNotComparable() {

        Method method = getMethod(AnimalRepository.class, "getSortedSet");
        Supplier<List<?>> list = () -> singletonList(new Animal("Ada"));
        Supplier<Optional<?>> singlResult = DynamicReturn.toSingleResult(method).apply(list);
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(Animal.class)
                .withMethodSource(method).withList(list)
                .withSingleResult(singlResult).build();

        assertThrows(DynamicQueryException.class, dynamicReturn::execute);
    }

    @Test
    public void shouldReturnNullWhenParamIsEmptyOnFindPagination() {
        assertNull(DynamicReturn.findPagination(null));
        assertNull(DynamicReturn.findPagination(new Object[0]));
    }

    @Test
    public void shouldFindPagination() {
        Pagination pagination = Pagination.page(1L).size(2);
        Pagination pagination1 = DynamicReturn.findPagination(new Object[]{"value", 23, pagination});
        Assertions.assertEquals(pagination, pagination1);
    }

    @Test
    public void shouldReturnNullWhenThereIsNotPagination() {
        Pagination pagination = DynamicReturn.findPagination(new Object[]{"value", 23, BigDecimal.TEN});
        assertNull(pagination);
    }

    @Test
    public void shouldReturnEmptyWhenThereIsNotParametersAtSorts() {
        assertTrue(findSorts(null).isEmpty());
        assertTrue(findSorts(new Object[0]).isEmpty());
    }

    @Test
    public void shouldShouldFindSortAtMethod() {
        Sort name = Sort.asc("name");
        Sort age = Sort.desc("age");
        List<Sort> sorts = findSorts(new Object[]{"Otavio", 23, Pagination.page(2).size(2), name, age});
        assertThat(sorts, Matchers.contains(name, age));
    }

    @Test
    public void shouldShouldFindSortsAtMethod() {
        Sort name = Sort.asc("name");
        Sort age = Sort.desc("age");
        List<Sort> sorts = findSorts(new Object[]{"Otavio", 23, Pagination.page(2).size(2), sorts().add(name).add(age)});
        assertThat(sorts, Matchers.contains(name, age));
    }

    @Test
    public void shouldShouldFindSortAndSortsAtMethod() {

        Sort name = Sort.asc("name");
        Sort age = Sort.desc("age");
        List<Sort> sorts = findSorts(new Object[]{"Otavio", 23, Pagination.page(2).size(2), name, age,
                Sorts.sorts().desc("name").asc("age")});

        assertThat(sorts, Matchers.contains(name, age, Sort.desc("name"), Sort.asc("age")));
    }

    @Test
    public void shouldFindEmptyListWhenThereIsNotSortOrSorts() {
        List<Sort> sorts = findSorts(new Object[]{"Otavio", 23, Pagination.page(2).size(2)});
        assertTrue(sorts.isEmpty());
    }


    private Method getMethod(Class<?> repository, String methodName) {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }


    private static class Animal {
        private final String name;

        private Animal(String name) {
            this.name = name;
        }


    }

    private static class Person implements Comparable<Person> {

        private final String name;

        private Person(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Person person = (Person) o;
            return Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public int compareTo(Person o) {
            return name.compareTo(o.name);
        }
    }

    private interface AnimalRepository extends Repository<Animal, String> {

        SortedSet<Person> getSortedSet();
    }

    private interface PersonRepository extends Repository<Person, String> {

        Optional<Person> getOptional();

        Person getInstance();

        List<Person> getList();

        Iterable<Person> getIterable();

        Collection<Person> getCollection();

        Set<Person> getSet();

        Queue<Person> getQueue();

        Stream<Person> getStream();

        SortedSet<Person> getSortedSet();

        NavigableSet<Person> getNavigableSet();

        Deque<Person> getDeque();

        Page<Person> getPage();
    }


}