//package latipe.search.configs;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.ElasticsearchTransport;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import java.io.IOException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import javax.net.ssl.SSLContext;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
//import org.apache.http.ssl.SSLContextBuilder;
//import org.apache.http.ssl.SSLContexts;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestClientBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.util.ResourceUtils;
//
///**
// * Configuration class for Elasticsearch client setup.
// * Configures the connection to Elasticsearch with proper authentication and SSL settings.
// */
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class ElasticsearchConfig {
//
//    private final ElasticsearchProperty elasticsearchProperty;
//
//    /**
//     * Creates and configures the Elasticsearch client bean.
//     * Sets up authentication, SSL, and other connection parameters based on properties.
//     *
//     * @return Configured ElasticsearchClient ready for use
//     * @throws CertificateException If there's an issue with SSL certificates
//     * @throws IOException If there's an I/O error
//     * @throws KeyStoreException If there's an error with the keystore
//     * @throws NoSuchAlgorithmException If a required cryptographic algorithm is not available
//     */
//    @Bean
//    public ElasticsearchClient elasticsearchClient()
//        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY,
//            new UsernamePasswordCredentials(elasticsearchProperty.getUsername(),
//                elasticsearchProperty.getPassword()));
//
//        SSLContext sslContext = SSLContexts.custom()
//            .loadTrustMaterial(null, (chains, authType) -> true)
//            .build();
//
//        RestClientBuilder builder = RestClient.builder(
//                new HttpHost(elasticsearchProperty.getHost(), elasticsearchProperty.getPort(), elasticsearchProperty.getScheme()))
//            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
//                .setDefaultCredentialsProvider(credentialsProvider)
//                .setSSLContext(sslContext));
//
//        RestClient restClient = builder.build();
//
//        ElasticsearchTransport transport = new RestClientTransport(
//            restClient, new JacksonJsonpMapper());
//
//        return new ElasticsearchClient(transport);
//    }
//}