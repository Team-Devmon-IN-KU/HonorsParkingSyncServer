package org.example.honorsparkingsyncserver.common.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {
    "org.example.honorsparkingsyncserver.sync.inout.repository"})
public class MongoConfig {

  @Value("${spring.data.mongodb.uri}")
  private String MongoURI;


  @Bean
  public MongoClient mongoClient() {
    return MongoClients.create(
        MongoURI);
  }

//  @Bean
//  public MongoTemplate mongoTemplate(MongoClient mongoClient) {
//    return new MongoTemplate(mongoClient, "your_database_name");
//  }

}
