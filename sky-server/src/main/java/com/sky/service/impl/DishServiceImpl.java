package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @Transactional
    public void saveWithFlovor(DishDTO dishDTO) {
        Dish dish = new Dish();

        //对象拷贝
        BeanUtils.copyProperties(dishDTO,dish);
        //新增菜品
        dishMapper.insert(dish);

        //获取菜品的id
        Long dishId = dish.getId();
        //新增口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.stream().forEach(flavor->{
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        //设置pagehelper分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //调用mapper，做查询、
        Page<DishVO> page =  dishMapper.page(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //查询菜品是否起售状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // select count(*) from setmeal_dish where dish_id in(1,2,3)
        //被套餐关联的菜品不能删除
        List<Long> setmealIds= setmealDishMapper.countSetmealDishByDishId(ids);
        //根据mybaties版本的不同 如果查不到也可能返回一个长度为0的集合(目前所使用的mybaties会返回null值)
        if(setmealIds!=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : ids) {
            //删除菜品
            dishMapper.deleteById(id);
            //删除口味
            dishFlavorMapper.deleteByDishId(id);
        }

    }

    /**
     * 查看菜品详情
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {

        //获取基础菜品信息
        Dish dish = dishMapper.getById(id);
        //口味信息
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavorList);

        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        //1修改菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.update(dish);

        //2修改口味表
        //2.1删除之前的口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //2.2插入新的口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.stream().forEach(flavor->{
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    //菜品的起售、停售
    @Override
    public void startOrStop(Integer status, Long id) {
        //菜品停售时，把套餐也停售了
        if(status==StatusConstant.DISABLE){
            List<Long> ids = new ArrayList<>();
            ids.add(id);

            List<Long> setmealIds = setmealDishMapper.countSetmealDishByDishId(ids);

            for (Long setmealId : setmealIds) {
                Setmeal setmeal = Setmeal.builder()
                        .id(setmealId)
                        .status(StatusConstant.DISABLE)
                        .build();

                setmealMapper.update(setmeal);
            }
        }

        Dish dish = Dish.builder()
                        .id(id)
                        .status(status)
                        .build();

        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
