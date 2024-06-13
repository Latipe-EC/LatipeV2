package latipe.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class fcommentSQL {

    public static void main(String[] args) {
        String[] codeLines = {
            "wParm.put(\"1\", \"231\")               // qweqweqwewqe",
            "wParm.put(\"1\", \"232221\")    // qweqweqwewqe",
            "wParm.put(\"1\", \"231\")   // qweqweqwewqe",
            "wParm.put(\"1\", \"23221\")         // qweqweqwewqe",
            "wParm.put(\"1\", \"2322221\")   // qweqweqwewqe",
            "",
            "if (isTrue) {",
            "    wParm.put(\"23231\", \"2322221\")   // qweq23232weqwewqe",
            "}",
            "",
            "wParm.put(\"1\", \"2231\")  // qweqweqwewqe",
            "wParm.put(\"1\", \"23222221\")      // qweqweqwewqe",
            "wParm.put(\"1\", \"2231\")  // qweqweqwewqe",
            "",
            "callSelectSql(\"id1\", wParm)",
            "",
            "wParm2.put(\"1\", \"23weqwewqe1\")      //   qweqweqwewqe",
            "wParm2.put(\"1\", \"23we1\")    // qweqwsdseqwewqe",
            "wParm2.put(\"1\", \"23wqwee1\")     //sd",
            "wParm2.put(\"1\", \"22331\")            // sdadas",
            "wParm2.put(\"1\", \"2seqwed31\")            //    sadas",
            "if (isTrue) {",
            "    const a = '1212';",
            "    a = true;",
            "    wParm.put(\"23231\", a)   // qweq23232weqwewqe",
            "}",
            "wParm2.put(\"1\", \"231e21\")   // 213",
            "wParm2.put(\"1\", \"23weaqweqs1\")              // 2131",
            "wParm2.put(\"1\", \"231221\")           //      213123 ",
            "",
            "callSelectSql(\"id1\", wParm2)"
        };

        List<String> data = Arrays.asList(codeLines);
        List<String> formattedLines = addSpacingToIfBlocks(data);
        formattedLines = removeExtraBlankLines(formattedLines);
        formattedLines = formatComments(formattedLines.toArray(new String[0]));
        formattedLines.forEach(System.out::println);
    }

    private static List<String> formatComments(String[] codeLines) {
        List<String> formattedLines = new ArrayList<>();
        List<String> block = new ArrayList<>();
        boolean inBlock = false;

        for (String line : codeLines) {
            if (line.contains("wParm") && !inBlock) {
                inBlock = true;  // Start of a new block
                block.add(line);
            } else if (line.contains("callSelectSql")) {
                if (inBlock) {
                    block.add(line);
                    formattedLines.addAll(formatBlock(block));
                    block.clear();
                    inBlock = false;  // End of the current block
                } else {
                    formattedLines.add(line);  // Outside any block
                }
            } else {
                if (inBlock) {
                    block.add(line);
                } else {
                    formattedLines.add(line);  // Outside any block
                }
            }
        }

        // In case the last block does not end properly
        if (inBlock && !block.isEmpty()) {
            formattedLines.addAll(formatBlock(block));
        }

        return formattedLines;
    }

    private static List<String> formatBlock(List<String> block) {
        int maxCodeLength = 0;
        List<String> formattedBlock = new ArrayList<>();

        // Determine the maximum length of lines before the comment in the block
        for (String line : block) {
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                maxCodeLength = Math.max(maxCodeLength, commentIndex);
            } else {
                maxCodeLength = Math.max(maxCodeLength, line.length());
            }
        }

        // Adjust the comment position based on the longest code part
        for (String line : block) {
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                String codePart = line.substring(0, commentIndex);
                String commentPart = line.substring(commentIndex + 2)
                    .trim(); // Remove original spaces after //
                formattedBlock.add(
                    String.format("%-" + maxCodeLength + "s // %s", codePart, commentPart));
            } else {
                formattedBlock.add(line);
            }
        }

        return formattedBlock;
    }

    // remove extra blank lines
    private static List<String> removeExtraBlankLines(List<String> lines) {
        List<String> processedLines = new ArrayList<>();
        boolean lastWasBlank = false;

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                if (!lastWasBlank) {
                    processedLines.add(line);
                    lastWasBlank = true;
                }
            } else {
                processedLines.add(line);
                lastWasBlank = false;
            }
        }

        return processedLines;
    }

    // add spacing to if blocks
    private static List<String> addSpacingToIfBlocks(List<String> lines) {
        List<String> formattedLines = new ArrayList<>();
        int depth = 0; // Track the nested depth of if blocks

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmedLine = line.trim();

            // Check if it's the start of an if block
            if (trimmedLine.startsWith("if (") || trimmedLine.startsWith("} else if (")
                || trimmedLine.startsWith("} else {")) {
                if (depth == 0 && i > 0 && !lines.get(i - 1).trim().isEmpty()) {
                    formattedLines.add("");
                }
                depth++;
            }

            formattedLines.add(line);

            // Check if it's the end of an if block
            if (trimmedLine.endsWith("}") && depth > 0) {
                depth--;
                if (depth == 0 && i < lines.size() - 1 && !lines.get(i + 1).trim().isEmpty()) {
                    formattedLines.add("");
                }
            }
        }

        return formattedLines;
    }
}

