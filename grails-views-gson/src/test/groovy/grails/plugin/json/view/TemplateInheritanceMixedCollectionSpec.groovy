/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * A row of a collection must render its own entity through every inherited template, also when
 * earlier rows of the same collection were other subclasses of the same superclass.
 *
 * The collection loop of DefaultGrailsJsonViewHelper.render(Map) reuses one model map for all rows,
 * so a row's binding still holds the earlier rows' entities under their class names. Upstream
 * prepareParentWritable() copied them into the parent model with put/putAll, replacing the row
 * passed by `inherits model:` and the row stored under its superclass name.
 */
class TemplateInheritanceMixedCollectionSpec extends Specification implements JsonViewTest {

    private static final List<ZooAnimal> ANIMALS = [new ZooDog(name: 'A'), new ZooCat(name: 'B'), new ZooDog(name: 'C')]
    private static final String EXPECTED = '[{"name":"A","kind":"ZooDog"},{"name":"B","kind":"ZooCat"},{"name":"C","kind":"ZooDog"}]'

    void "inherits keeps its explicit model when an earlier row was another subclass"() {
        when:
        def result = render(template: 'zooExplicit', model: [animals: ANIMALS])

        then:
        result.jsonText == EXPECTED
    }

    void "inherits without a model gets the row's own entity under the superclass name"() {
        when:
        def result = render(template: 'zooImplicit', model: [animals: ANIMALS])

        then:
        result.jsonText == EXPECTED
    }

    void "inherits keeps its explicit model when the key is the class name of an earlier row"() {
        // Row 2 is a ZooPuppy, whose explicit `zooDog` key is also the class-name key under which
        // row 1 (a ZooDog) is still in the binding. Copying the binding with putAll replaces it.
        when:
        def result = render(template: 'zooDogs', model: [dogs: [new ZooDog(name: 'A'), new ZooPuppy(name: 'B')]])

        then:
        result.jsonText == '[{"name":"A","kind":"ZooDog"},{"name":"B","kind":"ZooPuppy"}]'
    }
}

abstract class ZooAnimal {
    String name
}

class ZooDog extends ZooAnimal {
}

class ZooCat extends ZooAnimal {
}

class ZooPuppy extends ZooDog {
}
