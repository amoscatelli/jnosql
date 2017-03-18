/*
 * Copyright 2017 Otavio Santana and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.jnosql.diana.api.reader;

import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.TypeReferenceReader;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SetTypeReferenceReaderTest {

    private TypeReferenceReader referenceReader = new SetTypeReferenceReader();

    @Test
    public void shouldBeCompatible() {

        assertTrue(referenceReader.isCompatible(new TypeReference<Set<String>>(){}));
        assertTrue(referenceReader.isCompatible(new TypeReference<Set<Long>>(){}));

    }


    @Test
    public void shouldNotBeCompatible() {
        assertFalse(referenceReader.isCompatible(new TypeReference<String>(){}));
        assertFalse(referenceReader.isCompatible(new TypeReference<List<String>>(){}));
        assertFalse(referenceReader.isCompatible(new TypeReference<List<List<String>>>(){}));
        assertFalse(referenceReader.isCompatible(new TypeReference<Queue<String>>(){}));
        assertFalse(referenceReader.isCompatible(new TypeReference<Map<Integer, String>>(){}));
    }


    @Test
    public void shouldConvert() {
        assertEquals(singleton("123"), referenceReader.convert(new TypeReference<List<String>>(){}, "123"));
        assertEquals(singleton(123L), referenceReader.convert(new TypeReference<List<Long>>(){}, "123"));
    }

}