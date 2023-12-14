package latipe.product.utils;

import static java.lang.System.out;

public class test {

  public static void main(String[] args) {
    byte[] buffer = new byte[32];
   out.println(convert("PAYPALISHIRING", 3));
    out.println(convert("PAYPALISHIRING", 4));
    out.println(convert("A", 1));
  }

  public static String convert(String s, int numRows) {
    int len = s.length();
    String res = "";
    for (int i = 0; i < numRows; i++) {
      int run = i;
      if (i == 0 || i == numRows - 1)
        while (run < len) {
          res += s.charAt(run);
          run += 2 * numRows - 2;
        }else{
        while (run < len) {
          res += s.charAt(run);
          if (len > run + 2 * numRows - 2 - 2 * i) {
            res += s.charAt(run + 2 * numRows - 2 - 2 * i);
          }
          if ( 2 * numRows - 2 ==0)
            break;
          run += 2 * numRows - 2;
        }
      }
    }
    return res;
  }
}
