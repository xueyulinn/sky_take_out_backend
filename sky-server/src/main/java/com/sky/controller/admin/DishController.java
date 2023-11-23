package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = "dishCache",key="#dishDTO.categoryId")
    public Result save(@RequestBody DishDTO dishDTO){

        log.info("新增菜品数据：{}",dishDTO);
        //调用service,实现业务逻辑处理
        dishService.saveWithFlovor(dishDTO);

        //清除当前分类下的缓存菜品
        // cleanRedis("dish_"+dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页参数：{}",dishPageQueryDTO);

        //调用service
        PageResult pageResult = dishService.page(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public  Result deleteByIds(@RequestParam List<Long> ids){
        log.info("接收ids:{}",ids);
        //调用service,实现业务逻辑处理
        dishService.deleteByIds(ids);

        //清除所有分类下的缓存菜品
        // cleanRedis("dish_*");

        return Result.success();
    }

    /**
     * 查看菜品详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("查看菜品详情")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("接收id：{}",id);

        DishVO dishVO = dishService.getById(id);

        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("接收修改菜品数据：{}",dishDTO);

        dishService.update(dishDTO);

        //清除所有分类下的缓存菜品
        // cleanRedis("dish_*");

        return  Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("接收参数：{}，{}",status,id);
        dishService.startOrStop(status,id);

        //清除所有分类下的缓存菜品
        // cleanRedis("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

   /* private void cleanRedis(String key) {
        Set keys = redisTemplate.keys(key);// dish_16 dish_17
        redisTemplate.delete(keys);
    }*/
}
