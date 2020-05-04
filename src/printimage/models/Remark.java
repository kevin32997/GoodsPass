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
public class Remark {

    // Static fields
    public static final int REMARK_CREATE = 0;
    public static final int REMARK_UPDATE = 1;
    public static final int REMARK_PRINTED = 2;
    public static final int REMARK_REPRINTED = 3;
    public static final int REMARK_DELETED = 4;
    public static final int REMARK_CANCEL = 5;
    public static final int REMARK_APPROVE = 6;
    public static final int REMARK_CUSTOM = 7;

    public static final String TARGET_PASS = "pass_info";
    public static final String TARGET_BUSINESS = "business_info";

    private int id;
    private int type;
    private int remarkId;
    private int user_id;
    private String target_type;

    private String description;
    private String dateCreated;
    private String remarkType;
    private String remarksOf;

    public Remark() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;

        switch (this.type) {
            case 0:
                remarkType = "Create";
                break;
            case 1:
                remarkType = "Update";
                break;
            case 2:
                remarkType = "Print";
                break;
            case 3:
                remarkType = "Reprint";
                break;
            case 4:
                remarkType = "Delete";
                break;
            case 5:
                remarkType = "Cancel Pass";
                break;
            case 6:
                remarkType = "Approve Pass";
                break;
            case 7:
                remarkType = "Custom";
                break;

        }
    }

    public int getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(int remarkId) {
        this.remarkId = remarkId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getRemarkType() {
        return remarkType;
    }

    public void setRemarkType(String remarkType) {
        this.remarkType = remarkType;
    }

    public String getRemarksOf() {
        return remarksOf;
    }

    public void setRemarksOf(String remarksOf) {
        this.remarksOf = remarksOf;
    }

    public String getTarget_type() {
        return target_type;
    }

    public void setTarget_type(String target_type) {
        this.target_type = target_type;
    }

}
