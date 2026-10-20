package com.cloudtea.controller.user;

import com.cloudtea.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userStoreController")
@RequestMapping("/user/store")
@Api("门店相关接口")
@Slf4j
public class StoreController {
    public static final String KEY="STORE_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取门店状态
     * @return
     */
    @ApiOperation("获取门店状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        if (status == null) {
            status = 1; // 默认营业中
        }
        log.info("门店状态为：{}", status==1?"营业中":"打烊中");
        return Result.success(status);
    }
}