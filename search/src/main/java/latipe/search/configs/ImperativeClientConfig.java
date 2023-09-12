package latipe.search.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "latipe.search.repositories")
@ComponentScan(basePackages = "latipe.search.services")
@RequiredArgsConstructor
public class ImperativeClientConfig extends ElasticsearchConfiguration {

    private final ElasticsearchDataConfig elasticsearchConfig;
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchConfig.getUrl())
                .withBasicAuth(elasticsearchConfig.getUsername(), elasticsearchConfig.getPassword())
                .build();
    }
}
