package com.changgou.web.search.controller;

import com.changgou.framework.entity.Page;
import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/19 20:58
 */

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map searchMap, Model model) {
        //调用changgou-service-search微服务
        Map resultMap = skuFeign.search(searchMap);
        model.addAttribute("result", resultMap);


        long total = Long.parseLong(resultMap.get("total").toString());
        int pageSize = Integer.parseInt(resultMap.get("pageSize").toString());
        int pageNumber = Integer.parseInt(resultMap.get("pageNumber").toString());

        Page<SkuInfo> pageInfo = new Page<>(total, pageNumber + 1, pageSize);

        model.addAttribute("pageInfo", pageInfo);

        // 存储条件 用于回显
        model.addAttribute("searchMap", searchMap);

        String[] urls = url(searchMap);

        model.addAttribute("url", urls[0]);
        model.addAttribute("sortUrl", urls[1]);

        return "search";
    }

    /****
     *拼接组装用户请求的URL地址
     *获取用户每次请求的地址
     *页面需要在这次请求的地址上面添加额外的搜的条件
     * http://localhost: 18086/search/list
     * http://localhost :18086/search/list?keywords=华为
     * http://localhost: 18086/ search/list?keywords=华为&brand=华为
     * http://localhost: 18086/ search/list?keywords=华为&brand=华为&category=语言文字
     */
    public String[] url(Map<String, String> searchMap) {
        String url = "/search/list";
        String sortUrl = "/search/list";

        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            sortUrl += "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // 跳过排序参数
                if ("pageNum".equalsIgnoreCase(key)) {
                    continue;
                }


                url += key + "=" + value + "&";

                // 跳过排序参数
                if ("sortField".equalsIgnoreCase(key) || "sortRule".equalsIgnoreCase(key)) {
                    continue;
                }

                sortUrl += key + "=" + value + "&";
            }

            url = url.substring(0, url.length() - 1);
            sortUrl = sortUrl.substring(0, sortUrl.length() - 1);

        }

        return new String[]{url, sortUrl};
    }
}