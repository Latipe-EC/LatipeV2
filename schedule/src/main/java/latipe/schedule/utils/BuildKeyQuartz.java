package latipe.schedule.utils;

public class BuildKeyQuartz {

    public static final String CANCEL_ORDER = "cancel-order-%s";
    private static final String TRIGGER = "trigger-%s-%s";
    private static final String JOB = "job-%s-%s";

    public static String buildJobKey(String type, String id) {
        return JOB.formatted(type, id);
    }

    public static String buildTriggerKey(String type, String id) {
        return TRIGGER.formatted(type, id);
    }
}
