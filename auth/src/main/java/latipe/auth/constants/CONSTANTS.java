package latipe.auth.constants;

public class CONSTANTS {

    public static final String REQUEST_ID = "req-id";
    public static final String ENCRYPTION_KEY = "Jf27atfgC5X1tktm";


    public static enum TOKEN_TYPE {
        RESET_PASSWORD(1), VERIFY_EMAIL(2);


        private final int value;

        TOKEN_TYPE(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

}
