/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.graph;

import jakarta.data.exceptions.EmptyResultException;
import jakarta.data.exceptions.MappingException;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.AttributeConverter;
import org.eclipse.jnosql.mapping.Converters;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jnosql.mapping.reflection.ConstructorBuilder;
import org.eclipse.jnosql.mapping.reflection.ConstructorMetadata;
import org.eclipse.jnosql.mapping.reflection.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;
import org.eclipse.jnosql.mapping.reflection.InheritanceMetadata;
import org.eclipse.jnosql.mapping.reflection.ParameterMetaData;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jnosql.mapping.reflection.MappingType.EMBEDDED;


public abstract class GraphConverter {


    protected abstract EntitiesMetadata getEntities();

    protected abstract Converters getConverters();

    protected abstract GraphEventPersistManager getEventManager();

    protected abstract Graph getGraph();

    /**
     * Converts entity object to  TinkerPop Vertex
     *
     * @param entity the entity
     * @param <T>    the entity type
     * @return the ThinkerPop Vertex with the entity values
     * @throws NullPointerException when entity is null
     */
    public <T> Vertex toVertex(T entity) {
        requireNonNull(entity, "entity is required");

        EntityMetadata mapping = getEntities().get(entity.getClass());
        String label = mapping.getName();

        List<FieldGraph> fields = mapping.getFields().stream()
                .map(f -> to(f, entity))
                .filter(FieldGraph::isNotEmpty).collect(toList());

        Optional<FieldGraph> id = fields.stream().filter(FieldGraph::isId).findFirst();
        final Function<Property, Vertex> findVertexOrCreateWithId = p -> {
            Iterator<Vertex> vertices = getGraph().vertices(p.value());
            return vertices.hasNext() ? vertices.next() :
                    getGraph().addVertex(org.apache.tinkerpop.gremlin.structure.T.label, label,
                            org.apache.tinkerpop.gremlin.structure.T.id, p.value());
        };

        Vertex vertex = id.map(i -> i.toElement(getConverters()))
                .map(findVertexOrCreateWithId)
                .orElseGet(() -> getGraph().addVertex(label));

        fields.stream().filter(FieldGraph::isNotId)
                .flatMap(f -> f.toElements(this, getConverters()).stream())
                .forEach(p -> vertex.property(p.key(), p.value()));

        mapping.getInheritance().ifPresent(i ->
                vertex.property(i.getDiscriminatorColumn(), i.getDiscriminatorValue()));

        return vertex;
    }


    /**
     * List the fields in the entity as property exclude fields annotated with {@link jakarta.nosql.Id}
     *
     * @param entity the entity
     * @param <T>    the entity type
     * @return the properties of an entity
     * @throws NullPointerException when entity is null
     */
    public <T> List<Property<?>> getProperties(T entity) {
        Objects.requireNonNull(entity, "entity is required");
        EntityMetadata mapping = getEntities().get(entity.getClass());
        List<FieldGraph> fields = mapping.getFields().stream()
                .map(f -> to(f, entity))
                .filter(FieldGraph::isNotEmpty).collect(toList());

        return fields.stream().filter(FieldGraph::isNotId)
                .flatMap(f -> f.toElements(this, getConverters()).stream())
                .collect(Collectors.toList());
    }

    /**
     * Converts vertex to an entity
     *
     * @param vertex the vertex
     * @param <T>    the entity type
     * @return a entity instance
     * @throws NullPointerException when vertex is null
     */
    public <T> T toEntity(Vertex vertex) {
        requireNonNull(vertex, "vertex is required");
        EntityMetadata mapping = getEntities().findByName(vertex.label());

        ConstructorMetadata constructor = mapping.getConstructor();
        if (constructor.isDefault()) {
            List<Property> properties = vertex.keys()
                    .stream()
                    .map(k -> DefaultProperty.of(k, vertex.value(k))).collect(toList());

            T entity;
            if (mapping.isInheritance()) {
                entity = mapInheritanceEntity(vertex, properties, mapping.getType());
            } else {
                entity = toEntity((Class<T>) mapping.getType(), properties);
            }
            feedId(vertex, entity);
            getEventManager().firePostEntity(entity);
            return entity;
        } else {
            return convertEntityByConstructor(vertex, mapping);
        }
    }

