package latipe.product.services.product.Dtos;

import lombok.Data;

@Data
public class ProductUpdateDto extends ProductCreateDto {


    public class UpdateProductVariant {
        int index;
        int type;
        String name;

    }
}

