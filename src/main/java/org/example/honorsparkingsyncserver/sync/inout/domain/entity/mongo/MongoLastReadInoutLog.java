package org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo;

import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "lastReadInoutLog")
public class MongoLastReadInoutLog {

  @Id
  private Long id;  // MongoDB에서 자동 생성되는 _id가 있지만, 만약 별도의 필드로 관리하고 싶으면 사용
  private Long lastReadEntityId;  // 마지막으로 읽은 entryId
}
