/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.diana.api.document;


import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

/**
 * A default implementation of {@link DocumentCollectionEntity}
 */
final class DefaultDocumentCollectionEntity implements DocumentCollectionEntity {

    private final List<Document> documents = new ArrayList<>();

    private final String name;


    DefaultDocumentCollectionEntity(String name) {
        this.name = Objects.requireNonNull(name, "name name is required");
    }


    public String getName() {
        return name;
    }

    public boolean remove(String documentName) {
        return documents.removeIf(document -> document.getName().equals(documentName));
    }

    public List<Document> getDocuments() {
        return unmodifiableList(documents);
    }

    public void add(Document document) {
        Objects.requireNonNull(document, "Document is required");
        documents.add(document);
    }

    @Override
    public void addAll(Iterable<Document> documents) {
        Objects.requireNonNull(documents, "documents are required");
        documents.forEach(this.documents::add);
    }

    @Override
    public Optional<Document> find(String name) {
        return documents.stream().filter(document -> document.getName().equals(name)).findFirst();
    }

    @Override
    public int size() {
        return documents.size();
    }

    @Override
    public boolean isEmpty() {
        return documents.isEmpty();
    }

    @Override
    public DocumentCollectionEntity copy() {
        DefaultDocumentCollectionEntity entity = new DefaultDocumentCollectionEntity(this.name);
        entity.documents.addAll(this.getDocuments());
        return entity;
    }

    @Override
    public Map<String, Object> toMap() {
        return documents.stream().collect(Collectors.toMap(Document::getName, document -> document.getValue().get()));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentCollectionEntity)) {
            return false;
        }
        DocumentCollectionEntity that = (DocumentCollectionEntity) o;
        return Objects.equals(documents.stream().sorted(comparing(Document::getName)).collect(Collectors.toList()),
                that.getDocuments().stream().sorted(comparing(Document::getName)).collect(Collectors.toList())) &&
                Objects.equals(name, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(documents, name);
    }
}
