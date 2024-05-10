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
        // Get the time from the line
        String time = getTime(line);

        // Calculate the time difference from the last time
        int timeDifference = calculateTimeDifference(time);

        // Get the encoded part of the line
        String encodedString = getEncodedString(line);

        // -1 for the first time. If we are not at the first time, decode the byte
        if (timeDifference != -1) {
            // Get the byte if there are 7 bits
            String character = getByte(getBits(timeDifference));

            // Display the whole byte if it's not empty
            if (!Objects.equals(character, "")) {
//                System.out.print(character);
            }
        }

        // Decode the base64 string
        try {
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(encodedString);

            // Get the SQL type of the line
            previousSQLType = getSQLType(new String(decodedBytes));

            // print the decoded line
            System.out.println(new String(decodedBytes));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid base64 string: " + encodedString);
        }
    }

    /**
     * There are 2 types of SQL queries. The first type has 2 bits, the second type has 1 bit.
     * If the line contains "),7,1" it's the second type, otherwise it's the first type.
     * Return true if it's the first type, false if it's the second type.
     *
     * @param line
     * @return Boolean
     */
    private static Boolean getSQLType(String line) {
        return !line.contains("),7,1");
    }

    /**
     * Get the encoded part of from the line
     *
     * @param line
     * @return String
     */
    private static String getEncodedString(String line) {
        // There are 2 patterns for the encoded string
        Pattern pattern = Pattern.compile("&order=(.*?)%3D");
        Matcher matcher = pattern.matcher(line);

        Pattern pattern2 = Pattern.compile("&order=(.*?) ");
        Matcher matcher2 = pattern2.matcher(line);

        // If the first pattern is found, return this.
        // Else, return the second pattern
        if (matcher.find()) {
            return matcher.group(1);
        } else if (matcher2.find()) {
            return matcher2.group(1);
        }

        return null;
    }

    /**
     * Get the time from the line
     * From "30/Mar/2024:12:12:54 +0200" get "12:12:54"
     *
     * @param line
     * @return String
     */
    private static String getTime(String line) {
        Pattern pattern = Pattern.compile(":(\\d{2}:\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Calculate the time difference between lastTime and the time
     *
     * @param time
     * @return int
     */
    private static int calculateTimeDifference(String time) {
        if (lastTime == null) {
            lastTime = time;
            return -1;
        }

        // Split the time in hours, minutes and seconds
        String[] lastTimeParts = lastTime.split(":");
        String[] timeParts = time.split(":");

        // Calculate the time in seconds
        int lastTimeSeconds = Integer.parseInt(lastTimeParts[0]) * 3600 + Integer.parseInt(lastTimeParts[1]) * 60 + Integer.parseInt(lastTimeParts[2]);
        int timeSeconds = Integer.parseInt(timeParts[0]) * 3600 + Integer.parseInt(timeParts[1]) * 60 + Integer.parseInt(timeParts[2]);

        // Calculate the difference
        int difference = timeSeconds - lastTimeSeconds;

        lastTime = time;

        return difference;
    }

    /**
     * Get the bits based on time difference
     *
     * @param timeDifference
     * @return String
     */
    private static String getBits(int timeDifference) {
        String bits = "";

        // Based on previousSQLType, add 2 bits or 1 bit
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

        return bits;
    }

    /**
     * Get the byte based on the bits
     * If currentBytes are <7, add the bits to it
     * If currentBytes are 7, return the byte and convert it to ascii
     *
     * @param bits
     * @return String
     */
    private static String getByte(String bits) {
        currentByte += bits;

        if (currentByte.length() == 7) {
            String byteToReturn = currentByte;
            currentByte = "";

            int charCode = Integer.parseInt(byteToReturn, 2);

            return Character.toString((char) charCode);
        }

        return "";
    }

}
