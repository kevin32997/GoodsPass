/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.models;

import java.sql.Date;

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
    private String date_printed;
    private Date date_sql;

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

    public String getDate_printed() {
        return date_printed;
    }

    public void setDate_printed(String date_printed) {
        this.date_printed = date_printed;
    }

    public Date getDate_sql() {
        return date_sql;
    }

    public void setDate_sql(Date date_sql) {
        this.date_sql = date_sql;
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

    @Override
    public String toString() {
        return "Goodspass{" + "id=" + id + ", gpNo=" + gpNo + ", vehicleDesc=" + vehicleDesc + ", vehiclePlateNo=" + vehiclePlateNo + ", businessId=" + businessId + ", businessName=" + businessName + ", businessAddress=" + businessAddress + ", status=" + status + ", date_printed=" + date_printed + ", date_sql=" + date_sql + '}';
    }

}
