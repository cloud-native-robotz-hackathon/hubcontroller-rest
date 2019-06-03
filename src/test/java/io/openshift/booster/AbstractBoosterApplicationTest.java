/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openshift.booster;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public abstract class AbstractBoosterApplicationTest {

    private static final String GREETING_PATH = "api/robot";

    @Test
    public void testRobotEndpointPing() {
        given()
           .baseUri(baseURI())
           .get(GREETING_PATH)
           .then()
           .statusCode(200);
    }

    @Test
    public void testRobotEndpointPower() {
        given()
           .baseUri(baseURI())
           .get(GREETING_PATH + "/power")
           .then()
           .statusCode(200);
    }

    @Test
    public void testRobotEndpointVolt() {
        given()
           .baseUri(baseURI())
           .get(GREETING_PATH + "/volt")
           .then()
           .statusCode(200);
    }

    

    protected abstract String baseURI();
}
