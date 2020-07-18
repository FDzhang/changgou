package com.changgou.search.controller;

import com.changgou.framework.entity.Result;
import com.changgou.framework.entity.StatusCode;
import com.changgou.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/16 16:23
 */
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String, String> searchMap) {
        return skuService.search(searchMap);
    }

    /**
     * 导入数据
     *
     * @return
     */
    @GetMapping("/import")
    public Result importData() {
        skuService.importData();
        return new Result(true, StatusCode.OK, "导入数据到索引库中成功！");
    }
}
