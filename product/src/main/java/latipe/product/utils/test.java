package latipe.product.utils;

import static java.lang.System.out;

import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base32;

public class test {

  public static void main(String[] args) {
      byte[] buffer = new byte[32];
      new SecureRandom().nextBytes(buffer);
      out.println(new String(new Base32().encode(buffer)));
  }

}
