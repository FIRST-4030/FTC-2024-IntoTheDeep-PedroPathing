package pedroPathing;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.pedropathing.localization.Pose;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFile {

    private String prefix = null;
    private String fileType = null;
    private String poseFile = null;
    String logFolder, poseFolder;
    String message;

    private FileWriter logWriter = null, posePositions = null;

    long absoluteStartTime = 0;

    String timeStamp;

    /**
     * Note: For this method to work you have to be sure that the
     *       folder "/sdcard/FIRST/logs" exists on the Control Hub
     *
     * @param prefix - root name of the file that will have a time stamp added
     * @param fileType - file extension (typically "csv" or "txt")
     */
    public LogFile(String prefix, String fileType ) {

        this.prefix = prefix;
        this.fileType = fileType;

        absoluteStartTime = System.currentTimeMillis();

        // Define a unique file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US );
        timeStamp = dateFormat.format(new Date());

        logWriter = createFile( true );

        message = "Filter,Time Stamp (ms),x,y,Heading (deg)";
        localLog( logWriter, message );
    }

    /**
     *
     * @param useDate boolean used to indicate whether the file name should include the current date & time
     * @return name of the created file
     */
    FileWriter createFile(boolean useDate) {

        // Create a file to save pose positions with every loop thru 'MecanumDrive'
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

    /**
     *
     * @param message - string of characters to be written with a appended <NL>
     */
    public void log(String message) {
        localLog( logWriter, message );
    }

    /**
     *
     * @param pose - heading and position
     */
    @SuppressLint("DefaultLocale")
    public void logDetails(Pose pose) {
        long deltaTime = System.currentTimeMillis() - absoluteStartTime;
        int filter = 0;
        message = filter + "," +
                String.format("%.4f", (deltaTime /1000.0)) + "," +
                String.format("%.4f", pose.getX()) + "," +
                String.format("%.4f", pose.getY()) + "," +
                String.format("%.4f", Math.toDegrees(pose.getHeading()));

        localLog( logWriter, message );
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
