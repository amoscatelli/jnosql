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
package org.eclipse.jnosql.mapping.column;

import org.eclipse.jnosql.mapping.AttributeConverter;
import org.eclipse.jnosql.communication.column.Column;
import org.eclipse.jnosql.mapping.Converters;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;
import org.eclipse.jnosql.mapping.reflection.MappingType;
import org.eclipse.jnosql.mapping.reflection.FieldValue;
import org.eclipse.jnosql.mapping.reflection.DefaultFieldValue;
import org.eclipse.jnosql.mapping.reflection.GenericFieldMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.jnosql.mapping.reflection.MappingType.COLLECTION;

import static org.eclipse.jnosql.mapping.reflection.MappingType.EMBEDDED;
import static org.eclipse.jnosql.mapping.reflection.MappingType.ENTITY;
import static java.util.Collections.singletonList;

final class DefaultColumnFieldValue implements ColumnFieldValue {

    private final FieldValue fieldValue;

    private DefaultColumnFieldValue(FieldValue fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public Object getValue() {
        return fieldValue.getValue();
    }

    @Override
    public FieldMapping getField() {
        return fieldValue.getField();
    }

    @Override
    public boolean isNotEmpty() {
        return fieldValue.isNotEmpty();
    }

    @Override
    public <X, Y> List<Column> toColumn(ColumnEntityConverter converter, Converters converters) {
        if (EMBEDDED.equals(getType())) {
            return converter.toColumn(getValue()).columns();
        } else if (ENTITY.equals(getType())) {
            return singletonList(Column.of(getName(), converter.toColumn(getValue()).columns()));
        } else if (isEmbeddableCollection()) {
            return singletonList(Column.of(getName(), getColumns(converter)));
        }
        Optional<Class<? extends AttributeConverter<X, Y>>> optionalConverter = getField().getConverter();
        if (optionalConverter.isPresent()) {
            AttributeConverter<X, Y> attributeConverter = converters.get(optionalConverter.get());
            return singletonList(Column.of(getName(), attributeConverter.convertToDatabaseColumn((X) getValue())));
        }
        return singletonList(Column.of(getName(), getValue()));
    }

    private List<List<Column>> getColumns(ColumnEntityConverter converter) {
        List<List<Column>> columns = new ArrayList<>();
        for (Object element : (Iterable) getValue()) {
            columns.add(converter.toColumn(element).columns());
        }
        return columns;
    }

    private boolean isEmbeddableCollection() {
        return COLLECTION.equals(getType()) && isEmbeddableElement();
    }

    private MappingType getType() {
        return getField().getType();
    }

    private String getName() {
        return getField().getName();
    }

    private boolean isEmbeddableElement() {
        return ((GenericFieldMapping) getField()).isEmbeddable();
    }

    @Override
    public String toString() {
        return "ColumnFieldValue{" + "fieldValue=" + fieldValue +
                '}';
    }

    static ColumnFieldValue of(Object value, FieldMapping field) {
        return new DefaultColumnFieldValue(new DefaultFieldValue(value, field));
    }
}
