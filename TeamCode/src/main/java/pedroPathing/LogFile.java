package pedroPathing;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.pedropathing.localization.Pose;
import com.pedropathing.util.CustomPIDFCoefficients;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFile {

    private final String prefix;
    private final String fileType;
    String logFolder;
    String message;
    boolean isFirst = true;

    private FileWriter logWriter = null;

    long absoluteStartTime;

    String timeStamp;

    /**
     * Note: For this method to work you have to be sure that the
     *       folder "/sdcard/FIRST/logs" exists on the Control Hub
     *
     * @param prefix - root name of the file that will have a time stamp added
     * @param fileType - file extension (typically "csv" or "txt")
     */
    public LogFile( String prefix, String fileType ) {

        this.prefix = prefix;
        this.fileType = fileType;

        absoluteStartTime = System.currentTimeMillis();

        // Define a unique file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US );
        timeStamp = dateFormat.format(new Date());

        logWriter = createFile();
    }

    /**
     *
     * @return name of the created file
     */
    FileWriter createFile() {

        // Create a file to save pose positions with every loop thru 'MecanumDrive'
        FileWriter newFile = null;
        String fileName;

        logFolder = Environment.getExternalStorageDirectory().getPath(); // /storage/emulated/0 also maps to /sdcard

        fileName = logFolder + "/FIRST/logs/" + prefix + "_" + timeStamp + "." + fileType;

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
     * @param offset - numerical order to keep data in line
     * @param target - pose to achieve
     * @param pose - heading and position
     * @param coef - set of PID constants
     */
    @SuppressLint("DefaultLocale")
    public void logStraightBackAndForth(int offset, double target, Pose pose, CustomPIDFCoefficients coef) {
        if (isFirst) {
            isFirst = false;
            message = "P=" + String.format("%.4f", coef.P);
            localLog( logWriter, message );
            message = "I=" + String.format("%.4f", coef.I);
            localLog( logWriter, message );
            message = "D=" + String.format("%.4f", coef.D);
            localLog( logWriter, message );
            message = "offset,target,X,Y,Head";
            localLog( logWriter, message );
        } else {
            message = offset + "," +
                    String.format("%.4f", target) + "," +
                    String.format("%.4f", pose.getX()) + "," +
                    String.format("%.4f", pose.getY()) + "," +
                    String.format("%.4f", Math.toDegrees(pose.getHeading()));
            localLog( logWriter, message );
        }
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
