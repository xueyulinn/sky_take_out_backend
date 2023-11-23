package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@Api(tags = "C端用户接口")
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation("C端用户登录接口")
    public Result<UserLoginVO> wxLogin(@RequestBody UserLoginDTO userLoginDTO){
        log.info("接收小程序传递过来的授权码:{}",userLoginDTO);
        //调用service层实现业务逻辑
        User user = userService.login(userLoginDTO);
        //创建jwt令牌
        //自定义数据
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        //生成jwt令牌
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(jwt).build();

        return Result.success(userLoginVO);
    }


}
