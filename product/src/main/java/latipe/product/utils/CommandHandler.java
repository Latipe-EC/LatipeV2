package latipe.product.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import latipe.product.entity.Category;
import latipe.product.entity.attribute.Attribute;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CommandHandler {

  private final ICategoryRepository categoryRepository;
  private final IProductRepository productRepository;


  @PostConstruct
  public void init() {
//    var products = productRepository.findAll();
//
//    List<String> uniqueImages = products.stream()
//        .flatMap(product -> product.getImages().stream())
//        .distinct()
//        .toList();
//    String imageFolder = "/home/cozark/Pictures/test_dl";
//    uniqueImages = uniqueImages.subList(1171, uniqueImages.size());
//    for (String imageUrl : uniqueImages) {
//      try {
//        URL url = new URL(imageUrl);
//        String fileName = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
//        Path outputPath = Path.of(imageFolder, fileName);
//
//        try (InputStream in = url.openStream()) {
//          Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
//        }
//      } catch (Exception e) {
//        System.out.println("Error downloading image: " + imageUrl);
//        e.printStackTrace();
//      }
//    }

//    var products = productRepository.findAll();
//    for (var product : products) {
//      if(product.getPrice() == 0) {
//        product.setPrice(product.getProductClassifications().get(0).getPrice());
//      }
//      if (product.getPromotionalPrice() >= product.getPrice()) {
//        Random rand = new Random();
//        int randomNum = rand.nextInt(20) + 1;
//        product.setPromotionalPrice(product.getPrice() - product.getPrice() * randomNum / 100);
//      }
//      for (var classify : product.getProductClassifications()) {
//        if (classify.getPromotionalPrice() >= classify.getPrice()) {
//          Random rand = new Random();
//          int randomNum = rand.nextInt(20) + 1;
//          classify.setPromotionalPrice(classify.getPrice() - classify.getPrice() * randomNum / 100);
//        }
//      }
//    }
//    productRepository.saveAll(products);
//        handleCommand(categoryRepository);
//    addAttribute(categoryRepository);
  }

  public void addAttribute(ICategoryRepository categoryRepository) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Category> categories = categoryRepository.findAll();
    ClassPathResource resource = new ClassPathResource("attribute.json");

    List<Attribute> attributes = objectMapper.readValue(resource.getInputStream(),
        new TypeReference<List<Attribute>>() {
        });
    for (Category category : categories) {
      if (category.getParentCategoryId() != null) {
        category.setAttributes(attributes);
        categoryRepository.save(category);
      }
    }
  }

  public void handleCommand(ICategoryRepository categoryRepository) throws IOException {
    ClassPathResource resource = new ClassPathResource("parent.json");

    // Read the file content and map it to a list of strings
    ObjectMapper objectMapper = new ObjectMapper();
    List<cateobj> categories = objectMapper.readValue(resource.getInputStream(),
        new TypeReference<List<cateobj>>() {
        });

    for (cateobj category : categories) {
      // default data
      if (category.getName().contains("deda")) {
        for (String data : category.getData()) {
          if (categoryRepository.findByName(data) == null) {
            Category cate = new Category(data);
            categoryRepository.save(cate);
          }
        }
        ;
      } else {
        Category parCate = categoryRepository.findByName(category.getName());
        if (categoryRepository.findByName(category.getName()) == null) {
          parCate = new Category(category.getName());
          parCate = categoryRepository.save(parCate);
          for (String data : category.getData()) {
            Category cate = categoryRepository.findByName(data);
            if (cate == null) {
              cate = new Category(data);
            }
            cate.setParentCategoryId(parCate.getId());
            categoryRepository.save(cate);
          }
        } else {
          for (String data : category.getData()) {
            Category cate = categoryRepository.findByName(data);
            if (cate == null) {
              cate = new Category(data);
            }
            cate.setParentCategoryId(parCate.getId());
            categoryRepository.save(cate);
          }
        }
      }
    }
  }
}

//{       $or: [         { parentCategoryId: null },         { parentCategoryId: { $exists: false } }       ]     }