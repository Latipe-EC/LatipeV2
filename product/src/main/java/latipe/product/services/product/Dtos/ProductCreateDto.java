package latipe.product.services.product.Dtos;

import jakarta.validation.constraints.NotEmpty;
import latipe.product.Entity.ProductClassification;
import latipe.product.Entity.ProductVariant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductCreateDto {
    @NotEmpty(message = "Product Name  is required")
    private String name;
    @NotEmpty(message = "Product Description  is required")
    private String description;
    private Double price;
    private List<String> images = new ArrayList<>();
    private int quantity;
    List<ProductVariant> productVariant= new ArrayList<>();
    List<ProductClassification> productClassifications = new ArrayList<>();
}