    /**
     * Converts vertex to an entity
     *
     * @param type   the entity class
     * @param vertex the vertex
     * @param <T>    the entity type
     * @return a entity instance
     * @throws NullPointerException when vertex or type is null
     */
    public <T> T toEntity(Class<T> type, Vertex vertex) {
        requireNonNull(type, "type is required");
        requireNonNull(vertex, "vertex is required");

        List<Property> properties = vertex.keys().stream().map(k -> DefaultProperty.of(k, vertex.value(k))).collect(toList());
        T entity = toEntity(type, properties);
        feedId(vertex, entity);
        getEventManager().firePostEntity(entity);
        return entity;
    }

    /**
     * Converts vertex to an entity
     * Instead of creating a new object is uses the instance used in this parameters
     *
     * @param type   the entity class
     * @param vertex the vertex
     * @param <T>    the entity type
     * @return a entity instance
     * @throws NullPointerException when vertex or type is null
     */
    public <T> T toEntity(T type, Vertex vertex) {
        requireNonNull(type, "entityInstance is required");
        requireNonNull(vertex, "vertex is required");

        List<Property> properties = vertex.keys().stream()
                .map(k -> DefaultProperty.of(k, vertex.value(k)))
                .collect(toList());

        EntityMetadata mapping = getEntities().get(type.getClass());
        convertEntity(properties, mapping, type);
        feedId(vertex, type);
        return type;

    }

    /**
     * Converts {@link EdgeEntity} from {@link Edge} Thinkerpop
     *
     * @param edge the ThinkerPop edge
     * @return an EdgeEntity instance
     * @throws NullPointerException when Edge is null
     */
    public EdgeEntity toEdgeEntity(Edge edge) {
        requireNonNull(edge, "vertex is required");
        Object out = toEntity(edge.outVertex());
        Object in = toEntity(edge.inVertex());
        return new DefaultEdgeEntity<>(edge, in, out);
    }

    /**
     * Converts {@link Edge} from {@link EdgeEntity}
     *
     * @param edge the EdgeEntity instance
     * @return a Edge instance
     * @throws NullPointerException when edge entity is null
     */
    public Edge toEdge(EdgeEntity edge) {
        requireNonNull(edge, "vertex is required");
        Object id = edge.id();
        Iterator<Edge> edges = getGraph().edges(id);
        if (edges.hasNext()) {
            return edges.next();
        }
        throw new EmptyResultException("Edge does not found in the database with id: " + id);
    }

    private <T> T convertEntityByConstructor(Vertex vertex, EntityMetadata mapping) {
        ConstructorBuilder builder = ConstructorBuilder.of(mapping.getConstructor());
        List<Property<?>> properties = vertex.keys().stream()
                .map(k -> DefaultProperty.of(k, vertex.value(k)))
                .collect(toList());
        for (ParameterMetaData parameter : builder.getParameters()) {
            Optional<Property<?>> property = properties.stream()
                    .filter(c -> c.key().equals(parameter.getName()))
                    .findFirst();
            property.ifPresentOrElse(p -> parameter.getConverter().ifPresentOrElse(c -> {
                Object value = getConverters().get(c).convertToEntityAttribute(p.value());
                builder.add(value);
            }, () -> {
                Value value = Value.of(p.value());
                builder.add(value.get(parameter.getType()));
            }), builder::addEmptyParameter);
        }
        return builder.build();
    }

