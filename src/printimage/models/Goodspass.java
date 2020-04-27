/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 * @author Admin
 */
public class Goodspass {

    // Pass Info
    private int id;
    private String gpNo;

    // Vehicle Info
    private String vehicleDesc;
    private String vehiclePlateNo;

    // Business Info
    private String businessId;
    private String businessName;
    private String businessAddress;

    private String status;
    private String datePrinted;
    private String dateCreated;

    private Timestamp sqlDatePrinted;
    private Timestamp sqlDateCreated;

    public Goodspass() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVehicleDesc() {
        return vehicleDesc;
    }

    public void setVehicleDesc(String vehicleDesc) {
        this.vehicleDesc = vehicleDesc;
    }

    public String getVehiclePlateNo() {
        return vehiclePlateNo;
    }

    public void setVehiclePlateNo(String vehiclePlateNo) {
        this.vehiclePlateNo = vehiclePlateNo;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatePrinted() {
        return datePrinted;
    }

    public void setDatePrinted(String date_printed) {
        this.datePrinted = date_printed;
    }

    public String getGpNo() {
        return gpNo;
    }

    public void setGpNo(String gpNo) {
        this.gpNo = gpNo;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Timestamp getSqlDatePrinted() {
        return sqlDatePrinted;
    }

    public void setSqlDatePrinted(Timestamp sqlDatePrinted) {
        this.sqlDatePrinted = sqlDatePrinted;
    }

    public Timestamp getSqlDateCreated() {
        return sqlDateCreated;
    }

    public void setSqlDateCreated(Timestamp sqlDateCreated) {
        this.sqlDateCreated = sqlDateCreated;
    }

}
