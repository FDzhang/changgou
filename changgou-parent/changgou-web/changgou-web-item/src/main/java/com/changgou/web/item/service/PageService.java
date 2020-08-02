package com.changgou.web.item.service;

/**
 * @author ：zxq
 * @date ：Created in 2020/8/1 17:23
 */

public interface PageService {
    /**
     * 根据商品的ID 生成静态页
     * @param spuId
     */
    void createPageHtml(Long spuId) ;
}