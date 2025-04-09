package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
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

  @Column(name = "vehicle_number")
  private String vehicleNumber;

  @Column(name = "rfid_number")
  private String rfidNumber;

  @Column(name = "exit_time")
  private LocalDateTime exitTime;

  @Column(name = "exit_photo")
  private String exitPhoto;  // 사진 파일명

  @Column(name = "exit_lot_id")
  private String exitLotId;

  @Column(name = "exit_device_code")
  private String exitDeviceCode;

  @Column(name = "exit_type")
  private String exitType;

  @Column(name = "exit_type_id")
  private Long exitTypeId;

  @Column(name = "exit_result_type")
  private String exitResultType;

  @Column(name = "exit_status")
  private String exitStatus;

  @Column(name = "parking_minutes")
  private Integer parkingMinutes;

  @OneToOne
  @JoinColumn(name = "entry_id", referencedColumnName = "entry_id")
  private EntryMainRecordsEntity entryMainRecord;
}
