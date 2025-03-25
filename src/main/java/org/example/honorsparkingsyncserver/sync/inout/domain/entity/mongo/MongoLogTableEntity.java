package org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo;

import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "logTable")
public class MongoLogTableEntity {

  @Id
  private Long id;
  private String lastHash;
  private Boolean isUpdatable;

}
