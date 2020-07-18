package com.changgou.search.service;

import java.util.Map;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/16 16:12
 */

public interface SkuService {

    /**
     * 条件搜索
     */
    Map<String, Object> search(Map<String, String> searchMap);

    /**
     * 导入数据
     */
    void importData();
}