    private <T> void feedId(Vertex vertex, T entity) {
        EntityMetadata mapping = getEntities().get(entity.getClass());
        Optional<FieldMapping> id = mapping.getId();


        Object vertexId = vertex.id();
        if (Objects.nonNull(vertexId) && id.isPresent()) {
            FieldMapping fieldMapping = id.get();
            fieldMapping.getConverter().ifPresentOrElse(c -> {
                AttributeConverter attributeConverter = getConverters().get(c);
                Object attributeConverted = attributeConverter.convertToEntityAttribute(vertexId);
                fieldMapping.write(entity, fieldMapping.getValue(Value.of(attributeConverted)));
            }, () -> fieldMapping.write(entity, fieldMapping.getValue(Value.of(vertexId))));
        }
    }

    private <T> T toEntity(Class<T> type, List<Property> properties) {
        EntityMetadata mapping = getEntities().get(type);
        T instance = mapping.newInstance();
        return convertEntity(properties, mapping, instance);
    }

    private <T> T convertEntity(List<Property> elements, EntityMetadata mapping, T instance) {

        Map<String, FieldMapping> fieldsGroupByName = mapping.getFieldsGroupByName();
        List<String> names = elements.stream()
                .map(Property::key)
                .sorted()
                .collect(toList());
        Predicate<String> existField = k -> Collections.binarySearch(names, k) >= 0;

        fieldsGroupByName.keySet().stream()
                .filter(existField.or(k -> EMBEDDED.equals(fieldsGroupByName.get(k).getType())))
                .forEach(feedObject(instance, elements, fieldsGroupByName));

        return instance;
    }

    private <T> Consumer<String> feedObject(T instance, List<Property> elements,
                                            Map<String, FieldMapping> fieldsGroupByName) {
        return k -> {
            Optional<Property> element = elements
                    .stream()
                    .filter(c -> c.key().equals(k))
                    .findFirst();

            FieldMapping field = fieldsGroupByName.get(k);
            if (EMBEDDED.equals(field.getType())) {
                embeddedField(instance, elements, field);
            } else {
                element.ifPresent(e -> singleField(instance, e, field));
            }
        };
    }

    private <T, X, Y> void singleField(T instance, Property element, FieldMapping field) {
        Object value = element.value();
        Optional<Class<? extends AttributeConverter<X, Y>>> converter = field.getConverter();
        if (converter.isPresent()) {
            AttributeConverter<X, Y> attributeConverter = getConverters().get(converter.get());
            Object attributeConverted = attributeConverter.convertToEntityAttribute((Y) value);
            field.write(instance, field.getValue(Value.of(attributeConverted)));
        } else {
            field.write(instance, field.getValue(Value.of(value)));
        }
    }

    private <T> void embeddedField(T instance, List<Property> elements,
                                   FieldMapping field) {
        field.write(instance, toEntity(field.getNativeField().getType(), elements));
    }

    protected FieldGraph to(FieldMapping field, Object entityInstance) {
        Object value = field.read(entityInstance);
        return FieldGraph.of(value, field);
    }

    private <T> T mapInheritanceEntity(Vertex vertex,
                                       List<Property> properties, Class<?> type) {

        Map<String, InheritanceMetadata> group = getEntities()
                .findByParentGroupByDiscriminatorValue(type);

        if (group.isEmpty()) {
            throw new MappingException("There is no discriminator inheritance to the vertex "
                    + vertex.label());
        }
        String column = group.values()
                .stream()
                .findFirst()
                .map(InheritanceMetadata::getDiscriminatorColumn)
                .orElseThrow();


        String discriminator = properties.stream().filter(p -> p.key().equals(column))
                .map(Property::value).map(Object::toString)
                .findFirst()
                .orElseThrow(
                        () -> new MappingException("To inheritance there is the discriminator column missing" +
                                " on the Vertex, the document name: " + column));

        InheritanceMetadata inheritance = Optional.ofNullable(group.get(discriminator))
                .orElseThrow(() -> new MappingException("There is no inheritance map to the discriminator" +
                        " column value " + discriminator));

        EntityMetadata mapping = getEntities().get(inheritance.getEntity());
        return toEntity((Class<T>) mapping.getType(), properties);
    }
}
