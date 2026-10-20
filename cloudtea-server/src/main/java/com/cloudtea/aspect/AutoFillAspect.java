package com.cloudtea.aspect;

import com.cloudtea.annotation.AutoFill;
import com.cloudtea.constant.AutoFillConstant;
import com.cloudtea.context.BaseContext;
import com.cloudtea.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.cloudtea.mapper.*.*(..))&&@annotation(com.cloudtea.annotation.AutoFill)")
    public void autofillPointCut() {}
    /*前置通知，在通知中进行公共字段的赋值*/
    @Before("autofillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段子自动填充");
        //获取注解中的操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        //获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }
        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now =LocalDateTime.now();
        long currentId = BaseContext.getCurrentId();
        //根据操作类型，进行反射赋值
        if(operationType== OperationType.INSERT) {
            try {
                Method setCreatetime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setCreatetime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(operationType== OperationType.UPDATE) {
            try {
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
