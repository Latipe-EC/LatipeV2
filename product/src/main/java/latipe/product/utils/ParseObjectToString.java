package latipe.product.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseObjectToString {

  public static ObjectMapper objectMapper = new ObjectMapper();

  public static String parse(Object obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }

}
