package org.example.honorsparkingsyncserver.sync.unit.repository.mongo;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class InitMongoRepositoryTest {

  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

  @BeforeAll
  public static void setProperties() {
    System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
  }
}
