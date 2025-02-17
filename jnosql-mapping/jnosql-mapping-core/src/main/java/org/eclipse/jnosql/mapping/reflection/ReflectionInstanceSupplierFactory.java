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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Constructor;

@ApplicationScoped
class ReflectionInstanceSupplierFactory implements InstanceSupplierFactory {

    private Reflections reflections;

    @Inject
    ReflectionInstanceSupplierFactory(Reflections reflections) {
        this.reflections = reflections;
    }

    ReflectionInstanceSupplierFactory() {
    }

    @Override
    public InstanceSupplier apply(Constructor<?> constructor) {
        return () -> reflections.newInstance(constructor);
    }
}
