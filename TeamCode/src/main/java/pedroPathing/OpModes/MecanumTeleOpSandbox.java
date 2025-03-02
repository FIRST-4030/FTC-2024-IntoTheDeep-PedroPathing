package pedroPathing.OpModes;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.config.Config;

import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.BuildConfig;

import pedroPathing.LogFile;
import pedroPathing.MecanumDrive;
import pedroPathing.gamepad.InputAutoMapper;
import pedroPathing.gamepad.InputHandler;

@Config
@TeleOp()
public class MecanumTeleOpSandbox extends OpMode {
    public static boolean logDetails = false;
    public static boolean robotCentric = true;

    enum POSITION {SET_START, SET_END}
    enum LAST_CHAIN {TBD, LT, RT, ITERATING }
    LAST_CHAIN lastChain = LAST_CHAIN.TBD;

    boolean doUpAndBack = false;
    int iterations = 0, currentIteration = 0, pathState;
    Timer pathTimer;
    InputHandler inputHandler;
    LogFile detailsLog;
    MecanumDrive drive;

    Pose newStart = null, newEnd = null;
    PathChain endToStart = null, startToEnd = null;

    private final Pose startPose = new Pose(0, 0, Math.toRadians(90));

    @Override
    public void init() {
        //Initialize gamepad handler
        inputHandler = InputAutoMapper.normal.autoMap(this);

        if (logDetails) { detailsLog = new LogFile(LogFile.FileType.Details,"details", "csv"); }

        drive = new MecanumDrive(hardwareMap,startPose,detailsLog,logDetails);
        if (!drive.controlHub.isMacAddressValid()) {
            drive.controlHub.reportBadMacAddress(telemetry,hardwareMap);
            telemetry.update();
        }
        reportStartEnd();

        pathTimer = new Timer();
    }

    @Override
    public void init_loop() {
        telemetry.addData("DPAD_UP:   ", "increments 'iterations'");
        telemetry.addData("DPAD_DOWN: ", "decrements 'iterations'");
        telemetry.addData("LT:        ", "Move to origin");
        telemetry.addData("RT:        ", "Move to target");
        telemetry.addData("Y:         ", "Moves robot 'iterations' times");

        if (inputHandler.up("D1:DPAD_UP")) {
            iterations++;
        }

        if (inputHandler.up("D1:DPAD_DOWN")) {
            iterations--;
            if (iterations < 0) iterations = 0;
        }

        telemetry.addData("Compiled on:", BuildConfig.COMPILATION_DATE);
        telemetry.addData("Iterations:", iterations);
        telemetry.update();
        inputHandler.loop();
    }

    @Override
    public void start() {
        setPosition(POSITION.SET_START);
        drive.follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        if (!handleInput()) stop();

        drive.follower.setTeleOpMovementVectors(-gamepad1.left_stick_y,
                -gamepad1.left_stick_x, -gamepad1.right_stick_x, robotCentric);
        drive.follower.update();

        if (doUpAndBack) { moveOutAndBack(); }

        reportStartEnd();
    }

    private boolean handleInput() {
        inputHandler.loop();

        if (inputHandler.up("D1:Y")) {
            currentIteration = 0;
            pathState = 0;
            doUpAndBack = true;
        }

        if (inputHandler.up("D1:LB")) {
            setPosition(POSITION.SET_START);
        } else if (inputHandler.up("D1:RB")) {
            setPosition(POSITION.SET_END);
        }

        if (inputHandler.up("D1:LT")) {
            if (lastChain != LAST_CHAIN.LT) moveBack();
            lastChain = LAST_CHAIN.LT;   // Prevent robot from moving when it is already there
        } else if (inputHandler.up("D1:RT")) {
            if (lastChain != LAST_CHAIN.RT) moveOut();
            lastChain = LAST_CHAIN.RT;   // Prevent robot from moving when it is already there
        }
        return true;
    }

    Pose getPosition() {
        drive.follower.updatePose();
        return new Pose(drive.follower.getPose().getX(),
                drive.follower.getPose().getY(),
                drive.follower.getPose().getHeading());
    }

    private void moveBack() {
        drive.follower.followPath(endToStart);
        lastChain = LAST_CHAIN.LT;
    }

    private void moveOut() {
        drive.follower.followPath(startToEnd);
        lastChain = LAST_CHAIN.RT;
    }

    private void moveOutAndBack() {
        // This could be handled with a simple if-else construct but this also
        // shows how to set up a state machine for more complicated situations
        switch (pathState) {
            case 0:
                if(!drive.follower.isBusy()) {
                    drive.follower.followPath(startToEnd);
                    setPathState(1);
                }
                break;
            case 1:
                if(!drive.follower.isBusy()) {
                    drive.follower.followPath(endToStart);
                    currentIteration++;
                    if (currentIteration>=iterations) {
                        doUpAndBack = false;
                        lastChain = LAST_CHAIN.ITERATING;
                    } else {
                        setPathState(0);
                    }
                }
                break;
        }
    }

    void reportStartEnd() {
        if (newStart != null) {
            @SuppressLint("DefaultLocale")
            String start = "X: " + String.format("%.2f", newStart.getX()) + ", " +
                           "Y: " + String.format("%.2f", newStart.getY()) + ", " +
                           "Heading: " + String.format("%.2f", Math.toDegrees(newStart.getHeading()));
            telemetry.addLine("Start - " + start);
        }
        if (newEnd != null) {
            @SuppressLint("DefaultLocale")
            String end = "X: " + String.format("%.2f", newEnd.getX()) + ", " +
                         "Y: " + String.format("%.2f", newEnd.getY()) + ", " +
                         "Heading: " + String.format("%.2f", Math.toDegrees(newEnd.getHeading()));
            telemetry.addLine("End   - " + end);
        }
        telemetry.addData("lastChain: ", lastChain);
        telemetry.update();
    }

    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }

    void setPosition(POSITION pos) {
        if (pos == POSITION.SET_START) {
            newStart = getPosition();
        } else if (pos == POSITION.SET_END) {
            newEnd = getPosition();
        }

        if (newStart!=null && newEnd!=null) {
            endToStart = drive.follower.pathBuilder()
                    .addPath(new BezierLine(newEnd, newStart))
                    .setConstantHeadingInterpolation(newStart.getHeading())
                    .build();

            startToEnd = drive.follower.pathBuilder()
                    .addPath(new BezierLine(newStart, newEnd))
                    .setConstantHeadingInterpolation(newEnd.getHeading())
                    .build();
        }
    }
}