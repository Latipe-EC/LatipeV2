package latipe.media.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static latipe.media.constants.CONSTANTS.*;

public class FileCategorizeUtils {
    public static String categorizeFile(String fileName) {
        Path path = Paths.get(fileName);
        String fileExtension = getFileExtension(path);
        return switch (fileExtension.toLowerCase()) {
            case "mp4", "avi", "mov" -> VIDEO;
            case "jpg", "jpeg", "png", "gif" -> IMAGE;
            case "mp3", "wav", "flac" -> AUDIO;
            case "zip", "rar", "tar" -> ZIP;
            case "doc", "docx", "pdf" -> DOCUMENT;
            default -> OTHER;
        };
    }

    private static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }


}