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
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@EnableJms
@Path("/robot-async")
@Api(value = "Robot Api Async")
@Component
public class RobotEndpointAMQ {

    // private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    private final SimpleMessageConverter converter = new SimpleMessageConverter();

    private RobotEndpointAMQ() {

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

        System.out.println(userKey + ": Async remote Status called");

        return sendAMQMessage("status", "",userKey);

    }

   
    @POST
    @Path("/forward/{length_in_cm}")
    @ApiOperation(value = "Drives the robot forward by the indicated cm")
    @Produces("text/html")
    public Object forward(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("length_in_cm") Integer lengthInCm) {

        System.out.println(userKey + ": forward called -> " + lengthInCm);

        return sendAMQMessage("forward", "" + lengthInCm,userKey);
    }

    @POST
    @Path("/backward/{length_in_cm}")
    @ApiOperation(value = "Drives the robot backward by the indicated cm")
    @Produces("text/html")
    public Object backward(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("length_in_cm") Integer lengthInCm) {

        System.out.println(userKey + ": backward called -> " + lengthInCm);

        return sendAMQMessage("backward", "" + lengthInCm,userKey);
    }

    @POST
    @Path("/left/{degrees}")
    @ApiOperation(value = "Turns the robot left by the indicated degrees (positive)")
    @Produces("text/html")
    public Object left(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("degrees") Integer degrees) {

        System.out.println(userKey + ": left called -> " + degrees);

        return sendAMQMessage("left", "" + degrees,userKey);
    }

    @POST
    @Path("/right/{degrees}")
    @ApiOperation(value = "Turns the robot right by the indicated degrees")
    @Produces("text/html")
    public Object right(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("degrees") Integer degrees) {

        System.out.println(userKey + ": right called -> " + degrees);

        return sendAMQMessage("right", "" + degrees,userKey);

    }

    @POST
    @Path("/reset")
    @ApiOperation(value = "Resets all sensors and motors")
    @Produces("text/html")
    public Object reset(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers) {

        System.out.println(userKey + ": reset called");

        return sendAMQMessage("reset", "",userKey);
    }

    @POST
    @Path("/servo/{degrees}")
    @ApiOperation(value = "Turns the robot servo by the indicated degrees")
    @Produces("text/html")
    public Object servo(@ApiParam(value = "User Key", required = true) @FormParam("user_key") String userKey,
            @Context HttpHeaders headers, @PathParam("degrees") Integer degrees) {

        System.out.println(userKey + ": servo called -> " + degrees);

        return sendAMQMessage("servo", "" + degrees,userKey);
    }

    @GET
    @Path("/power")
    @ApiOperation(value = "Checks the current voltage of the robot battery")
    @Produces("text/html")
    public Object power(@Context HttpHeaders headers,
            @ApiParam(value = "User Key", required = true) @QueryParam("user_key") String userKey) {

        System.out.println(userKey + ": power called");

        return sendAMQMessage("power", "",userKey);
    }

    @GET
    @Path("/distance")
    @ApiOperation(value = "Checks the current distance to the next object in mm")
    @Produces("text/html")
    public Object distance(@Context HttpHeaders headers,
            @ApiParam(value = "User Key", required = true) @QueryParam("user_key") String userKey) {

        System.out.println(userKey + ": distance called");

        return sendAMQMessage("distance", "", userKey);
    }

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
            
            e.printStackTrace();
        }

        return "http://" + robotIp + ":5000";
    }

    private Object sendAMQMessage(String operation, String parameter, String userKey) {

        System.out.println("Sending AMQ Message with params -> " + operation + " " + parameter);
        javax.jms.Message received = null;
        try {
            received = jmsTemplate.sendAndReceive(userKey, new MessageCreator() {

                @Override
                public javax.jms.Message createMessage(Session session) throws JMSException {
                    System.out.println("Creating Message");
                    
                    String msgId = UUID.randomUUID().toString();
                    System.out.println("msgId -> " + msgId);
                    TextMessage message;
                    try {
                        message = session.createTextMessage(createRobotMsg(operation, parameter));
                    } catch (JsonProcessingException e) {
                        
                        e.printStackTrace();
                        throw new JMSException("Error Processing JSON", e.toString());

                    }
                    message.setJMSCorrelationID(msgId);
                    return message;
                }
            });

            System.out.println("Reply received: " + this.converter.fromMessage(received));
            return this.converter.fromMessage(received);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
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

    private String createRobotMsg(String operation, String parameter) throws JsonProcessingException {

        System.out.println("Creating JSON Message with -> " + operation + " " + parameter);
        Map<String, String> map = new HashMap<>();
        map.put("operation", operation);
        map.put("paramter", parameter);

        ObjectMapper mapper = new ObjectMapper();

        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

        System.out.println("JSON -> " + jsonResult );

        return jsonResult;
    }

    
}
