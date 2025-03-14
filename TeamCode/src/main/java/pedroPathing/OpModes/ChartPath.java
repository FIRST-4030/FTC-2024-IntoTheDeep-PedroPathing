package pedroPathing.OpModes;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.BuildConfig;

import java.io.IOException;

import pedroPathing.CSVReader;
import pedroPathing.LogFile;
import pedroPathing.MecanumDrive;
import pedroPathing.gamepad.InputAutoMapper;
import pedroPathing.gamepad.InputHandler;

@Config
@TeleOp()
public class ChartPath extends OpMode {
    public static boolean logDetails = false;
    public static boolean robotCentric = true;

    enum Options { TBD, START, NEXT, END }
    ChartPath.Options option = ChartPath.Options.TBD;

    boolean useLeftStickToDrive = true;
    boolean returnToStart = false, retracePath = false;
    double driveCoefficient = 0.6;
    int currentPose;
    String poseFile = "posePositions.csv";
    Timer pathTimer;
    InputHandler inputHandler;
    LogFile detailsLog;
    CSVReader posePositions;
    MecanumDrive drive;
    Path goToStart;
    PathChain nextStep;
    Pose thisPose, lastPose;

    private final Pose startPose = new Pose(0, 0, Math.toRadians(90));

    @Override
    public void init() {
        //Initialize gamepad handler
        inputHandler = InputAutoMapper.normal.autoMap(this);

        if (logDetails) { detailsLog = new LogFile("details", "csv"); }
        posePositions = new CSVReader(poseFile,true);

        drive = new MecanumDrive(hardwareMap,startPose,driveCoefficient);
        if (!drive.controlHub.isMacAddressValid()) {
            drive.controlHub.reportBadMacAddress(telemetry,hardwareMap);
            sleep( 10000 );
        }

        pathTimer = new Timer();
    }

    @Override
    public void init_loop() {
        telemetry.addData("A:    ", "Set way point");
        telemetry.addData("B:    ", "Return to start");
        telemetry.addData("X:    ", "Read back stored pose values");
        telemetry.addData("Y:    ", "Replay route");

        telemetry.addData("Compiled on:", BuildConfig.COMPILATION_DATE);
        telemetry.update();
        inputHandler.loop();
    }

    @Override
    public void start() {
        setWayPoint(startPose);
        drive.follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        try {
            handleInput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (useLeftStickToDrive) {
            drive.follower.setTeleOpMovementVectors(-gamepad1.left_stick_y*driveCoefficient,
                    -gamepad1.left_stick_x*driveCoefficient,
                    -gamepad1.right_stick_x*driveCoefficient, robotCentric);
        } else {
            drive.follower.setTeleOpMovementVectors(-gamepad1.right_stick_y*driveCoefficient,
                    -gamepad1.right_stick_x*driveCoefficient,
                    -gamepad1.left_stick_x*driveCoefficient, robotCentric);
        }

        drive.follower.update();

        if (returnToStart) {
            if (!drive.follower.isBusy()) {
                returnToStart();
                returnToStart = false;
            }
        }

        if (retracePath) {
            retracePaths();
        }

        if (logDetails) { detailsLog.logDetails( drive.follower.getPose() ); }
    }

    void handleInput() throws IOException {
        inputHandler.loop();

        if (inputHandler.up("D1:A")) {  // set a way point
            setWayPoint();
        }

        if (inputHandler.up("D1:B")) {  // return to starting position
            returnToStart = true;
        }

        if (inputHandler.up("D1:X")) {
            readBackStoredPosePositions();
        }

        if (inputHandler.up("D1:Y")) {  // retrace path
            retraceSetup();
            option = Options.START;
            retracePath   = true;
        }
    }

    Pose convertToPose(String[] oneLine) {
        String x = oneLine[0], y = oneLine[1], h = oneLine[2];
        return new Pose(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(h));
    }

    Pose getPosition() {
        drive.follower.updatePose();
        return new Pose(drive.follower.getPose().getX(),
                drive.follower.getPose().getY(),
                drive.follower.getPose().getHeading());
    }

    void readBackStoredPosePositions() {
        CSVReader readBackData = new CSVReader(poseFile,false);
        readBackData.readFile();

        for (int i=0 ; i<readBackData.values.size() ; i++) {
            Pose thisPose = convertToPose(readBackData.values.get(i));
            @SuppressLint("DefaultLocale")
            String message = String.format("%.4f", thisPose.getX()) + "," +
                    String.format("%.4f", thisPose.getY()) + "," +
                    String.format("%.4f", Math.toDegrees(thisPose.getHeading()));
            telemetry.addLine(message);
        }
        telemetry.update();
    }

    void retracePaths() {
        switch (option) {
            case START:
                lastPose = convertToPose(posePositions.values.get(currentPose)); // get starting point
                currentPose++; // increment counter into steps
                option = Options.NEXT;
                break;
            case NEXT:
                if (!drive.follower.isBusy()) {
                    if (currentPose<posePositions.values.size()) {
                        thisPose = convertToPose(posePositions.values.get(currentPose));
                        nextStep = drive.follower.pathBuilder()
                                .addPath(new BezierLine(lastPose, thisPose))
                                .setLinearHeadingInterpolation(lastPose.getHeading(),thisPose.getHeading())
                                .build();
                        drive.follower.followPath(nextStep);
                        lastPose = thisPose;
                        currentPose++; // increment counter into steps
                        option = Options.NEXT;   // redundant setting of option toi be clear as to what is going on
                    } else {
                        option = Options.END;
                    }
                }
                break;
            case END:
                if (!drive.follower.isBusy()) {
                    retracePath=false;
                    readBackStoredPosePositions();                }
                break;
        }
    }

    void retraceSetup() throws IOException {
        posePositions.closeFile();
        posePositions.readFile();
        currentPose = 0;
    }

    void returnToStart() {
        Pose currentPose = getPosition();
        goToStart = new Path(new BezierLine(new Point(currentPose), new Point(startPose)));
        goToStart.setLinearHeadingInterpolation(currentPose.getHeading(), startPose.getHeading());

        drive.follower.followPath(goToStart);
    }

    void setWayPoint() {
        posePositions.logPose(drive.follower.getPose());
    }

    void setWayPoint(Pose posePos) {
        posePositions.logPose(posePos);
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}