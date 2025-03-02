package pedroPathing;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFile {

    public enum FileType { Details, Action }

    private final String prefix;
    private final String fileType;
    String logFolder;

    private final FileWriter logWriter;
    FileWriter logDelta;
    private String label = null;

    private double startHeading = 0.0;
    private double startPoseX   = 0.0;
    private double startPoseY   = 0.0;

    final long absoluteStartTime;
    private long startTime;
    private long deltaTime;

    public String fullPath = null;
    String timeStamp;

    /**
     * Note: For this method to work you have to be sure that the
     *       folder "/sdcard/FIRST/logs" exists on the Control Hub
     *
     * @param prefix - root name of the file that will have a time stamp added
     * @param fileType - file extension (typically "csv" or "txt")
     */
    public LogFile(FileType type, String prefix, String fileType ) {

        this.prefix = prefix;
        this.fileType = fileType;

        String message;
        absoluteStartTime = System.currentTimeMillis();

        // Define a unique file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US );
        timeStamp = dateFormat.format(new Date());

        logWriter = createFile( true );

        switch (type) {
            case Details:
                message = "Filter,Time Stamp (ms),Ang Vel,Linear Vel x,Linear Vel y,Err x,Err y,Heading (deg)";
                localLog( logWriter, message );
                break;
            case Action:
                message = ",Delta,Start,Start,Start,End,End,End,Delta,Delta,Delta";
                localLog( logWriter, message );
                message = ",Time,Heading,X,Y,Heading,X,Y,Heading,X,Y";
                localLog( logWriter, message );
                break;
        }
    }

    /**
     *
     * @param message - string of characters to be written with a appended <NL>
     */
    public void log(String message) {
        localLog( logWriter, message );
    }

//    /**
//     *
//     * @param vector - heading and position
//     * @param error - error in x, y, & heading
//     */
//    public void logDetails(PoseVelocity2d vector, Pose2d error) {
//        deltaTime = System.currentTimeMillis() - absoluteStartTime;
//        int filter = 0;
//        String message = filter + "," +
//                String.format(Locale.US, "%.4f", (deltaTime/1000.0)) + "," +
//                String.format(Locale.US, "%.4f", vector.angVel) + "," +
//                String.format(Locale.US, "%.4f", vector.linearVel.x) + "," +
//                String.format(Locale.US, "%.4f", vector.linearVel.y) + "," +
//                String.format(Locale.US, "%.4f", error.position.x) + "," +
//                String.format(Locale.US, "%.4f", error.position.y) + "," +
//                String.format(Locale.US, "%.4f", error.heading.real);
//
//        localLog( logWriter, message );
//    }
//
//    /**
//     *
//     * @param vector - heading and position
//     */
//    public void logDetails(PoseVelocity2d vector) {
//        deltaTime = System.currentTimeMillis() - absoluteStartTime;
//        int filter = 0;
//        String message = filter + "," +
//                String.format(Locale.US, "%.4f", (deltaTime/1000.0)) + "," +
//                String.format(Locale.US, "%.4f", vector.angVel) + "," +
//                String.format(Locale.US, "%.4f", vector.linearVel.x) + "," +
//                String.format(Locale.US, "%.4f", vector.linearVel.y);
//
//        localLog( logWriter, message );
//    }
//
//    /**
//     *
//     * @param pose - x & y position of the robot
//     * @param label - text string used to describe the sample
//     */
//    public void logAction( Pose2d pose, String label ) {
//
//        this.label   = label;
//        startTime    = System.currentTimeMillis();
//        startHeading = Math.toDegrees(pose.heading.toDouble());
//        startPoseX   = pose.position.x;
//        startPoseY   = pose.position.y;
//    }
//
//    /**
//     *
//     * @param _pose - x & y position of the robot
//     */
//    public void logAction( Pose2d _pose ) {
//
//        deltaTime  = System.currentTimeMillis() - startTime;
//        double endHeading = Math.toDegrees(_pose.heading.toDouble());
//        double endPoseX   = _pose.position.x;
//        double endPoseY   = _pose.position.y;
//
//        String message = this.label + "," +
//                String.format(Locale.US, "%.2f", (double) deltaTime / 1000.01) + "," +
//                String.format(Locale.US, "%.2f", startHeading) + "," +
//                String.format(Locale.US, "%.2f", startPoseX) + "," +
//                String.format(Locale.US, "%.2f", startPoseY) + "," +
//                String.format(Locale.US, "%.2f", endHeading) + "," +
//                String.format(Locale.US, "%.2f", endPoseX) + "," +
//                String.format(Locale.US, "%.2f", endPoseY) + "," +
//                String.format(Locale.US, "%.2f", startHeading - endHeading) + "," +
//                String.format(Locale.US, "%.2f", startPoseX - endPoseX) + "," +
//                String.format(Locale.US, "%.2f", startPoseY - endPoseY);
//        localLog( logWriter, message );
//    }
//
//    public void logDelta( Pose2d _start, Pose2d _target ) {
//        String message;
//
//        logDelta = createFile( false );
//
//        localLog( logDelta, timeStamp );
//        message = "Start,Start,Start,Target,Target,Target,Delta,Delta,Delta";
//        localLog( logDelta, message );
//        message = "X,Y,Head,X,Y,Head,X,Y,Head";
//        localLog( logDelta, message );
//
//        message = String.format(Locale.US, "%.2f", _start.position.x) + "," +
//                  String.format(Locale.US, "%.2f", _start.position.y) + "," +
//                  String.format(Locale.US, "%.2f", Math.toDegrees(_start.heading.toDouble())) + "," +
//                  String.format(Locale.US, "%.2f", _target.position.x) + "," +
//                  String.format(Locale.US, "%.2f", _target.position.y) + "," +
//                  String.format(Locale.US, "%.2f", Math.toDegrees(_target.heading.toDouble())) + "," +
//                  String.format(Locale.US, "%.2f", (_start.position.x-_target.position.x)) + "," +
//                  String.format(Locale.US, "%.2f", (_start.position.y-_target.position.y)) + "," +
//                  String.format(Locale.US, "%.2f", (Math.toDegrees(_start.heading.toDouble()))-Math.toDegrees(_target.heading.toDouble()));
//        localLog( logDelta, message );
//    }

    FileWriter createFile(boolean useDate) {

        FileWriter newFile = null;
        String fileName;

        logFolder = Environment.getExternalStorageDirectory().getPath(); // /storage/emulated/0 also maps to /sdcard

        if (useDate) {
            fileName = logFolder + "/FIRST/logs/" + prefix + "_" + timeStamp + "." + fileType;
        } else {
            fileName = logFolder + "/FIRST/logs/deltas.csv";
        }

        try {
            File logFile = new File(fileName);
            fullPath = logFile.getPath();
            if (!logFile.exists()) {
                newFile = new FileWriter(logFile);
            } else {
                newFile = new FileWriter(logFile, true); // Append mode
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    private void localLog(FileWriter file, String message) {
        try {
            file.write(message + "\n");
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
