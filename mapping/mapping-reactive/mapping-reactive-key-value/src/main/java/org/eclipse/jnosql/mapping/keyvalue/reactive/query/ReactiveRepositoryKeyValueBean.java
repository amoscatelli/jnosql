/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.keyvalue.reactive.query;

import jakarta.nosql.mapping.DatabaseType;
import jakarta.nosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.keyvalue.reactive.ReactiveKeyValueTemplate;
import org.eclipse.jnosql.mapping.reactive.ReactiveRepository;
import org.eclipse.jnosql.mapping.spi.AbstractBean;
import org.eclipse.jnosql.mapping.util.AnnotationLiteralUtil;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Artemis discoveryBean to CDI extension to register {@link KeyValueTemplate}
 */
public class ReactiveRepositoryKeyValueBean extends AbstractBean<ReactiveRepository<?,?>> {

    private final Class<?> type;

    private final Set<Type> types;

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     *
     * @param type        the tye
     * @param beanManager the beanManager
     * @param provider    the provider name, that must be a
     */
    public ReactiveRepositoryKeyValueBean(Class<?> type, BeanManager beanManager, String provider) {
        super(beanManager);
        this.type = type;
        this.types = Collections.singleton(type);
        this.provider = provider;
        if (provider.isEmpty()) {
            this.qualifiers = new HashSet<>();
            qualifiers.add(DatabaseQualifier.ofKeyValue());
            qualifiers.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
            qualifiers.add(AnnotationLiteralUtil.ANY_ANNOTATION);
        } else {
            this.qualifiers = Collections.singleton(DatabaseQualifier.ofKeyValue(provider));
        }

    }

    @Override
    public Class<?> getBeanClass() {
        return type;
    }


    @Override
    public ReactiveRepository<?,?> create(CreationalContext<ReactiveRepository<?,?>> creationalContext) {
        KeyValueTemplate template = provider.isEmpty() ? getInstance(KeyValueTemplate.class) :
                getInstance(KeyValueTemplate.class, DatabaseQualifier.ofKeyValue(provider));

        ReactiveKeyValueTemplate reactiveTemplate = provider.isEmpty() ? getInstance(ReactiveKeyValueTemplate.class) :
                getInstance(ReactiveKeyValueTemplate.class, DatabaseQualifier.ofKeyValue(provider));
        ReactiveKeyValueRepositoryProxy<?> handler = new ReactiveKeyValueRepositoryProxy<>(template, reactiveTemplate, type);
        return (ReactiveRepository<?,?>) Proxy.newProxyInstance(type.getClassLoader(),
                new Class[]{type},
                handler);
    }


    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String getId() {
        return type.getName() + '@' + DatabaseType.KEY_VALUE + "-" + provider;
    }

}