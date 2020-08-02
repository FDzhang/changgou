package com.changgou.goods.feign;

import com.changgou.framework.entity.Result;
import com.changgou.goods.pojo.Spu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author ：zxq
 * @date ：Created in 2020/8/1 17:21
 */
@FeignClient(name = "goods")
@RequestMapping(value = "/spu")
public interface SpuFeign {

    /***
     * 根据SpuID查询Spu信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Spu> findById(@PathVariable(name = "id") Long id);
}
