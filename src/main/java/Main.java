import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
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
        Pattern pattern = Pattern.compile("&order=(.*?)%3D");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String encodedString = matcher.group(1);

            // decode the line
            try {
                byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(encodedString);

                // print the decoded line
                System.out.println(new String(decodedBytes));
            } catch(IllegalArgumentException e) {
                System.out.println("Invalid base64 string: " + encodedString);
            }
        }

    }
}
