package pedroPathing;

public class GeneralConstants {

    public static final String PRIMARY_BOT = ControlHub.getBotAddress(2);
    public static final String SECONDARY_BOT = ControlHub.getBotAddress(3);
    public static final boolean ENABLE_CALIBRATION = false;

    //These constant names allow for more effective grouping when the
    //annotation, "Teleop" or "Autonomous", is used to mark an opmode
    //to submit to the driver station or dashboard
    public static final String SAMPLE_OPMODE = "SAMPLE";
    public static final String TEST_OPMODE = "TEST";
    public static final String DEPLOYED_OPMODE = "DEPLOYED";

    //NANOSECONDS to X conversions coefficients
    public static final double NANO2MS = 1e-6d;
    public static final double NANO2SEC = 1e-9d;

    //MILLISECONDS to X conversions coefficients
    public static final double MS2NANO = 1e6d;
    public static final double MS2SEC = 1e-3d;

    //SECONDS to X conversion coefficients
    public static final double SEC2NANO = 1e9d;
    public static final double SEC2MS = 1e3d;

    //meters to X conversion coefficients
    public static final double METERS2INCHES = 39.3701d;
    public static final double METERS2FEET = 3.28084d;
    public static final double METERS2CM = 100;
    public static final double METERS2MM = 1000;

    //feet to X conversion coefficients
    public static final double FEET2INCHES = 12d;
    public static final double FEET2METERS = 0.3048;

    //inches to X conversion coefficients
    public static final double INCHES2FEET = 1/12d;
    public static final double INCHES2METERS = 0.0254;

    //centimeters to meters and millimeters
    public static double CM2METERS = 1/100d;
    public static double CM2MM = 10d;

    //millimeters to centimeters and meters
    public static double MM2CM = 1/10d;
    public static double MM2METERS = 1/1000d;

    //mathematical constants that are useful
    public static final double PI = Math.PI;
    public static final double TAU = Math.PI * 2d;
    public static final double PHI = (1 + Math.sqrt(5d))/2d; //also known as "the golden ratio"

    //mathematical conversion
    public static final double DEG2RAD = PI / 180d;
    public static final double RAD2DEG = 180d / PI;
}
