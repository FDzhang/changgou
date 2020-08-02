package com.changgou.goods.feign;

import com.changgou.framework.entity.Result;
import com.changgou.goods.pojo.Sku;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/16 16:08
 */
@FeignClient(name = "goods") // 与配置文件中的application.name对应
@RequestMapping(value = "/sku")
public interface SkuFeign {

    /***
     * 查询Sku全部数据
     * @return
     */
    @ApiOperation(value = "查询所有Sku", notes = "查询所Sku有方法详情", tags = {"SkuController"})
    @GetMapping
    Result<List<Sku>> findAll();


    /**
     * 根据条件搜索
     *
     * @param sku
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);
}