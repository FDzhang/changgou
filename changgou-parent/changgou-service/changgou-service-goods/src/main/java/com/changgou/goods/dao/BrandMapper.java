package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.goods.dao
 * Dao层:
 *  使用通用Mapper->MyBatis动态SQL
 *  0SQL语句,面向对象操作
 *  要求：Dao必须集成Mapper<T>接口
 *        Mapper接口中有增删改查各种操作
 ****/
public interface BrandMapper extends Mapper<Brand> {

    /***
     * 查询分类对应的品牌集合
     */
    @Select("SELECT tb.* FROM tb_category_brand tcb, tb_brand tb WHERE tcb.category_id=#{categoryId} AND tb.id=tcb.brand_id")
    List<Brand> findByCategory(Integer categoryId);

}
