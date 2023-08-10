package latipe.store.services.store.Dtos;

import lombok.Data;

@Data
public class StoreUpdateDto extends StoreCreateDto {


    public class UpdateProductVariant {
        int index;
        int type;
        String name;

    }
}

