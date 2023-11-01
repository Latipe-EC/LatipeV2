package latipe.product.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import latipe.product.entity.Category;
import latipe.product.entity.attribute.Attribute;
import latipe.product.repositories.ICategoryRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


@Component
public class CommandHandler {

  private final ICategoryRepository categoryRepository;

  public CommandHandler(ICategoryRepository categoryRepository) throws IOException {
    this.categoryRepository = categoryRepository;
//    handleCommand(categoryRepository);
//    addAttribute(categoryRepository);
  }

  @PostConstruct
  public void init() {

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