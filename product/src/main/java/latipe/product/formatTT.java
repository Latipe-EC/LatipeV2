package latipe.product;

import java.util.ArrayList;
import java.util.List;

public class formatTT {

    public static void main(String[] args) {
        String[] codeLines = {
            "//  222",
            "war.12122;",
            "",
            "  // 21312",
            "   // 12312",
            "// 1212",
            "param.cl()"
        };

        List<String> formattedLines = alignComments(codeLines);
        for (String line : formattedLines) {
            System.out.println(line);
        }
    }

    private static List<String> alignComments(String[] codeLines) {
        List<String> formattedLines = new ArrayList<>();
        String lastNonCommentLine = "";

        for (String line : codeLines) {
            line = line.replaceAll("\\s+$", "");  // Trim trailing whitespace

            if (line.trim().startsWith("//")) {
                // This is a standalone comment line
                formattedLines.add(
                    formatCommentLine(line.trim()));  // Format and add the standalone comment
            } else if (line.contains("//")) {
                // Code line with a comment
                int commentIndex = line.indexOf("//");
                String codePart = line.substring(0, commentIndex);
                String commentPart = line.substring(commentIndex);

                formattedLines.add(codePart + formatCommentLine(
                    commentPart));  // Format and add the inline comment
                lastNonCommentLine = codePart;
            } else {
                // Regular code line
                if (!line.trim().isEmpty()) {
                    lastNonCommentLine = line;
                }
                formattedLines.add(line);
            }
        }

        return formattedLines;
    }

    private static String formatCommentLine(String comment) {
        // Ensure there is exactly one space after "//"
        return "// " + comment.substring(2).trim();
    }
}