package com.changgou.search.dao;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/16 16:16
 * <p>
 * ElasticsearchRepository<SkuInfo,Long> ： 实体类，主键类型
 */
@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo, Long> {
}
