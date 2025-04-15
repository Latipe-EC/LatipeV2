package latipe.payment.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Utility class for handling message internationalization and formatting.
 * Provides methods to retrieve localized messages from resource bundles and format them with parameters.
 */
public class MessagesUtils {

    static ResourceBundle messageBundle = ResourceBundle.getBundle("messages.messages",
        Locale.getDefault());

    private MessagesUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Retrieves a localized message and formats it with the provided parameters.
     *
     * @param errorCode The key to look up in the message resource bundle
     * @param var2 Optional parameters to be inserted into the message
     * @return The formatted message string
     */
    public static String getMessage(String errorCode, Object... var2) {
        String message;
        try {
            message = messageBundle.getString(errorCode);
        } catch (MissingResourceException ex) {
            // case message_code is not defined.
            message = errorCode;
        }
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(message, var2);
        return formattingTuple.getMessage();
    }
}
