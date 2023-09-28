package latipe.product.utils;

import com.google.gson.Gson;

public class ParseObjectToString {

  Gson gson = new Gson();

  public static String parse(Object obj) {
    return obj.toString();
  }

}
