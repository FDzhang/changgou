package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.framework.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // 分类分组查询实现
        List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);

        resultMap.put("categoryList", categoryList);

        return resultMap;

    }

    /**
     * 搜索条件封装
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQueryBuilder : 搜索条件构建对象，用于封装各种搜索条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        if (searchMap != null && searchMap.size() > 0) {
            // 根据关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            if (!StringUtils.isEmpty(keywords)) {
                nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
        }
        return nativeSearchQueryBuilder;
    }

    /**
     * 结果集搜索
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /*
         * 执行搜索，响应结果给我
         * 1)搜索条件封装对象
         * 2)搜索的结果集(集合数据)需要转换的类型
         * 3)AggregatedPage<SkuInfo>:搜索结果集的封装
         */
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

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
        return resultMap;
    }

    /**
     * 分类分组查询
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /*
         *  分组在询分类集合
         * . addAggregation( :添加一一个聚 合操作
         *  1)取别名
         *  2)长示根据哪个城进行分组在询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /*
        获取分组数据
        aggregatedPage. getAggregations() :获取的是集合,可以根据多个域进行分组
        . get (”skuCategory") :获取指定域的集合数 [手机，家用电器，手机配件]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");

        List<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            // 其中一个分类的名字
            String categoryName = bucket.getKeyAsString();
            categoryList.add(categoryName);
        }
        return categoryList;
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
