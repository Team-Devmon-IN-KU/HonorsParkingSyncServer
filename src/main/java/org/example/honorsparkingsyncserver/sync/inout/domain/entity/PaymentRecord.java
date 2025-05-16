package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "payment_records")  // MSSQL 테이블명
public class PaymentRecord {

  @Id
  @Column(name = "payment_id")
  private Long paymentId;  // 기본 키

  @Column(name = "payment_time")
  private LocalDateTime paymentTime;

  @Column(name = "calculated_minutes")
  private Integer calculatedMinutes;

  @Column(name = "initial_fee")
  private Integer initialFee;

  @Column(name = "discount_fee")
  private Integer discountFee;

  @Column(name = "payment_fee")
  private Integer paymentFee;

  @Column(name = "fee_code")
  private String feeCode;

  @Column(name = "discount_applied")
  private Boolean discountApplied;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Column(name = "payment_type")
  private String paymentType;

  @Column(name = "payment_status")
  private String paymentStatus;

  @Column(name = "pay_device_code")
  private String payDeviceCode;

  @ManyToOne
  @JoinColumn(name = "entry_id", referencedColumnName = "entry_id")
  private EntryMainRecordsEntity entryMainRecord;  // 출입 기록과 연관 관계
}
