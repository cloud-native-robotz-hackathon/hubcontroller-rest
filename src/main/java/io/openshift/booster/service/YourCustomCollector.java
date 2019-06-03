package io.openshift.booster.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class YourCustomCollector extends Collector {

  @Value("${edge.controller.uri}")
  private String edgeControllerEndpoint;

  // private final static String ROBOT_ENDPOINT = "http://localhost:8089";
  private RestTemplate restTemplate = new RestTemplate();

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    // With no labels.

    
    System.out.println("Running Prometheus Collector");

    for (Map.Entry<String, String> entry : getRobotIPs().entrySet()) {
      
      System.out.println("Setting gauge value for ip -> " + entry.getValue());

      mfs.add(new GaugeMetricFamily("robot_gauge_" + entry.getKey(), "Current power of robot",
      Integer.valueOf(restTemplate.getForObject("http://" + entry.getValue() + ":5000/power", String.class))));

    }
    
    return mfs;
  }

  private Map<String, String> getRobotIPs() {
    String apiTokenMap = System.getenv().getOrDefault("MAP", "{}");

    System.out.println("Token map raw -> " + apiTokenMap);

    ObjectReader reader = new ObjectMapper().readerFor(Map.class);

    Map<String, String> robotIpMap = new HashMap<>();

    try {
      robotIpMap = reader.readValue(apiTokenMap);

      System.out.println("Token Map map -> " + robotIpMap);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return robotIpMap;

  }

}