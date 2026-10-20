package com.cloudtea.controller.admin;

import com.cloudtea.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminStoreController")
@RequestMapping("/admin/store")
@Api("门店相关接口")
@Slf4j
public class StoreController {
    public static final String KEY="STORE_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置门店状态
     * @param status 门店状态
     * @return
     */
    @ApiOperation("设置门店状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置门店状态为：{}", status==1?"营业中":"打烊中");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();

    }
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