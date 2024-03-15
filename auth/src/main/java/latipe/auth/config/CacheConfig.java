package latipe.auth.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

@Configuration
@EnableCaching
public class CacheConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfig.class);

  @Value("${spring.cache.config.entryTtl-access-token:60}")
  private int entryTokenTtl;

  @Value("${spring.cache.config.entryTtl-data:60}")
  private int entryDataTtl;

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    LOGGER.info("Start redis in [Auth service]");

    return (builder) -> builder
        .withCacheConfiguration("token",
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(entryTokenTtl)))
        .withCacheConfiguration("dataCache",
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(entryDataTtl)));
  }
}
