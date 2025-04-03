package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "exit_main_records")  // MSSQL 테이블명
public class ExitMainRecordEntity {

  @Id
  private Long exitId;  // 기본 키

  private String vehicleNumber;
  private String rfidNumber;

  private LocalDateTime exitTime;

  private String exitPhoto;  // 사진 파일명

  private Long exitLotId;
  private String exitDeviceCode;
  private String exitType;
  private Long exitTypeId;
  private String exitResultType;
  private String exitStatus;

  private Integer parkingMinutes;

  @OneToOne
  @JoinColumn(name = "entry_id", referencedColumnName = "entry_id")
  private EntryMainRecordsEntity entryMainRecord;
}
