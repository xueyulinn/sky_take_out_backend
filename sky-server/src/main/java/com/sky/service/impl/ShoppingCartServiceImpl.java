package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //思考：有可能是加数量update，有可能添加新的菜品insert
        //目标：通过查询购物车，确定购物车中有没有该菜品或者套餐 dishid dishflovor  setmealid
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //获取当前登陆人的id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询购物车
        // 菜品：select * from shopping_cart where dish_id=? and dish_flovor and user_id=?
        // 套餐：select * from shopping_cart where setmeal_id and user_id=?
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        //判断购物车中有没有该菜品或者套餐
        if(shoppingCartList!=null && shoppingCartList.size()>0){
            //购物车中已经存在该菜品或者套餐，修改数量
            shoppingCart = shoppingCartList.get(0);
            //获取原始数量
            shoppingCart.setNumber(shoppingCart.getNumber()+1);

            shoppingCartMapper.update(shoppingCart);
        }else{
            //插入购物车
            //区分是菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if(dishId==null){
                //套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }else{
                //菜品
                Dish dish = dishMapper.getById(shoppingCart.getDishId());

                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);

            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> list() {
        //获取当前登录人的id
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        return shoppingCartList;
    }

    @Override
    public void clean() {
        //获取当前登录人的id
        Long userId = BaseContext.getCurrentId();

        //调用mapper,清空购物车
        // shoppingCartMapper.delete ByUserId(userId);
    }
}
