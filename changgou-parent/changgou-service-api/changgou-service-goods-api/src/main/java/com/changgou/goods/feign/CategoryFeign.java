package com.changgou.goods.feign;

import com.changgou.framework.entity.Result;
import com.changgou.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author ：zxq
 * @date ：Created in 2020/8/1 17:20
 */
@FeignClient(name = "goods")
@RequestMapping(value = "/category")
public interface CategoryFeign {

    /**
     * 获取分类的对象信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Category> findById(@PathVariable(name = "id") Integer id);
}
