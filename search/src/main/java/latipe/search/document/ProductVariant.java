package latipe.search.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant   {
    String id;
    String name;
    String image;
    List<Options> options;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        String value;
    }
}
