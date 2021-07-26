/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.helpers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 *
 * @author Admin
 */
public class Helper {

    public static String getMd5(String input) {
        try {

            // Static getInstance method is called with hashing MD5 
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest 
            //  of an input digest() return array of byte 
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value 
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        } // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static WritableImage getFontAwesomeIcon(FontAwesomeIcon icon) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        WritableImage writableImage = new WritableImage(1100, 1100);
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(100, 100));
        return SwingFXUtils.toFXImage(SwingFXUtils.fromFXImage(iconView.snapshot(spa, writableImage), null), null);
    }

    public static WritableImage getMaterialDesignIcon(MaterialDesignIcon icon) {
        MaterialDesignIconView iconView = new MaterialDesignIconView(icon);
        WritableImage writableImage = new WritableImage(1100, 1100);
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(100, 100));
        return SwingFXUtils.toFXImage(SwingFXUtils.fromFXImage(iconView.snapshot(spa, writableImage), null), null);
    }
}
