package com.mfexpress.rent.deliver.dto.data.deliver;


import lombok.Data;

@Data
public class DeliverVehicleMqDTO {

    private Integer carId;

    private Integer selectStatus;

    private Integer insuranceStatus;

    private Double mileage;

    private Double vehicleAge;

    private String carNum;

}
