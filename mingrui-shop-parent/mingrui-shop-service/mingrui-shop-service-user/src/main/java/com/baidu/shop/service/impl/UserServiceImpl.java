package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.constant.UserConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: UserServiceImpl
 * @Author jinluying
 * @create: 2020-10-13 15:04
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private RedisRepository  redisRepository;

    @Override
    public Result<List<UserEntity>> checkUsernameOrPhonenumber(String value, Integer type) {
        System.out.println(value);
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(type == UserConstant.USER_TYPE_USERNAME){
            criteria.andEqualTo("username",value);
        }else if(type == UserConstant.USER_TYPE_PHONE){
            criteria.andEqualTo("phone",value);
        }
        List<UserEntity> userList = userMapper.selectByExample(example);

        return this.setResultSuccess(userList);
    }

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {

        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setCreated(new Date());
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));

        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {
        //生成随机6位验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        //发送短信验证码
//        LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code);
        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);

        redisRepository.set(MrShopConstant.USER_PHONE_CODE_VALIDE +userDTO.getPhone(),code);
        redisRepository.expire(MrShopConstant.USER_PHONE_CODE_VALIDE+userDTO.getPhone(),120);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkValidCode(String phone, String validcode) {

        String redisValidCode = redisRepository.get(MrShopConstant.USER_PHONE_CODE_VALIDE + phone);
        if(!validcode.equals(redisValidCode)){
            return this.setResultError("验证码输入错误");
        }

        return this.setResultSuccess();
    }
}
