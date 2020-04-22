/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.helpers;

/**
 *
 * @author Admin
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import printimage.models.BusinessInfo;
import printimage.models.Crew;
import printimage.models.Goodspass;

public class SQLDatabase {

    private static final String DB_SERVER_ADD = "jdbc:mysql://localhost:3306/goodsregistry";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    private Connection con;
    private Statement stmt;

    public SQLDatabase(String URL, String username, String password) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        con = DriverManager.getConnection(
                "jdbc:mysql://" + URL, username, password);
        stmt = con.createStatement();

        // Init DB Updated
        if (this.getDBUpdated().equals("")) {
            this.createUpdateTimeStamp();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                  DRIVER CRUD 
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Count
    public int getDriverCount() {
        int count = 0;
        String query = "SELECT count(*) FROM passes";
        try {
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    // Add
    public boolean createPass(Goodspass pass) {

        try {
            String query = " INSERT into passes ("
                    + "gp_no, "
                    + "vehicle_desc, "
                    + "vehicle_plate_no, "
                    + "business_id, "
                    + "business_name, "
                    + "status "
                    + ") values ("
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setString(1, pass.getGpNo());
            preparedStmt.setString(2, pass.getVehicleDesc());
            preparedStmt.setString(3, pass.getVehiclePlateNo());
            preparedStmt.setString(4, pass.getBusinessId());
            preparedStmt.setString(5, pass.getBusinessName());
            preparedStmt.setString(6, pass.getStatus());

            preparedStmt.execute();

            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Pass Information ADDED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("DB ERROR");
            alert.setHeaderText("Database Error");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // Update
    public boolean updatePassInfo(Goodspass info) {

        try {
            String query = "";
            if (info.getDate_sql() != null) {
                query = "UPDATE passes SET "
                        + "gp_no = ?, "
                        + "vehicle_desc = ?, "
                        + "vehicle_plate_no = ?, "
                        + "business_id = ?, "
                        + "status = ?, "
                        + "date_printed = ? "
                        + "WHERE id = ?";
            } else {
                query = "UPDATE passes SET "
                        + "gp_no = ?, "
                        + "vehicle_desc = ?, "
                        + "vehicle_plate_no = ?, "
                        + "business_id = ?, "
                        + "status = ? "
                        + "WHERE id = ?";
            }

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.getGpNo());
            ps.setString(2, info.getVehicleDesc());
            ps.setString(3, info.getVehiclePlateNo());
            ps.setString(4, info.getBusinessId());

            ps.setInt(5, Integer.parseInt(info.getStatus()));

            if (info.getDate_sql() != null) {
                ps.setDate(6, info.getDate_sql());
                ps.setInt(7, info.getId());
            } else {
                ps.setInt(6, info.getId());
            }
            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("DB ERROR");
            alert.setHeaderText("Database Error");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // Update
    public boolean updatePassInfoForDebug(Goodspass info) {

        try {
            String query = "UPDATE passes SET "
                    + "gp_no = ?, "
                    + "vehicle_desc = ?, "
                    + "vehicle_plate_no = ?, "
                    + "business_id = ?, "
                    + "business_name = ?, "
                    + "status = ?, "
                    + "date_printed = ? "
                    + "WHERE id = ?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.getGpNo());
            ps.setString(2, info.getVehicleDesc());
            ps.setString(3, info.getVehiclePlateNo());
            ps.setString(4, info.getBusinessId());
            ps.setString(5, info.getBusinessName());
            ps.setInt(6, Integer.parseInt(info.getStatus()));

            if (info.getDate_sql() != null) {
                ps.setDate(7, info.getDate_sql());
            } else {
                ps.setDate(7, null);
            }

            ps.setInt(8, info.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("DB ERROR");
            alert.setHeaderText("Database Error");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // Delete
    public boolean removePass(int id) {
        try {
            String query = "DELETE FROM passes WHERE id = ?";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, id);
            preparedStmt.execute();

            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Driver Information DELETED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("DB ERROR");
            alert.setHeaderText("Database Error");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public Goodspass getPassInfoById(int id) {
        ObservableList<Goodspass> passes = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes WHERE id='" + id + "' limit 1");
            while (resultSet.next()) {
                Goodspass pass = new Goodspass();
                pass.setId(resultSet.getInt("id"));
                pass.setGpNo(resultSet.getString("gp_no"));
                pass.setVehicleDesc(resultSet.getString("vehicle_desc"));
                pass.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                pass.setBusinessId(resultSet.getString("business_id"));
                pass.setBusinessName(resultSet.getString("business_name"));
                pass.setStatus(resultSet.getString("status"));
                pass.setDate_printed(resultSet.getString("date_printed"));
                passes.add(pass);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (passes.size() > 0) {
            return passes.get(0);
        } else {
            return null;
        }
    }

    public Goodspass getPassInfoByPassNo(String passNo) {
        ObservableList<Goodspass> passes = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes WHERE gp_no='" + passNo + "' limit 1");
            while (resultSet.next()) {
                Goodspass pass = new Goodspass();
                pass.setId(resultSet.getInt("id"));
                pass.setGpNo(resultSet.getString("gp_no"));
                pass.setVehicleDesc(resultSet.getString("vehicle_desc"));
                pass.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                pass.setBusinessId(resultSet.getString("business_id"));
                pass.setBusinessName(resultSet.getString("business_name"));
                pass.setStatus(resultSet.getString("status"));
                pass.setDate_printed(resultSet.getString("date_printed"));
                passes.add(pass);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (passes.size() > 0) {
            return passes.get(0);
        } else {
            return null;
        }
    }

    public ObservableList<Goodspass> getPassInfoByBusinessId(int businessId) {
        ObservableList<Goodspass> passes = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes WHERE business_id='" + businessId + "'");
            while (resultSet.next()) {
                Goodspass pass = new Goodspass();
                pass.setId(resultSet.getInt("id"));
                pass.setGpNo(resultSet.getString("gp_no"));
                pass.setVehicleDesc(resultSet.getString("vehicle_desc"));
                pass.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                pass.setBusinessId(resultSet.getString("business_id"));
                pass.setStatus(resultSet.getString("status"));
                passes.add(pass);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return passes;

    }

    public ObservableList<Goodspass> searchPassInfoByGivenColumnLimit(String searchby, String start, int limit) {
        ObservableList<Goodspass> passes = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes WHERE " + searchby + " LIKE '%" + start + "%' limit " + limit);
            while (resultSet.next()) {
                Goodspass driver = new Goodspass();
                driver.setId(resultSet.getInt("id"));
                driver.setGpNo(resultSet.getString("gp_no"));
                driver.setVehicleDesc(resultSet.getString("vehicle_desc"));
                driver.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                driver.setBusinessId(resultSet.getString("business_id"));
                driver.setStatus(resultSet.getString("status"));
                passes.add(driver);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return passes;

    }

    public ObservableList<Goodspass> getPassInfoByBusinessLimitDesc(int offset, int count) {
        ObservableList<Goodspass> passes = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes ORDER BY id DESC limit " + offset + "," + count + "");
            while (resultSet.next()) {
                Goodspass pass = new Goodspass();
                pass.setId(resultSet.getInt("id"));
                pass.setGpNo(resultSet.getString("gp_no"));
                pass.setVehicleDesc(resultSet.getString("vehicle_desc"));
                pass.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                pass.setBusinessId(resultSet.getString("business_id"));
                pass.setStatus(resultSet.getString("status"));
                passes.add(pass);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return passes;

    }

    public ObservableList<Goodspass> getPassInfoByBusinessLimitAsc(int offset, int count) {
        ObservableList<Goodspass> drivers = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes ORDER BY id limit " + offset + "," + count + "");
            while (resultSet.next()) {
                Goodspass driver = new Goodspass();
                driver.setId(resultSet.getInt("id"));
                driver.setGpNo(resultSet.getString("gp_no"));
                driver.setVehicleDesc(resultSet.getString("vehicle_desc"));
                driver.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                driver.setBusinessId(resultSet.getString("business_id"));
                driver.setStatus(resultSet.getString("status"));
                drivers.add(driver);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return drivers;

    }

    public ObservableList<Goodspass> getAllPassesByBusinessId(int businessId) {
        ObservableList<Goodspass> drivers = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes WHERE business_id='" + businessId + "'");
            while (resultSet.next()) {
                Goodspass driver = new Goodspass();
                driver.setId(resultSet.getInt("id"));
                driver.setGpNo(resultSet.getString("gp_no"));
                driver.setVehicleDesc(resultSet.getString("vehicle_desc"));
                driver.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                driver.setBusinessId(resultSet.getString("business_id"));
                driver.setStatus(resultSet.getString("status"));
                driver.setDate_printed(resultSet.getString("date_printed"));
                drivers.add(driver);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return drivers;
    }

    // View All
    public ObservableList<Goodspass> getAllPasses() {
        ObservableList<Goodspass> drivers = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes");
            while (resultSet.next()) {
                Goodspass driver = new Goodspass();
                driver.setId(resultSet.getInt("id"));
                driver.setGpNo(resultSet.getString("gp_no"));
                driver.setVehicleDesc(resultSet.getString("vehicle_desc"));
                driver.setVehiclePlateNo(resultSet.getString("vehicle_plate_no"));
                driver.setBusinessId(resultSet.getString("business_id"));
                driver.setStatus(resultSet.getString("status"));
                drivers.add(driver);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return drivers;
    }

    // View All
    public String getLatestGPNo() {
        ObservableList<Goodspass> passes = FXCollections.observableArrayList();
        String returnValue = "";
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM passes ORDER BY id DESC limit 1");
            while (resultSet.next()) {
                returnValue = resultSet.getString("gp_no");
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                  BUSINESS CRUD INFO 
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Count
    public int getBusinessCount() {
        int count = 0;
        String query = "SELECT count(*) FROM business_info";
        try {
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    public void createBusinessInfo(BusinessInfo info) {
        try {
            String query = "INSERT into business_info ("
                    + "owner, "
                    + "business_name, "
                    + "permit_no, "
                    + "address, "
                    + "contact_no, "
                    + "contact"
                    + ") values ("
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setString(1, info.getOwner());
            preparedStmt.setString(2, info.getBusinessName());
            preparedStmt.setString(3, info.getPermitNo());
            preparedStmt.setString(4, info.getAddress());
            preparedStmt.setString(5, info.getContact());
            preparedStmt.setString(6, info.getContactPerson());
            preparedStmt.execute();

            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Business Information ADDED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int getLastBusinessInfoId() {
        int returnValue = 0;
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT id FROM business_info ORDER BY id DESC limit 1");
            while (resultSet.next()) {
                returnValue = resultSet.getInt("id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    // Update
    public boolean updateBusinessInfo_boolean(BusinessInfo info) {
        try {
            String query = "UPDATE business_info SET "
                    + "owner=?, "
                    + "business_name=?, "
                    + "permit_no=?, "
                    + "address=?, "
                    + "contact_no=?, "
                    + "contact=? "
                    + "WHERE id=?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.getOwner());
            ps.setString(2, info.getBusinessName());
            ps.setString(3, info.getPermitNo());
            ps.setString(4, info.getAddress());
            ps.setString(5, info.getContact());
            ps.setString(6, info.getContactPerson());
            ps.setInt(7, info.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public int updateBusinessInfo_int(BusinessInfo info) {
        int returnVal = -1;
        try {
            String query = "UPDATE business_info SET "
                    + "owner=?, "
                    + "business_name=?, "
                    + "permit_no=?, "
                    + "address=?, "
                    + "contact_no=?, "
                    + "contact=? "
                    + "WHERE id=?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.getOwner());
            ps.setString(2, info.getBusinessName());
            ps.setString(3, info.getPermitNo());
            ps.setString(4, info.getAddress());
            ps.setString(5, info.getContact());
            ps.setString(6, info.getContactPerson());
            ps.setInt(7, info.getId());

            returnVal = ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnVal;
    }

    // Delete
    public void removeBusinessInfo(int id) {
        try {
            String query = "DELETE FROM business_info WHERE id = ?";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, id);
            preparedStmt.execute();

            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Business Information DELETED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BusinessInfo getBusinessInfoById(int id) {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info WHERE id='" + id + "' limit 1");
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));
                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (businesses.size() > 0) {
            return businesses.get(0);
        } else {
            return null;
        }
    }

    public BusinessInfo getBusinessInfoByPermitNo(String permitNo) {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info WHERE permit_no='" + permitNo + "' limit 1");
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));

                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (businesses.size() > 0) {
            return businesses.get(0);
        } else {
            return null;
        }
    }

    // View All
    public ObservableList<BusinessInfo> getAllBusinessInfo() {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info");
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));
                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return businesses;
    }

    // View LIMIT
    public ObservableList<BusinessInfo> getBusinessInfoLimitAsc(int offset, int count) {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info ORDER BY id LIMIT " + offset + "," + count);
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));
                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return businesses;
    }

    public ObservableList<BusinessInfo> getBusinessInfoLimitDesc(int offset, int count) {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info ORDER BY id DESC LIMIT " + offset + "," + count);
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));
                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return businesses;
    }

    // Search
    public ObservableList<BusinessInfo> searchBusinessInfoLimit(String text, int limit) {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info WHERE business_name LIKE '%" + text + "%' limit " + limit);
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));
                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return businesses;
    }

    public ObservableList<BusinessInfo> searchBusinessInfoByColumnLimit(String column, String text, int limit) {
        ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM business_info WHERE " + column + " LIKE '%" + text + "%' limit " + limit);
            while (resultSet.next()) {
                BusinessInfo info = new BusinessInfo();
                info.setId(resultSet.getInt("id"));
                info.setOwner(resultSet.getString("owner"));
                info.setBusinessName(resultSet.getString("business_name"));
                info.setPermitNo(resultSet.getString("permit_no"));
                info.setAddress(resultSet.getString("address"));
                info.setContact(resultSet.getString("contact_no"));
                info.setContactPerson(resultSet.getString("contact"));
                businesses.add(info);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return businesses;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                  CREW INFO CRUD 
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Count
    public int getCrewCount() {
        int count = 0;
        String query = "SELECT count(*) FROM crew_info";
        try {
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            this.showError(ex);
        }
        return count;
    }

    // CREATE
    public boolean createCrewInfo(Crew info) {
        int return_value = 0;
        try {
            String query = "INSERT into crew_info ("
                    + "gp_no, "
                    + "full_name, "
                    + "person_address, "
                    + "designation, "
                    + "id_presented, "
                    + "id_presented_no"
                    + ") values ("
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?, "
                    + "?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query, new String[]{"id"});
            preparedStmt.setString(1, info.getGpNo());
            preparedStmt.setString(2, info.getFullname());
            preparedStmt.setString(3, info.getAddress());
            preparedStmt.setString(4, info.getDesignation());
            preparedStmt.setString(5, info.getIdPresented());
            preparedStmt.setString(6, info.getIdNumber());
            return_value = preparedStmt.executeUpdate();
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Business Information ADDED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            showError(ex);
        }
        return return_value > 0;
    }

    // VIEW
    public Crew getCrewInfoById(int id) {
        ObservableList<Crew> crews = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM crew_info WHERE id='" + id + "' limit 1");
            while (resultSet.next()) {
                Crew crew = new Crew();
                crew.setId(resultSet.getInt("id"));
                crew.setGpNo(resultSet.getString("gp_no"));
                crew.setFullname(resultSet.getString("full_name"));
                crew.setAddress(resultSet.getString("person_address"));
                crew.setDesignation(resultSet.getString("designation"));
                crew.setIdPresented(resultSet.getString("id_presented"));
                crew.setIdNumber(resultSet.getString("id_presented_no"));
                crews.add(crew);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            showError(ex);
        }
        if (crews.size() > 0) {
            return crews.get(0);
        } else {
            return null;
        }
    }

    public ObservableList<Crew> getAllCrewByGPNo(String gp_no) {
        ObservableList<Crew> crews = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM crew_info WHERE gp_no='" + gp_no + "'");
            while (resultSet.next()) {
                Crew crew = new Crew();
                crew.setId(resultSet.getInt("id"));
                crew.setGpNo(resultSet.getString("gp_no"));
                crew.setFullname(resultSet.getString("full_name"));
                crew.setAddress(resultSet.getString("person_address"));
                crew.setDesignation(resultSet.getString("designation"));
                crew.setIdPresented(resultSet.getString("id_presented"));
                crew.setIdNumber(resultSet.getString("id_presented_no"));
                crews.add(crew);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            showError(ex);

        }
        return crews;
    }

    // View LIMIT
    public ObservableList<Crew> getCrewInfoLimitAsc(int offset, int count) {
        ObservableList<Crew> crews = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM crew_info ORDER BY id LIMIT " + offset + "," + count);
            while (resultSet.next()) {
                Crew crew = new Crew();
                crew.setId(resultSet.getInt("id"));
                crew.setGpNo(resultSet.getString("gp_no"));
                crew.setFullname(resultSet.getString("full_name"));
                crew.setAddress(resultSet.getString("person_address"));
                crew.setDesignation(resultSet.getString("designation"));
                crew.setIdPresented(resultSet.getString("id_presented"));
                crew.setIdNumber(resultSet.getString("id_presented_no"));
                crews.add(crew);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            showError(ex);
        }
        return crews;
    }

    // View LIMIT
    public ObservableList<Crew> getCrewInfoLimitDesc(int offset, int count) {
        ObservableList<Crew> crews = FXCollections.observableArrayList();
        try {

            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM crew_info ORDER BY id DESC LIMIT " + offset + "," + count);
            while (resultSet.next()) {
                Crew crew = new Crew();
                crew.setId(resultSet.getInt("id"));
                crew.setGpNo(resultSet.getString("gp_no"));
                crew.setFullname(resultSet.getString("full_name"));
                crew.setAddress(resultSet.getString("person_address"));
                crew.setDesignation(resultSet.getString("designation"));
                crew.setIdPresented(resultSet.getString("id_presented"));
                crew.setIdNumber(resultSet.getString("id_presented_no"));
                crews.add(crew);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return crews;
    }

    // VIEW BY DB COLUMN
    public ObservableList<Crew> searchCrewInfoByColumnLimit(String column, String text, int limit) {
        ObservableList<Crew> crews = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = this.stmt.executeQuery("SELECT * FROM crew_info WHERE " + column + " LIKE '%" + text + "%' limit " + limit);
            while (resultSet.next()) {
                Crew crew = new Crew();
                crew.setId(resultSet.getInt("id"));
                crew.setGpNo(resultSet.getString("gp_no"));
                crew.setFullname(resultSet.getString("full_name"));
                crew.setAddress(resultSet.getString("person_address"));
                crew.setDesignation(resultSet.getString("designation"));
                crew.setIdPresented(resultSet.getString("id_presented"));
                crew.setIdNumber(resultSet.getString("id_presented_no"));
                crews.add(crew);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return crews;
    }

    // Update
    public boolean updateCrewInfo(Crew info) {
        try {
            String query = "UPDATE crew_info SET "
                    + "gp_no=?, "
                    + "full_name=?, "
                    + "person_address=?, "
                    + "designation=?, "
                    + "id_presented=?, "
                    + "id_presented_no=? "
                    + "WHERE id=?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.getGpNo());
            ps.setString(2, info.getFullname());
            ps.setString(3, info.getAddress());
            ps.setString(4, info.getDesignation());
            ps.setString(5, info.getIdPresented());
            ps.setString(6, info.getIdNumber());
            ps.setInt(7, info.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            showError(ex);
            return false;
        }
        return true;
    }

    // Delete
    public void removeCrewInfo(int id) {
        try {
            String query = "DELETE FROM crew_info WHERE id = ?";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, id);
            preparedStmt.execute();

            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Crew Information DELETED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            this.showError(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                  REMARKS INFO CRUD 
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DB Update
    private void createUpdateTimeStamp() {
        try {
            String query = "INSERT INTO db_updated (update_time) VALUES (current_timestamp())";

            // create the mysql insert preparedstatement
            Statement stmt = con.createStatement();
            stmt.execute(query);

            Logger.getLogger(SQLDatabase.class.getName()).log(Level.FINE, null, "Driver Information ADDED!");
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getDBUpdated() {
        String returnVal = "";
        try {

            ResultSet getUpdateResultSet = this.stmt.executeQuery("SELECT * FROM db_updated limit 1");
            while (getUpdateResultSet.next()) {
                returnVal = getUpdateResultSet.getString("update_time");
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnVal;
    }

    public void updateDB() {
        try {
            String query = "UPDATE db_updated SET update_time=current_timestamp()";
            Statement stmt = con.createStatement();
            stmt.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showError(Exception ex) {

        Logger.getLogger(SQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("DB ERROR");
        alert.setHeaderText("Database Error");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

}
