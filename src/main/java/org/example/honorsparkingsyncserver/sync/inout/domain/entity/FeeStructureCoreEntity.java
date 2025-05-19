package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fee_structure_core")
public class FeeStructureCoreEntity {

    @Id
    @Column(name = "fee_code", length = 20)
    private String feeCode;

    @Column(name = "fee_name", length = 30)
    private String feeName;

    @Column(name = "start_time", length = 10)
    private String startTime;

    @Column(name = "end_time", length = 10)
    private String endTime;

    @Column(name = "return_time")
    private Integer returnTime;

    @Column(name = "exit_service_time")
    private Integer exitServiceTime;

    @Column(name = "base_time")
    private Integer baseTime;

    @Column(name = "base_fee")
    private Integer baseFee;

    @Column(name = "unit_time")
    private Integer unitTime;

    @Column(name = "unit_fee")
    private Integer unitFee;

    @Column(name = "daily_max_fee")
    private Integer dailyMaxFee;
}