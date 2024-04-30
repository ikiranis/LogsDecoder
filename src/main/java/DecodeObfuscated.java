import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DecodeObfuscated {
    public static void main(String[] args) {
        // check if the file path is provided
        if (args.length == 0) {
            System.out.println("Please provide the file path as an argument");
            return;
        }

        // parse the file
        String fileContent = readFile(args[0]);

        fileContent = splitLinesOnQuestionMark(fileContent);

        System.out.println(fileContent);

        fileContent = concatenateStrings(fileContent);

        System.out.println(fileContent);
    }

    // Get file content to String
    private static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
            return "";
        }
    }

   private static String splitLinesOnQuestionMark(String fileContent) {
        String[] lines = fileContent.split("\\;");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line).append(";\n");
        }
        return result.toString();
    }

    // Concatenate strings when in format (('S'+'il')+('en'+'t')+'ly'+('Cont'+'i'+'nue'));
    private static String concatenateStrings(String fileContent) {
        String[] lines = fileContent.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            if (line.contains("+")) {
                String[] parts = line.split("\\+");
                StringBuilder concatenatedString = new StringBuilder();
                for (String part : parts) {
                    concatenatedString.append(part.replace("(", "").replace(")", "").replace("'", ""));
                }
                result.append(concatenatedString).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }
        return result.toString();

    }
}
