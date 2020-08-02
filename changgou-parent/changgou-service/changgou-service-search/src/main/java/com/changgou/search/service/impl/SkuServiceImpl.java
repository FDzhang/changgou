package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.framework.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/16 16:13
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 多条件搜索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        // 搜索条件封装
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        // 集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);
        // 分类、品牌、规格
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);

        resultMap.putAll(groupMap);

        return resultMap;

    }

    /**
     * 搜索条件封装
     *
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQueryBuilder : 搜索条件构建对象，用于封装各种搜索条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        // BoolQuery must  must_not should
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size() > 0) {
            // 根据关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            if (!StringUtils.isEmpty(keywords)) {
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
            // 分类 ->category
            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }
            // 品牌 ->brand
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            // 规格
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key以spec, _开始，则表示规格筛选查询
                if (key.startsWith("spec_")) {
                    String value = entry.getValue();
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
                }
            }

            // price
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                price = price.replace("元", "").replace("以上", "");
                String[] prices = price.split("-");
                if (prices != null && prices.length > 0) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if (prices.length > 1) {
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }
            // 排序实现
            // 排序的域
            // 排序的规则
            String sortField = searchMap.get("sortField");
            String sortRule = searchMap.get("sortRule");
            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
                nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule)));
            }

            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        }

        // 分页 不传分页参数，默认第一页
        Integer pageNum = converterPage(searchMap);
        Integer size = 50;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, size));

        return nativeSearchQueryBuilder;
    }


    public Integer converterPage(Map<String, String> searchMap) {
        if (searchMap != null && searchMap.get("pageNum") != null) {
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    /**
     * 结果集搜索
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        // 前缀 <em style="color:red;">
        field.preTags("<em style=\"color:red;\">");
        // 后缀 </em>
        field.postTags("</em>");
        // 碎片长度 关键词数据的长度 （合起来 100个）  今天<em style="color:red;">小红</em>穿 了一件花衣服，好美丽啊，好美丽啊，好美丽啊，好美丽啊，好
        field.fragmentSize(100);

        nativeSearchQueryBuilder.withHighlightFields(field);

        /*
         * 执行搜索，响应结果给我
         * 1)搜索条件封装对象
         * 2)搜索的结果集(集合数据)需要转换的类型
         * 3)AggregatedPage<SkuInfo>:搜索结果集的封装
         */
        AggregatedPage<SkuInfo> page = elasticsearchTemplate
                .queryForPage(
                        nativeSearchQueryBuilder.build(),
                        SkuInfo.class,
                        new SearchResultMapper() {
                            @Override
                            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                                //存储所有转换后的高亮数据对象
                                List<T> list = new ArrayList<>();

                                // 执行查询，获取所有数据->结果集[非高亮数据|高亮数据]
                                //分析结果集数据，获取非高亮数据
                                for (SearchHit hit : response.getHits()) {
                                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

                                    //分析结果集数据，获取高亮数据->只有某个域的高亮数据
                                    HighlightField highlightField = hit.getHighlightFields().get("name");

                                    if (highlightField != null && highlightField.getFragments() != null) {

                                        Text[] fragments = highlightField.getFragments();
                                        StringBuilder builder = new StringBuilder();
                                        for (Text fragment : fragments) {
                                            builder.append(fragment.toString());
                                        }
                                        //非高亮数据中指定的域替换成高亮数据
                                        skuInfo.setName(builder.toString());
                                    }
                                    //将 高亮数据添加到集合中
                                    list.add((T) skuInfo);
                                }

                                /***
                                 * 1)搜索的集合数据: (携带 高亮)List<T> content
                                 * 2)分页对象信息: Pageable pageable
                                 * 3)搜索记录的总条数: long total
                                 */
                                //将数据返回
                                return new AggregatedPageImpl<T>(list, pageable, response.getHits().getTotalHits());
                            }
                        });


        // 分析数据
        // 分页参数-总记录数
        long totalElements = page.getTotalElements();

        // 总页数
        int totalPages = page.getTotalPages();

        // 获取数据结果集
        List<SkuInfo> contents = page.getContent();

        // 封装一个Map存储所有数据，并返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", contents);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);

        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        Pageable pageable = query.getPageable();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();

        resultMap.put("pageSize",pageSize);
        resultMap.put("pageNumber",pageNumber);

        return resultMap;
    }

    /**
     * 分组查询
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String, String> searchMap) {
        /*
         *  分组在询分类集合
         * . addAggregation( :添加一一个聚 合操作
         *  1)取别名
         *  2)长示根据哪个城进行分组在询
         */

        Map<String, Object> groupMap = new HashMap<>();

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /*
        获取分组数据
        aggregatedPage. getAggregations() :获取的是集合,可以根据多个域进行分组
        . get (”skuCategory") :获取指定域的集合数 [手机，家用电器，手机配件]
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(categoryTerms);
            groupMap.put("categoryList", categoryList);
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            List<String> brandList = getGroupList(brandTerms);
            groupMap.put("brandList", brandList);
        }

        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> stringSetMap = putAllSpec(specList);

        groupMap.put("specList", stringSetMap);

        return groupMap;
    }

    private List<String> getGroupList(StringTerms stringTerms) {
        List<String> groupList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            // 其中一个分类的名字
            String fieldName = bucket.getKeyAsString();
            groupList.add(fieldName);
        }
        return groupList;
    }

    /**
     * 规格汇总合并
     *
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        //合并后的Map对象
        Map<String, Set<String>> allSpec = new HashMap<>();
        // 1循环specList
        for (String spec : specList) {
            // 2.将每个JSON字符串转成Map
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);
            // 3.将每个Map对象合成成一 个Map<String, Set<String>>
            // 4.合并流程
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                // 4.1循环所有Map
                // 4.2取出当前Map，并且获取对应的Key 以及对应value
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specSet = allSpec.get(key);
                if (specSet == null) {
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                // 4.3将当前循环的数据合并到-个Map<String, Set<String>>中
                allSpec.put(key, specSet);
            }
        }
        return allSpec;
    }

    /**
     * 导入索引库
     */
    @Override
    public void importData() {
        //Feign调用，查i询List<Sku>
        Result<List<Sku>> skuResult = skuFeign.findAll();

        //将List<Sku>转成List<SkuInfo>
        List<SkuInfo> skuList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);

        for (SkuInfo skuInfo : skuList) {
            //获取spec -> Map (String)->Map类型{” 电视音响效果”:”小影院”，”电视屏幕尺寸”:”20英寸”,”尺码”:”165"}
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            //如果需婴生成动态的域，只需要将该域存入到-个Map<String, object>对象中即可，该map<String, Object>的key会生成一个域， 域的名字为该Mapi的key
            //当前Map<String, object>后面object的值会作为当前Sku对象该域(key)对应的值
            skuInfo.setSpecMap(specMap);
        }

        //调川Dao实现数据批址导入
        skuEsMapper.saveAll(skuList);
    }
}
