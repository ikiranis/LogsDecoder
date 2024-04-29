import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static String lastTime = null;
    private static String currentByte = "";
    private static Boolean previousSQLType = true;

    public static void main(String[] args) {
        // check if the file path is provided
        if (args.length == 0) {
            System.out.println("Please provide the file path as an argument");
            return;
        }

        // parse the file
        parseFile(args[0]);
    }

    /**
     * Read the file and decode the lines
     *
     * @param path
     */
    public static void parseFile(String path) {
        // read the file
        try {
            Files.lines(Paths.get(path)).forEach(Main::decodeLine);
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }

    }

    /**
     * Decode the line, from base64 and print it
     *
     * @param line
     */
    public static void decodeLine(String line) {
//        System.out.println("Line: " + line);
        String time = getTime(line);

        int timeDifference = calculateTimeDifference(time);

        String encodedString = getEncodedString(line);

        // -1 for the first time
        if (timeDifference != -1) {
//            System.out.println(getBits(timeDifference));
            String character = getByte(getBits(timeDifference));

            // Display the whole byte
            if (!Objects.equals(character, "")) {
                System.out.print(character);
            }
        }

        // decode the line
        try {
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(encodedString);

            previousSQLType = getSQLType(new String(decodedBytes));

            // print the decoded line
//            System.out.println(new String(decodedBytes));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid base64 string: " + encodedString);
        }
    }

    private static Boolean getSQLType(String line) {
        return !line.contains("),7,1");
    }

    private static String getEncodedString(String line) {
        Pattern pattern = Pattern.compile("&order=(.*?)%3D");
        Matcher matcher = pattern.matcher(line);

        Pattern pattern3 = Pattern.compile("&order=(.*?) ");
        Matcher matcher3 = pattern3.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        } else if (matcher3.find()) {
            return matcher3.group(1);
        }

        return null;
    }

    // From "30/Mar/2024:12:12:54 +0200" get "12:12:54"
    private static String getTime(String line) {
        Pattern pattern = Pattern.compile(":(\\d{2}:\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Calculate the time difference between lastTime and the time
    private static int calculateTimeDifference(String time) {
        if (lastTime == null) {
            lastTime = time;
            return -1;
        }

        String[] lastTimeParts = lastTime.split(":");
        String[] timeParts = time.split(":");

        int lastTimeSeconds = Integer.parseInt(lastTimeParts[0]) * 3600 + Integer.parseInt(lastTimeParts[1]) * 60 + Integer.parseInt(lastTimeParts[2]);
        int timeSeconds = Integer.parseInt(timeParts[0]) * 3600 + Integer.parseInt(timeParts[1]) * 60 + Integer.parseInt(timeParts[2]);

        int difference = timeSeconds - lastTimeSeconds;

        lastTime = time;

//        System.out.println(lastTime);

        return difference;
    }

    private static String getBits(int timeDifference) {
        String bits = "";

        if (previousSQLType) {
            switch (timeDifference) {
                case 0 -> bits = "00";
                case 2 -> bits = "01";
                case 4 -> bits = "10";
                case 6 -> bits = "11";
            }
        } else {
            switch (timeDifference) {
                case 0, 2 -> bits = "0";
                case 4 -> bits = "1";
            }
        }

//        System.out.println(previousSQLType + ": " + timeDifference + " --> " + bits);

        return bits;
    }

    // If currentBytes are <8, add the bits to it
    // If currentBytes are 8, return the byte and convert it to ascii
    private static String getByte(String bits) {
        currentByte += bits;
//        System.out.println(currentByte);
        if (currentByte.length() == 7) {
            String byteToReturn = currentByte;
            currentByte = "";

            int charCode = Integer.parseInt(byteToReturn, 2);

//            return byteToReturn + " " + Character.toString((char) charCode);
            return Character.toString((char) charCode);
        }
        return "";
    }

}
