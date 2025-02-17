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

import org.eclipse.jnosql.communication.column.ColumnDeleteQuery;
import org.eclipse.jnosql.communication.column.ColumnEntity;
import org.eclipse.jnosql.communication.column.ColumnQuery;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.enterprise.event.Event;

import static org.eclipse.jnosql.communication.column.ColumnDeleteQuery.delete;
import static org.eclipse.jnosql.communication.column.ColumnQuery.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ColumnEventPersistManagerTest {


    @InjectMocks
    private ColumnEventPersistManager subject;

    @Mock
    private Event<ColumnEntityPrePersist> columnEntityPrePersistEvent;

    @Mock
    private Event<ColumnEntityPostPersist> columnEntityPostPersistEvent;

    @Mock
    private Event<EntityPrePersist> entityPrePersistEvent;

    @Mock
    private Event<EntityPostPersist> entityPostPersistEvent;

    @Mock
    private Event<EntityColumnPrePersist> entityColumnPrePersist;

    @Mock
    private Event<EntityColumnPostPersist> entityColumnPostPersist;

    @Mock
    private Event<ColumnQueryExecute> columnQueryExecute;

    @Mock
    private Event<ColumnDeleteQueryExecute> columnDeleteQueryExecute;


    @Test
    public void shouldFirePreColumn() {
        ColumnEntity entity = ColumnEntity.of("columnFamily");
        subject.firePreColumn(entity);
        ArgumentCaptor<ColumnEntityPrePersist> captor = ArgumentCaptor.forClass(ColumnEntityPrePersist.class);
        verify(columnEntityPrePersistEvent).fire(captor.capture());

        ColumnEntityPrePersist captorValue = captor.getValue();
        assertEquals(entity, captorValue.get());
    }


    @Test
    public void shouldFirePostColumn() {
        ColumnEntity entity = ColumnEntity.of("columnFamily");
        subject.firePostColumn(entity);
        ArgumentCaptor<ColumnEntityPostPersist> captor = ArgumentCaptor.forClass(ColumnEntityPostPersist.class);
        verify(columnEntityPostPersistEvent).fire(captor.capture());

        ColumnEntityPostPersist captorValue = captor.getValue();
        assertEquals(entity, captorValue.get());
    }

    @Test
    public void shouldFirePreEntity() {
        Jedi jedi = new Jedi();
        jedi.name = "Luke";
        subject.firePreEntity(jedi);
        ArgumentCaptor<EntityPrePersist> captor = ArgumentCaptor.forClass(EntityPrePersist.class);
        verify(entityPrePersistEvent).fire(captor.capture());
        EntityPrePersist value = captor.getValue();
        assertEquals(jedi, value.get());
    }

    @Test
    public void shouldFirePostEntity() {
        Jedi jedi = new Jedi();
        jedi.name = "Luke";
        subject.firePostEntity(jedi);
        ArgumentCaptor<EntityPostPersist> captor = ArgumentCaptor.forClass(EntityPostPersist.class);
        verify(entityPostPersistEvent).fire(captor.capture());
        EntityPostPersist value = captor.getValue();
        assertEquals(jedi, value.get());
    }

    @Test
    public void shouldFirePreColumnEntity() {
        Jedi jedi = new Jedi();
        jedi.name = "Luke";
        subject.firePreColumnEntity(jedi);
        ArgumentCaptor<EntityColumnPrePersist> captor = ArgumentCaptor.forClass(EntityColumnPrePersist.class);
        verify(entityColumnPrePersist).fire(captor.capture());
        EntityColumnPrePersist value = captor.getValue();
        assertEquals(jedi, value.get());
    }

    @Test
    public void shouldFirePostColumnEntity() {
        Jedi jedi = new Jedi();
        jedi.name = "Luke";
        subject.firePostColumnEntity(jedi);
        ArgumentCaptor<EntityColumnPostPersist> captor = ArgumentCaptor.forClass(EntityColumnPostPersist.class);
        verify(entityColumnPostPersist).fire(captor.capture());
        EntityColumnPostPersist value = captor.getValue();
        assertEquals(jedi, value.get());
    }

    @Test
    public void shouldFirePreQuery() {

        ColumnQuery query = select().from("person").build();
        subject.firePreQuery(query);
        ArgumentCaptor<ColumnQueryExecute> captor = ArgumentCaptor.forClass(ColumnQueryExecute.class);
        verify(columnQueryExecute).fire(captor.capture());
        assertEquals(query, captor.getValue().get());
    }

    @Test
    public void shouldFirePreDeleteQuery() {

        ColumnDeleteQuery query = delete().from("person").build();
        subject.firePreDeleteQuery(query);
        ArgumentCaptor<ColumnDeleteQueryExecute> captor = ArgumentCaptor.forClass(ColumnDeleteQueryExecute.class);
        verify(columnDeleteQueryExecute).fire(captor.capture());
        assertEquals(query, captor.getValue().get());
    }


    static class Jedi {
        private String name;
    }
}