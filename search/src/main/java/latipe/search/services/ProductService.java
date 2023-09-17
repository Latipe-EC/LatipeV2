package latipe.search.services;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;

import latipe.search.constants.ProductField;
import latipe.search.constants.ESortType;
import latipe.search.document.Product;
import latipe.search.viewmodel.ProductGetVm;
import latipe.search.viewmodel.ProductListGetVm;
import latipe.search.viewmodel.ProductNameGetVm;
import latipe.search.viewmodel.ProductNameListVm;
import org.elasticsearch.common.unit.Fuzziness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService {
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public CompletableFuture<ProductListGetVm> findProductAdvance(String keyword,
                                                                  Integer page,
                                                                  Integer size,
                                                                  String category,
                                                                  String classification,
                                                                  Double minPrice,
                                                                  Double maxPrice,
                                                                  ESortType sortType) {
        return CompletableFuture.supplyAsync(() -> {
            NativeQueryBuilder nativeQuery = NativeQuery.builder()
                    .withAggregation("categories", Aggregation.of(a -> a
                            .terms(ta -> ta.field(ProductField.CATEGORIES))))
                    .withAggregation("classifications", Aggregation.of(a -> a
                            .terms(ta -> ta.field(ProductField.CLASSIFICATIONS))))
                    .withQuery(q -> q
                            .bool(b -> b
                                    .should(s -> s
                                            .multiMatch(m -> m
                                                    .fields(ProductField.NAME, ProductField.CLASSIFICATIONS)
                                                    .query(keyword)
                                                    .fuzziness(Fuzziness.ONE.asString())
                                            )
                                    )
                            )
                    )
                    .withPageable(PageRequest.of(page, size));
            nativeQuery.withFilter(f -> f
                    .bool(b -> {
                        extractedStr(category, ProductField.CATEGORIES, b);
                        extractedStr(classification, ProductField.CLASSIFICATIONS, b);
                        extractedRange(minPrice, maxPrice, ProductField.PRICE, b);
                        return b;
                    })
            );

            if (sortType == ESortType.PRICE_ASC) {
                nativeQuery.withSort(Sort.by(Sort.Direction.ASC, ProductField.PRICE));
            } else if (sortType == ESortType.PRICE_DESC) {
                nativeQuery.withSort(Sort.by(Sort.Direction.DESC, ProductField.PRICE));
            } else {
                nativeQuery.withSort(Sort.by(Sort.Direction.DESC, ProductField.CREATE_ON));
            }

            SearchHits<Product> searchHitsResult = elasticsearchOperations.search(nativeQuery.build(), Product.class);
            SearchPage<Product> productPage = SearchHitSupport.searchPageFor(searchHitsResult, nativeQuery.getPageable());

            List<ProductGetVm> productListVmList = searchHitsResult.stream()
                    .map(i -> ProductGetVm.fromModel(i.getContent())).toList();

            return new ProductListGetVm(
                    productListVmList,
                    productPage.getNumber(),
                    productPage.getSize(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages(),
                    productPage.isLast(),
                    getAggregations(searchHitsResult));
        });

    }

    private void extractedStr(String strField, String productField, BoolQuery.Builder b) {
        if (strField != null && !strField.isBlank()) {
            String[] strFields = strField.split(",");
            for (String str : strFields) {
                b.should(s -> s
                        .term(t -> t
                                .field(productField)
                                .value(str)
                                .caseInsensitive(true)
                        )
                );
            }
        }
    }

    private void extractedRange(Number min, Number max, String productField, BoolQuery.Builder b) {
        if (min != null || max != null) {
            b.must(m -> m
                    .range(r -> r
                            .field(productField)
                            .from(min != null ? min.toString() : null)
                            .to(max != null ? max.toString() : null)
                    )
            );
        }
    }

    private Map<String, Map<String, Long>> getAggregations(SearchHits<Product> searchHits) {
        List<org.springframework.data.elasticsearch.client.elc.Aggregation> aggregations = new ArrayList<>();
        if (searchHits.hasAggregations()) {
            ((List<ElasticsearchAggregation>) searchHits.getAggregations().aggregations()) //NOSONAR
                    .forEach(elsAgg -> aggregations.add(elsAgg.aggregation()));
        }

        Map<String, Map<String, Long>> aggregationsMap = new HashMap<>();
        aggregations.forEach(agg -> {
            Map<String, Long> aggregation = new HashMap<>();
            StringTermsAggregate stringTermsAggregate = (StringTermsAggregate) agg.getAggregate()._get();
            List<StringTermsBucket> stringTermsBuckets = (List<StringTermsBucket>) stringTermsAggregate.buckets()._get();
            stringTermsBuckets.forEach(bucket -> aggregation.put(bucket.key()._get().toString(), bucket.docCount()));
            aggregationsMap.put(agg.getName(), aggregation);
        });

        return aggregationsMap;
    }

    public ProductNameListVm autoCompleteProductName(final String keyword) {
        NativeQuery matchQuery = NativeQuery.builder()
                .withQuery(
                        q -> q.matchPhrasePrefix(
                                mPP -> mPP.field("name").query(keyword)
                        )
                )
                .withSourceFilter(new FetchSourceFilter(
                        new String[]{"name"},
                        null)
                )
                .build();
        SearchHits<Product> result = elasticsearchOperations.search(matchQuery, Product.class);
        List<Product> products = result.stream().map(SearchHit::getContent).toList();

        return new ProductNameListVm(products.stream().map(ProductNameGetVm::fromModel).toList());
    }
}