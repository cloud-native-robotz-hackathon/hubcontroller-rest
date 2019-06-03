package io.openshift.booster;

import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeaturesConfig {

  @Value("${cxf.path}")
  private String basePath;

  @Bean("swagger2Feature")
  public Feature swagger2Feature() {
    Swagger2Feature result = new Swagger2Feature();
    result.setTitle("Robot API");
    result.setDescription("RESTful API to control your robot");
    result.setBasePath(this.basePath);
    result.setVersion("v1");    
    result.setSchemes(new String[] { "https" });
    result.setHost("<HOSTNAME>");
    result.setPrettyPrint(true);
    return result;
  }
}