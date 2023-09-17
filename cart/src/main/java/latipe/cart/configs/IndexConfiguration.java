package latipe.cart.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

@Component
public class IndexConfiguration implements CommandLineRunner {
    @Value("${app.cicd.skip-command-line-runners:false}")
    private boolean skipCommandLineRunners;
    private final MongoTemplate mongoTemplate;
    public IndexConfiguration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    @Override
    public void run(String... args) {
        if (skipCommandLineRunners) {
            return;
        }
        createUniqueIndexIfNotExists(mongoTemplate);
    }
    private void createUniqueIndexIfNotExists(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps("Carts");
        IndexDefinition indexDefinition = new Index().on("userID", Sort.Direction.ASC).unique();
        indexOperations.ensureIndex(indexDefinition);
    }
}

