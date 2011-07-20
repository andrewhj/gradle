/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.tooling.model.internal;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.util.GUtil;

import java.io.Serializable;
import java.util.*;

public class ImmutableDomainObjectSet<T> extends AbstractSet<T> implements DomainObjectSet<T>, Serializable {
    private final Set<T> elements = new LinkedHashSet<T>();

    public ImmutableDomainObjectSet(Iterable<? extends T> elements) {
        GUtil.addToCollection(this.elements, elements);
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    public T getAt(int index) throws IndexOutOfBoundsException {
        return getAll().get(index);
    }

    public List<T> getAll() {
        return new ArrayList<T>(elements);
    }
}
