package com.baidu.shop.mapper;

import com.baidu.shop.entity.OrderDetailEntity;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @ClassName OrderDetailMapper
 * @Description: OrderDetailMapper
 * @Author jinluying
 * @create: 2020-10-21 14:25
 * @Version V1.0
 **/
public interface OrderDetailMapper extends Mapper<OrderDetailEntity> , InsertListMapper<OrderDetailEntity>{
}
