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
package org.eclipse.jnosql.mapping.keyvalue.spi;

import jakarta.nosql.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.Convert;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueWorkflow;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.reflection.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.test.entities.Person;
import org.eclipse.jnosql.mapping.test.entities.User;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddPackages(value = {Convert.class, KeyValueWorkflow.class})
@AddPackages(MockProducer.class)
@AddExtensions({EntityMetadataExtension.class, KeyValueExtension.class})
public class KeyValueExtensionTest {

    @Inject
    private KeyValueTemplate template;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE, provider = "keyvalueMock")
    private KeyValueTemplate templateMock;

    @Inject
    private UserRepository userRepository;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE)
    private UserRepository userRepositoryDefault;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE, provider = "keyvalueMock")
    private UserRepository userRepositoryMock;

    @Test
    public void shouldUseMock() {
        Person person = template.get(10L, Person.class).get();

        Person personMock = templateMock.get(10L, Person.class).get();

        assertEquals("Default", person.getName());
        assertEquals("keyvalueMock", personMock.getName());

    }


    @Test
    public void shouldUseRepository() {
        User user = userRepository.findById("user").get();
        User userDefault = userRepositoryDefault.findById("user").get();
        User userMock = userRepositoryMock.findById("user").get();
        assertEquals("Default", user.getName());
        assertEquals("Default", userDefault.getName());
        assertEquals("keyvalueMock", userMock.getName());
    }

}