package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端的购物车相关接口")
public class ShopCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("接收添加购物车的参数：{}",shoppingCartDTO);
        //2，调用service
        shoppingCartService.add(shoppingCartDTO);

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("购物车查询")
    public Result<List<ShoppingCart>> list(){
        //2，调用service
        List<ShoppingCart> shoppingCartList = shoppingCartService.list();

        return Result.success(shoppingCartList);
    }

    @DeleteMapping("/clean")
    @ApiOperation("清除购物车")
    public Result clean(){
        //2，调用service
        shoppingCartService.clean();

        return Result.success();
    }
}
