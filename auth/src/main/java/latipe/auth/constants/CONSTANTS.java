package latipe.auth.constants;

public class CONSTANTS {
  public static enum TOKEN_TYPE {
    RESET_PASSWORD(1), VERIFY_EMAIL(2);

    private final int value;

    private TOKEN_TYPE(int value) {
      this.value = value;
    }

    public int getValue() {
      return this.value;
    }
  }

}
