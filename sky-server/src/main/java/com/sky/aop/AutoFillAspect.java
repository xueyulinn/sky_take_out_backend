package com.sky.aop;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {

    //切点表达式 execution  @annotation  insert update
    @Pointcut("execution(* com.sky.mapper.*Mapper.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void pt(){}


    // @Before("pt()")
    public void commonFiledFill(JoinPoint joinPoint){
        log.info("公共字段自动填充，开始了。。。");
        //1.获取注解后是新增还是修改的类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取签名方法
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //2.获取方法参数对象
        Object[] args = joinPoint.getArgs();
        if(args==null||args.length==0){
            return;
        }
        Object arg = args[0];

        //3.准备数据
        LocalDateTime now = LocalDateTime.now();
        Long empId = BaseContext.getCurrentId();

        //4.填充公共字段  class = Filed  Method  Constructor
        if(operationType.equals(OperationType.INSERT)){
            //填充4个字段 通过反射设置
            try {
                Field createTime = arg.getClass().getDeclaredField("createTime");
                Field updateTime = arg.getClass().getDeclaredField("updateTime");
                Field createUser = arg.getClass().getDeclaredField("createUser");
                Field updateUser = arg.getClass().getDeclaredField("updateUser");

                createTime.setAccessible(true);
                updateTime.setAccessible(true);
                createUser.setAccessible(true);
                updateUser.setAccessible(true);

                createTime.set(arg,now);
                updateTime.set(arg,now);
                createUser.set(arg,empId);
                updateUser.set(arg,empId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //填充2个字段 通过反射设置
            try {
                Field updateTime = arg.getClass().getDeclaredField("updateTime");
                Field updateUser = arg.getClass().getDeclaredField("updateUser");

                //暴力反射
                updateTime.setAccessible(true);
                updateUser.setAccessible(true);

                updateTime.set(arg,now);
                updateUser.set(arg,empId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Before("pt()")
    public void autoFill(JoinPoint joinPoint){

        //获取注解的类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取目标方法的参数
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }
        Object object = args[0];

        //准备数据
        LocalDateTime now = LocalDateTime.now();
        Long empId = BaseContext.getCurrentId();
        //目标：对新增或者修改的参数对象填充公共字段
        if(operationType.equals(OperationType.INSERT)){
            //维护四个字段
            try {
                Method setCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(object,now);
                setCreateUser.invoke(object,empId);
                setUpdateTime.invoke(object,now);
                setUpdateUser.invoke(object,empId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(object,now);
                setUpdateUser.invoke(object,empId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
