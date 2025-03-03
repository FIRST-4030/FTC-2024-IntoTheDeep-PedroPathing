package pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.Constants;
import com.qualcomm.robotcore.hardware.HardwareMap;

import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

public class MecanumDrive {
    public Follower follower;

    Pose pose;

    public static String macAddress;
    public ControlHub controlHub = new ControlHub();

    public MecanumDrive(HardwareMap hardwareMap, Pose pose, double maxPower) {
        this.pose = pose;

        this.macAddress = controlHub.getMacAddress();

        Constants.setConstants(FConstants.class, LConstants.class);
        follower = new Follower(hardwareMap);
        follower.setMaxPower(maxPower);
        follower.setStartingPose(pose);
    }
}
