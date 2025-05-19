package org.example.honorsparkingsyncserver.sync.inout.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "parking_lots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLotEntity implements Serializable {

    @Id
    @Column(name = "parking_lot_id")
    private Integer parkingLotId;

    @Column(name = "parking_lot_code", length = 10)
    private String parkingLotCode;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "business_number", length = 30)
    private String businessNumber;

    @Column(name = "business_owner", length = 30)
    private String businessOwner;

    @Column(name = "business_address", length = 200)
    private String businessAddress;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "occupied_spaces")
    private Integer occupiedSpaces;

    @Column(name = "comments", length = 300)
    private String comments;
}
