package pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.Constants;
import com.qualcomm.robotcore.hardware.HardwareMap;

import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

public class MecanumDrive {
    public static String macAddress;

    public Follower follower;
    HardwareMap hardwareMap;
    Pose pose;

    double maxPower;

    public ControlHub controlHub = new ControlHub();

    public MecanumDrive(HardwareMap hardwareMap, Pose pose, double maxPower) {
        this.hardwareMap = hardwareMap;
        this.pose = pose;
        this.maxPower = maxPower;

        macAddress = controlHub.getMacAddress();

        FConstants fConstants = new FConstants();
        LConstants lConstants = new LConstants();
        Constants.setConstants(fConstants.getClass(), lConstants.getClass());
        follower = new Follower(hardwareMap);
        follower.setMaxPower(maxPower);
        follower.setStartingPose(pose);
    }
}
