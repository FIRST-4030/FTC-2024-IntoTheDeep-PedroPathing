package pedroPathing;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * This class, which instantiated in <i>MecanumDrive</i>, ensures
 * that only known Control Hubs are used. Using the MAC address, <br>
 * this class controls which set of calibration constants will be used.
 * <p>
 * After creating the <i>drive</i> object you must check to see that
 * the MAC Address is recognized. For example,
 * <pre>
 *     MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose, detailsLog, logDetails);
 *
 *     if (!drive.controlHub.isMacAddressValid()) {
 *        drive.controlHub.reportBadMacAddress(telemetry,hardwareMap);
 *        telemetry.update();
 *     }
 * </pre>
 * If an address is not found than the Driver Station will issue an alert sound
 * and display a message showing the MAC address that was not found
 */
public class ControlHub {
    /*
     * Note: Only the MAC Address is used to control operations within the
     *       source code. The other fields are merely used as descriptors
     */
    static String[][] addresses = {
            //   MAC Address          Network               Comment
            {"00:1A:2C:24:D7:F0", "8628-RC",          "8628 Competition Bot"},
            {"7C:A7:B0:07:BE:CF", "8628-RC-2",        "8628 Backup Bot"},
            {"C8:FE:0F:2C:56:14", "7462-RC",          "7462 Competition Bot"},
            {"7C:A7:B0:0F:CB:78", "7462-RC-2",        "7462 Demo Base"},
            {"7C:A7:B0:09:82:54", "FTC-7462-TankGuy", "Duck Bot"}
    };

    private final String macAddress;
    private boolean isKnown;

    /**
     * Constructor for the ControlHub class
     */
    public ControlHub() {
        StringBuilder macAddressStr = null;

        try {
            // Get the network interfaces on the system
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();

                // Get the hardware address (MAC address)
                byte[] macAddressBytes = network.getHardwareAddress();

                if (macAddressBytes != null) {
                    // Convert the byte array to a readable MAC address format
                    macAddressStr = new StringBuilder();
                    for (int i = 0; i < macAddressBytes.length; i++) {
                        macAddressStr.append(String.format("%02X", macAddressBytes[i]));
                        if (i < macAddressBytes.length - 1) {
                            macAddressStr.append(":");
                        }
                    }
                }
            }
        } catch (
                SocketException e) {
            e.printStackTrace();
        }

        if (macAddressStr==null) {
            this.macAddress = null;
        } else {
            this.macAddress = macAddressStr.toString();
            isKnown = false;
            for (String[] address : addresses) {
                if (address[0].equals(this.macAddress)) {
                    isKnown = true;
                    break;
                }
            }
        }
    }

    /**
     *
     * @return a boolean that defines if the found MAC address is part of the known addresses
     */
    public boolean isMacAddressValid() {
        return isKnown;
    }

    /**
     *
     * @param offset Integer offset into the <i>addresses</i> array for the given robot
     * @return Specific <b>MAC Address</b> string from the <i>addresses</i> array
     */
    public static String getBotAddress(int offset)
    {
        return addresses[offset][0];
    }

    /**
     *
     * @return <b>MAC Address</b> string from the <i>addresses</i> array
     */
    public String getMacAddress() {
        return this.macAddress;
    }

    /**
     *
     * @return <b>Network</b> string from the <i>addresses</i> array as it should appear on the Driver Station
     */
    public String getNetworkName() {

        String networkName = "";

        for (String[] address : addresses) {
            if (address[0].equals(this.macAddress)) {
                networkName = address[1];
            }
        }
        return networkName;
    }

    /**
     *
     * @return <b>Comment</b> string from the <i>addresses</i> array
     */
    public String getComment() {

        String comment = "";

        for (String[] address : addresses) {
            if (address[0].equals(this.macAddress)) {
                comment = address[2];
            }
        }
        return comment;
    }

    /**
     *
     * @param telemetry Telemetry object used to report a message to the Driver Station
     * @param hardwareMAp Current HardwareMap of the robot
     */
    public void reportBadMacAddress( Telemetry telemetry, HardwareMap hardwareMAp) {
        /*
         *  Note: "red_alert.wav" can be found in the "TeamCode/res/raw" folder
         */
        int soundID = hardwareMAp.appContext.getResources().getIdentifier("red_alert", "raw", hardwareMAp.appContext.getPackageName());
        SoundPlayer.getInstance().startPlaying(hardwareMAp.appContext, soundID);
        telemetry.addLine();
        telemetry.addLine("Control Hub Mac Address is not recognized: ");
        telemetry.addLine("           "+this.getMacAddress());
        telemetry.addLine();
        telemetry.addLine("Update 'ControlHub' to include this new address.");
    }
}