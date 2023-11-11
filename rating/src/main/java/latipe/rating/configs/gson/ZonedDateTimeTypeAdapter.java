package latipe.rating.configs.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {

  @Override
  public void write(JsonWriter out, ZonedDateTime value) throws IOException {
    Instant instant = value.toInstant();
    if (instant == null) {
      out.nullValue();
      return;
    }
    out.value(instant.toString());
  }

  @Override
  public ZonedDateTime read(JsonReader in) throws IOException {
    Instant instant = Instant.parse(in.nextString());
    if (instant == null) {
      return null;
    }
    return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
  }
}