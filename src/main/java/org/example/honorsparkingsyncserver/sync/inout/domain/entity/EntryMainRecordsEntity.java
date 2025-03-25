package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "entry_main_records")  // MSSQL 테이블명
public class EntryMainRecordsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment
  @Column(name = "entry_id")
  private Long entryId;

  @Column(name = "vehicle_number")
  private String vehicleNumber;

  @Column(name = "rfid_number")
  private String rfidNumber;

  @Column(name = "entry_time")
  private LocalDateTime entryTime;

  @Column(name = "entry_photo")
  private String entryPhoto;

  @Column(name = "entry_lot_id")
  private String entryLotId;

  @Column(name = "entry_device_code")
  private String entryDeviceCode;

  @Column(name = "entry_type")
  private String entryType;

  @Column(name = "entry_type_id")
  private Integer entryTypeId;

  @Column(name = "entry_result_type")
  private String entryResultType;

  @Column(name = "entry_fee_code")
  private String entryFeeCode;

  @Column(name = "parking_status")
  private String parkingStatus;
}
