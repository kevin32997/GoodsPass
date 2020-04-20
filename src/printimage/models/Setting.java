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
public class Setting {
    
    private String serverAddress;
    private String username;
    private String password;
    private String folderPath;
    private int tableRowSize;
    
    public Setting(){}

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
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

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public int getTableRowSize() {
        return tableRowSize;
    }

    public void setTableRowSize(int tableRowSize) {
        this.tableRowSize = tableRowSize;
    }
    
    
}
