package pedroPathing;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.pedropathing.localization.Pose;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.io.*;
import java.util.*;

public class CSVReader {
    public List<String[]> values = new ArrayList<>();
    String filePath;
    FileWriter csvFile = null;
    File poseFile = null;

    public CSVReader(String fileName, boolean openForWrite) {

        String poseFolder = Environment.getExternalStorageDirectory().getPath(); // /storage/emulated/0 also maps to /sdcard

        filePath = poseFolder + "/FIRST/logs/" + fileName;

        if (openForWrite) {
            try {
                poseFile = new File(filePath);
                csvFile = new FileWriter(poseFile, false); // Append mode
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void readFile() {
        String line;
        String[] oneLine;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                oneLine = line.split(","); // Splitting by comma
                values.add(oneLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    public void logPose(Pose pose) {
        String message = String.format("%.4f", pose.getX()) + "," +
                         String.format("%.4f", pose.getY()) + "," +
                         String.format("%.4f", pose.getHeading());

        try {
            csvFile.write(message + "\n");
            csvFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() throws IOException {
        csvFile.close();
    }
}
