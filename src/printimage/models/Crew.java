/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.models;

/**
 *
 * @author Admin
 */
public class Crew {
    
    private int id;
    private String gpNo;
    private String fullname;
    private String address;
    private String designation;
    private String idPresented;
    private String idNumber;
    
    public Crew(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGpNo() {
        return gpNo;
    }

    public void setGpNo(String gpNo) {
        this.gpNo = gpNo;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdPresented() {
        return idPresented;
    }

    public void setIdPresented(String idPresented) {
        this.idPresented = idPresented;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    
    
}
