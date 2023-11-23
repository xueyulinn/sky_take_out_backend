package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        //访问微信接口，获取openid
        String openid = getString(userLoginDTO);

        //判断openid是否存在
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断是不是新用户，如果不是添加用户到user表中
        User user = userMapper.getByOpenId(openid);

        if(user==null){
            //新用户
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();

            //插入到user表中
            userMapper.insert(user);
        }

        return user;
    }


    private String getString(UserLoginDTO userLoginDTO) {
        //访问微信接口，获取openid
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type","authorization_code");

        String responseBody = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", map);

        //fastjson
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        String openid = (String) jsonObject.get("openid");
        // String session_key = (String) jsonObject.get("session_key");
        return openid;
    }


}
