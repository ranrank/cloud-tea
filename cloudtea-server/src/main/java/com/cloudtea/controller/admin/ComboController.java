package com.cloudtea.controller.admin;

import com.cloudtea.dto.ComboDTO;
import com.cloudtea.dto.ComboPageQueryDTO;
import com.cloudtea.result.PageResult;
import com.cloudtea.result.Result;
import com.cloudtea.service.ComboService;
import com.cloudtea.vo.ComboVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/combo")
@Slf4j
@Api(tags = "组合套餐相关接口")
public class ComboController {
    @Autowired
    private ComboService comboService;
    @PostMapping
    @ApiOperation("新增组合套餐")
    @CacheEvict(cacheNames="comboCache",key = "#comboDTO.categoryId")
    public Result Save(@RequestBody ComboDTO comboDTO){
        log.info("新增组合套餐，组合套餐信息：{}", comboDTO);
        comboService.saveWithDrink(comboDTO);
        return Result.success();
    }
    @GetMapping("/page")
    public Result<PageResult> page(ComboPageQueryDTO comboPageQueryDTO){
        log.info("分页查询组合套餐，组合套餐信息：{}", comboPageQueryDTO);
        PageResult pageResult = comboService.page(comboPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除组合套餐")
    @CacheEvict(cacheNames="comboCache",allEntries = true)
     public Result delete(@RequestParam List<Long> ids){
        log.info("删除组合套餐，组合套餐id：{}", ids);
        comboService.delete(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询组合套餐")
    public Result<ComboVO> getById(@PathVariable Long id){
        log.info("根据id查询组合套餐，组合套餐id：{}", id);
        ComboVO comboVO = comboService.getById(id);
        return Result.success(comboVO);
    }
    @PutMapping
    @ApiOperation("更新组合套餐")
    @CacheEvict(cacheNames="comboCache",allEntries = true)
    public Result update(@RequestBody ComboDTO comboDTO){
        log.info("更新组合套餐，组合套餐信息：{}", comboDTO);
        comboService.update(comboDTO);
        return Result.success();
    }
    @PostMapping("status/{status}")
    @ApiOperation("更新组合套餐状态")
    @CacheEvict(cacheNames="comboCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id){
        log.info("更新组合套餐状态，组合套餐id：{},状态：{}", id,status);
        comboService.startOrStop(status,id);
        return Result.success();
    }
}