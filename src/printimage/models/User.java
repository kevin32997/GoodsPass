/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.models;

import java.sql.Timestamp;

/**
 *
 * @author Admin
 */
public class User {

    public static final int USER_ADMIN = 0;
    public static final int USER_ENCODER = 1;

    private int id;
    private String username;
    private String password;
    private int usertype;
    private String text_usertype;
    private String fullname;

    private Timestamp dateCreated;
    private Timestamp dateUpdated;

    public User() {
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUsertype() {
        return usertype;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Timestamp getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Timestamp dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getText_usertype() {
        return text_usertype;
    }

    public void setText_usertype(String text_usertype) {
        this.text_usertype = text_usertype;
    }
    
    

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username=" + username + ", password=" + password + ", usertype=" + usertype + ", fullname=" + fullname + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + '}';
    }


 
}
