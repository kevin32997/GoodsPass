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
public class Passes {

    private String businessName;
    private String address;
    private String plateNo;
    private String description;
    private String designation_1;
    private String designation_2;
    private String qrCode;
    private String ctrlNo;
    private String status;
    private String date_printed;

    

    public Passes() {
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDesignation_1() {
        return designation_1;
    }

    public void setDesignation_1(String designation_1) {
        this.designation_1 = designation_1;
    }

    public String getDesignation_2() {
        return designation_2;
    }

    public void setDesignation_2(String designation_2) {
        this.designation_2 = designation_2;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getCtrlNo() {
        return ctrlNo;
    }

    public void setCtrlNo(String ctrlNo) {
        this.ctrlNo = ctrlNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Passes{" + "businessName=" + businessName + ", address=" + address + ", plateNo=" + plateNo + ", description=" + description + ", designation_1=" + designation_1 + ", designation_2=" + designation_2 + ", qrCode=" + qrCode + ", ctrlNo=" + ctrlNo + ", status=" + status + ", date_printed=" + date_printed + '}';
    }

}
