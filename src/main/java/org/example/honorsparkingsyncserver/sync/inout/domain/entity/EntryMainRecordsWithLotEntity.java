package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "entry_main_records")
public class EntryMainRecordsWithLotEntity {

    @Id
    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "entry_photo")
    private String entryPhoto;

    @Column(name = "entry_lot_id")
    private String entryLotId;

    @Column(name = "parking_status")
    private String parkingStatus;

    @ManyToOne
    @JoinColumn(name = "entry_lot_id", referencedColumnName = "parking_lot_code", insertable = false, updatable = false)
    private ParkingLotEntity parkingLot;
}
