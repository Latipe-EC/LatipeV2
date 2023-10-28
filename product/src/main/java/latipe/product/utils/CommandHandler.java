package latipe.product.utils;

import jakarta.annotation.PostConstruct;
import latipe.product.entity.Category;
import latipe.product.repositories.ICategoryRepository;
import org.springframework.stereotype.Component;


@Component

public class CommandHandler {

  private final ICategoryRepository categoryRepository;

  public CommandHandler(ICategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @PostConstruct
  public void init() {
    // Check if role exists and create role if it does not exist
    if (!categoryRepository.existsByName("electric")) {
      Category category = new Category("electric");
      categoryRepository.save(category);
    }
    if (!categoryRepository.existsByName("phone")) {
      Category category = new Category("phone");
      categoryRepository.save(category);
    }

  }

  public void handleCommand() {
    // Handle command here
  }
}