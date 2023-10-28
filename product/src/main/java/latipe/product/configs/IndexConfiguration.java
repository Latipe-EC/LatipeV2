package latipe.product.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

//@Component
public class IndexConfiguration implements CommandLineRunner {

  private final MongoTemplate mongoTemplate;
  @Value("${app.cicd.skip-command-line-runners:false}")
  private boolean skipCommandLineRunners;

  public IndexConfiguration(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void run(String... args) {
    if (skipCommandLineRunners) {
      return;
    }
    createUniqueIndexIfNotExists(mongoTemplate, "name");
  }

  private void createUniqueIndexIfNotExists(MongoTemplate mongoTemplate, String fieldName) {
    IndexOperations indexOperations = mongoTemplate.indexOps("Categories");
    IndexDefinition indexDefinition = new Index().on(fieldName, Sort.Direction.ASC).unique();
    indexOperations.ensureIndex(indexDefinition);
  }
}

