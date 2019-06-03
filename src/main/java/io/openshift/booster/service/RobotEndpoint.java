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
package io.openshift.booster.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import io.prometheus.client.Gauge;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

@Path("/robot")
@Api(value = "Robot Api")
@Component
public class RobotEndpoint {

    // private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final Gauge powerMetric = Gauge.build().name("Robot_Power").labelNames("power")
            .help("Current voltage of robot").register();

    private static final Gauge distanceMetric = Gauge.build().name("Robot_Distance").labelNames("distance")
            .help("Current distance to next object").register();

    // @Value("${edge.controller.uri}")
    //private String edgeControllerEndpoint;

    //@Value("#{${robots}}")
    //private Map<String, String> robotIpMapping;
    private RestTemplate restTemplate = new RestTemplate();

    private RobotEndpoint() {
       /* powerMetric.setChild(new Gauge.Child() {
            public double get() {

                String response = restTemplate.getForObject(edgeControllerEndpoint + "/power", String.class);
                return Double.valueOf(response);
            }
        }, "power");

        distanceMetric.setChild(new Gauge.Child() {
            public double get() {

                String response = restTemplate.getForObject(edgeControllerEndpoint + "/distance", String.class);
                return Double.valueOf(response);
            }
        }, "distance");

        */
    }

    @GET
    @Path("/status")
    @ApiOperation(value = "Checks the status of the API")
    @Produces("text/html")
    @ResponseBody

    public Object status(@Context HttpHeaders headers,
            @ApiParam(value = "User Key", required = true) @QueryParam("user_key") String userKey) {

        System.out.println(userKey + ": Status called");

        return "OK";
    }

    @GET
    @Path("/remote_status")
    @ApiOperation(value = "Checks the status of connected robot")
    @Produces("text/html")
    public Object remoteStatus(@Context HttpHeaders headers,
            @ApiParam(value = "User Key", required = true) @QueryParam("user_key") String userKey) {

        System.out.println(userKey + ": Remote Status called");

        String response = restTemplate.getForObject(getRobotURLFromConfigMap(userKey), String.class);
        return response;
    }

    @POST
    @Path("/forward/{length_in_cm}")
    @ApiOperation(value = "Drives the robot forward by the indicated cm")
    @Produces("text/html")
    public Object forward(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("length_in_cm") Integer lengthInCm) {

        System.out.println(userKey + ": forward called -> " + lengthInCm);

        HttpEntity<String> request = new HttpEntity<>(new String(""));
        String response = restTemplate.postForObject(getRobotURLFromConfigMap(userKey) + "/forward/" + lengthInCm,
                request, String.class);
        return response;
    }

    @POST
    @Path("/backward/{length_in_cm}")
    @ApiOperation(value = "Drives the robot backward by the indicated cm")
    @Produces("text/html")
    public Object backward(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("length_in_cm") Integer lengthInCm) {

        System.out.println(userKey + ": backward called -> " + lengthInCm);

        HttpEntity<String> request = new HttpEntity<>(new String(""));
        String response = restTemplate.postForObject(getRobotURLFromConfigMap(userKey) + "/backward/" + lengthInCm,
                request, String.class);
        return response;
    }

    @POST
    @Path("/left/{degrees}")
    @ApiOperation(value = "Turns the robot left by the indicated degrees (positive)")
    @Produces("text/html")
    public Object left(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("degrees") Integer degrees) {

        System.out.println(userKey + ": left called -> " + degrees);

        HttpEntity<String> request = new HttpEntity<>(new String(""));
        String response = restTemplate.postForObject(getRobotURLFromConfigMap(userKey) + "/left/" + degrees, request,
                String.class);
        return response;
    }

    @POST
    @Path("/right/{degrees}")
    @ApiOperation(value = "Turns the robot right by the indicated degrees")
    @Produces("text/html")
    public Object right(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("degrees") Integer degrees) {

        System.out.println(userKey + ": right called -> " + degrees);

        HttpEntity<String> request = new HttpEntity<>(new String(""));
        String response = restTemplate.postForObject(getRobotURLFromConfigMap(userKey) + "/right/" + degrees, request,
                String.class);
        return response;
    }

    @POST
    @Path("/reset")
    @ApiOperation(value = "Resets all sensors and motors")
    @Produces("text/html")
    public Object reset(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers) {

        System.out.println(userKey + ": reset called");

        HttpEntity<String> request = new HttpEntity<>(new String(""));
        String response = restTemplate.postForObject(getRobotURLFromConfigMap(userKey) + "/reset", request,
                String.class);
        return response;
    }

    @POST
    @Path("/servo/{degrees}")
    @ApiOperation(value = "Turns the robot servo by the indicated degrees")
    @Produces("text/html")
    public Object servo(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("degrees") Integer degrees) {

        System.out.println(userKey + ": servo called -> " + degrees);

        HttpEntity<String> request = new HttpEntity<>(new String(""));
        String response = restTemplate.postForObject(getRobotURLFromConfigMap(userKey) + "/servo/" + degrees, request,
                String.class);
        return response;
    }

    @GET
    @Path("/power")
    @ApiOperation(value = "Checks the current voltage of the robot battery")
    @Produces("text/html")
    public Object power(@Context HttpHeaders headers,
            @ApiParam(value = "User Key", required = true) @QueryParam("user_key") String userKey) {

        System.out.println(userKey + ": power called");

        String response = restTemplate.getForObject(getRobotURLFromConfigMap(userKey) + "/power", String.class);

        return response;
    }

    @GET
    @Path("/distance")
    @ApiOperation(value = "Checks the current distance to the next object in mm")
    @Produces("text/html")
    public Object distance(@Context HttpHeaders headers,
            @ApiParam(value = "User Key", required = true) @QueryParam("user_key") String userKey) {

        System.out.println(userKey + ": distance called");

        String response = restTemplate.getForObject(getRobotURLFromConfigMap(userKey) + "/distance", String.class);
        return response;
    }

  /*  private String getRobotIpFromProperties(String robotId) {
        System.out.println("IP Map -> " + robotIpMapping);
        System.out.println("robotId -> " + robotId);
        System.out.println("got -> " + robotIpMapping.get(robotId));

        return "http://" + robotIpMapping.get(robotId) + ":5000";
    }
    */

    private String getRobotURLFromConfigMap(String token) {

        String apiTokenMap = System.getenv().getOrDefault("MAP", "{}");

        System.out.println("Token Map raw -> " + apiTokenMap);

        ObjectReader reader = new ObjectMapper().readerFor(Map.class);

        String robotIp = null;

        try {
            Map<String, String> map = reader.readValue(apiTokenMap);

            System.out.println("Token Map map -> " + map);

            System.out.println("Token -> " + token);

            robotIp = map.get(token);

            System.out.println("Got IP -> " + robotIp);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "http://" + robotIp + ":5000";
    }

    

    private String getRobotURLFromHeaders(HttpHeaders headers) {

        String token = getToken(headers);

        return getRobotURLFromConfigMap(token);
    }

    private String getToken(HttpHeaders headers) {

        System.out.println("Headers -> " + headers.getRequestHeaders());

        String token = headers.getRequestHeader("token").get(0);

        System.out.println("Extracted token -> " + token);

        return token;
    }

    
}
