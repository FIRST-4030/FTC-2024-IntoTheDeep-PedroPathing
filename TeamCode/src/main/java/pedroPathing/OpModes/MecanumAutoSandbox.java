package pedroPathing.OpModes;

import com.acmerobotics.dashboard.config.Config;

import com.pedropathing.localization.Pose;

import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.BuildConfig;

import pedroPathing.LogFile;
import pedroPathing.MecanumDrive;
import pedroPathing.gamepad.InputAutoMapper;
import pedroPathing.gamepad.InputHandler;

@Config
@Autonomous
public class MecanumAutoSandbox extends OpMode {

    public static boolean logDetails = false;
    boolean doAutonomous = false;
    boolean repeatIt = false;

    public static double startHead = 90;
    public static double step1Head = 90;
    public static double step2Head = 90;

    public static double maxPower = 0.4;

    enum Options { TBD, MOVE_LEFT, MOVE_FORWARD, END, FALL_BACK, SHOVE_LEFT, MOVE_RIGHT }
    Options option = Options.TBD;

    Timer pathTimer, opmodeTimer;
    InputHandler inputHandler;
    LogFile detailsLog;
    MecanumDrive drive;

    private final Pose startPose = new Pose( 0, 0, Math.toRadians(startHead));

    public static double largeMoveLeftX = -30, largeMoveLeftY = 35;
    private final Pose leftPose = new Pose(largeMoveLeftX, largeMoveLeftY, Math.toRadians(step1Head));

    public static double forwardX = -30, forwardY = 45;
    private final Pose forwardPose = new Pose(forwardX, forwardY, Math.toRadians(step2Head));

    public static double shoveLeftX = -45, shoveLeftY = 45;
    private final Pose shoveLeftPose = new Pose(shoveLeftX, shoveLeftY, Math.toRadians(step2Head));

    public static double backX = -45, backY = 20;
    private final Pose backPose = new Pose(backX, backY, Math.toRadians(step2Head));

    public static double rightX = -20, rightY = 20;
    private final Pose rightPose = new Pose(rightX, rightY, Math.toRadians(step1Head));

    PathChain moveLeft, moveForward, shoveLeft, fallBack, moveRight, moveEnd;

    @Override
    public void init() {
        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        if (logDetails) { detailsLog = new LogFile("details", "csv"); }

        drive = new MecanumDrive(hardwareMap, startPose, maxPower);
        if (!drive.controlHub.isMacAddressValid()) {
            drive.controlHub.reportBadMacAddress(telemetry,hardwareMap);
            sleep( 10000 );
        }

        ElapsedTime runtime = new ElapsedTime();
        runtime.reset();

        inputHandler = InputAutoMapper.normal.autoMap(this);
        buildPaths();
    }

    @Override
    public void init_loop() {
        inputHandler.loop();

        if (inputHandler.up("D1:X")) {
            doAutonomous = false;
        }

        telemetry.addData("Compiled on:", BuildConfig.COMPILATION_DATE);
        telemetry.addLine();
        telemetry.addData("D1:X:","Abort Autonomous");
        telemetry.update();
    }

    @Override
    public void start() {
        opmodeTimer.resetTimer();

        option = Options.MOVE_LEFT;
        doAutonomous = true;
    }

    @Override
    public void loop() {
        drive.follower.update();

        // Feedback to Driver Hub
        telemetry.addData("path state", option);
        telemetry.addData("x", drive.follower.getPose().getX());
        telemetry.addData("y", drive.follower.getPose().getY());
        telemetry.addData("heading", drive.follower.getPose().getHeading());
        telemetry.update();

        if (doAutonomous) {
            autonomousPaths();

            if (logDetails) { detailsLog.logDetails(drive.follower.getPose()); }
        }
    }

    void autonomousPaths() {

        switch (option) {
            case MOVE_LEFT:
                if (repeatIt) {
                    if (!drive.follower.isBusy()) {
                        drive.follower.followPath(moveLeft, true);
                    }
                } else {
                    drive.follower.followPath(moveLeft, true);
                    repeatIt = true;
                }
                option = Options.MOVE_FORWARD;
                break;
            case MOVE_FORWARD:
                if (!drive.follower.isBusy()) {
                    drive.follower.followPath(moveForward, true);
                    option = Options.SHOVE_LEFT;
                }
                break;
            case SHOVE_LEFT:
                if (!drive.follower.isBusy()) {
                    drive.follower.followPath(shoveLeft, true);
                    option = Options.FALL_BACK;
                }
                break;
            case FALL_BACK:
                if (!drive.follower.isBusy()) {
                    drive.follower.followPath(fallBack, true);
                    option = Options.MOVE_RIGHT;
                }
                break;
            case MOVE_RIGHT:
                if (!drive.follower.isBusy()) {
                    drive.follower.followPath(moveRight, true);
                    option = Options.END;
                }
                break;
            case END:
                if (!drive.follower.isBusy()) {
                    drive.follower.followPath(moveEnd, true);
                    option = Options.MOVE_LEFT;
//                    doAutonomous = false;
                }
                break;
            }
    }

    public void buildPaths() {
        moveLeft = drive.follower.pathBuilder()
                .addPath(new BezierLine(new Point(startPose), new Point(leftPose)))
                .setLinearHeadingInterpolation(startPose.getHeading(), leftPose.getHeading())
                .build();

        moveForward = drive.follower.pathBuilder()
                .addPath(new BezierLine(new Point(leftPose), new Point(forwardPose)))
                .setLinearHeadingInterpolation(leftPose.getHeading(), forwardPose.getHeading())
                .build();

        shoveLeft = drive.follower.pathBuilder()
                .addPath(new BezierLine(new Point(forwardPose), new Point(shoveLeftPose)))
                .setLinearHeadingInterpolation(forwardPose.getHeading(), shoveLeftPose.getHeading())
                .build();

        fallBack = drive.follower.pathBuilder()
                .addPath(new BezierLine(new Point(shoveLeftPose), new Point(backPose)))
                .setLinearHeadingInterpolation(shoveLeftPose.getHeading(), backPose.getHeading())
                .build();

        moveRight = drive.follower.pathBuilder()
                .addPath(new BezierLine(new Point(backPose), new Point(rightPose)))
                .setLinearHeadingInterpolation(backPose.getHeading(), rightPose.getHeading())
                .build();

        moveEnd = drive.follower.pathBuilder()
                .addPath(new BezierLine(new Point(rightPose), new Point(startPose)))
                .setLinearHeadingInterpolation(rightPose.getHeading(), startPose.getHeading())
                .build();
    }

    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}