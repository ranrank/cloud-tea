package com.cloudtea.controller.admin;

import com.cloudtea.dto.DrinkDTO;
import com.cloudtea.dto.DrinkPageQueryDTO;
import com.cloudtea.entity.Drink;
import com.cloudtea.result.PageResult;
import com.cloudtea.result.Result;
import com.cloudtea.service.DrinkService;
import com.cloudtea.vo.DrinkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/drink")
@Api(tags = "饮品相关接口")
@Slf4j
public class DrinkController {
    @Autowired
    private DrinkService drinkService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增饮品
     */
    @PostMapping
    public Result save(@RequestBody DrinkDTO drinkDTO ){
        log.info("新增饮品：{}", drinkDTO);
        //调用服务层新增饮品
        drinkService.saveWithSpec(drinkDTO);
        //删除redis中的饮品缓存
        cleanCache("drink_*");
        return Result.success();
    }

    /**
     * 分页查询饮品
     */
    @GetMapping("/page")
    public Result<PageResult> page(DrinkPageQueryDTO drinkPageQueryDTO){
        log.info("分页查询饮品：{}", drinkPageQueryDTO);
        //调用服务层分页查询饮品
        PageResult pageResult = drinkService.pageQuery(drinkPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除饮品
     */
    @DeleteMapping
    @ApiOperation("删除饮品")
    public Result delete( @RequestParam List<Long> ids){
        log.info("饮品批量删除：{}", ids);
        drinkService.deleteBatch(ids);
        //将所有分类的饮品缓存删除掉
        cleanCache("drink_*");
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据饮品id查询饮品详情")
    public Result<DrinkVO> getById(@PathVariable Long id){
        log.info("根据饮品id查询饮品详情：{}", id);
        DrinkVO drinkVO = drinkService.getByIdWithSpec(id);
        return Result.success(drinkVO);
    }
    /**
     * 更新饮品
     */
    @PutMapping
    @ApiOperation("更新饮品")
    public Result update(@RequestBody DrinkDTO drinkDTO){
        log.info("更新饮品：{}", drinkDTO);
        //调用服务层更新饮品
        drinkService.updateWithSpec(drinkDTO);
        //将所有分类的饮品缓存删除掉
        cleanCache("drink_*");
        return Result.success();
    }
    /**
     * 根据分类id查询饮品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询饮品")
    public Result<List<Drink>> list(Long categoryId){
        log.info("根据分类id查询饮品：{}", categoryId);
        List<Drink> list = drinkService.list(categoryId);
        return Result.success(list);
    }
    /**
     * 饮品上架下架
     */
    @PostMapping("/status/{status}")
    @ApiOperation("饮品上架下架")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("饮品上架下架：{},{}", status,id);
        drinkService.startOrStop(status,id);
        //将所有分类的饮品缓存删除掉
        cleanCache("drink_*");
        return Result.success();
    }
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }
}
