package latipe.rating.configs;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "latipe.rating.repositories")
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  @Bean
  public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
    return new MongoTransactionManager(mongoDatabaseFactory);
  }

  @Override
  protected String getDatabaseName() {
    return "Latipe-Rating-DB";
  }

  @Override
  public MongoClient mongoClient() {
    ConnectionString connectionString = new ConnectionString(mongoUri);
    return MongoClients.create(connectionString);
  }

  @Override
  public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory,
      MongoCustomConversions customConversions, MongoMappingContext mappingContext) {
    DbRefResolver dbRefResolver = new DefaultDbRefResolver(databaseFactory);
    MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver,
        mappingContext);
    mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return mappingConverter;
  }
}



