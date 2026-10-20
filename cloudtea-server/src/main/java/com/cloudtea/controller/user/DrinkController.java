package com.cloudtea.controller.user;

import com.cloudtea.constant.StatusConstant;
import com.cloudtea.entity.Drink;
import com.cloudtea.result.Result;
import com.cloudtea.service.DrinkService;
import com.cloudtea.vo.DrinkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDrinkController")
@RequestMapping("/user/drink")
@Slf4j
@Api(tags = "C端-饮品浏览接口")
public class DrinkController {
    @Autowired
    private DrinkService drinkService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询饮品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询饮品")
    public Result<List<DrinkVO>> list(Long categoryId) {
        //构造redis key，规则drink_categoryId
        String key = "drink_" + categoryId;

        //查询redis中是否存在饮品数据
        List<DrinkVO> list = (List<DrinkVO>)redisTemplate.opsForValue().get(key);
        if (list != null&&list.size()>0) {
            //如果存在，直接返回，无需查询数据库
            return Result.success(list);
        }




        Drink drink = new Drink();
        drink.setCategoryId(categoryId);
        drink.setStatus(StatusConstant.ENABLE);//查询上架中的饮品
        //如果不存在，查询数据库，将查询到的数据放入redis中
        list = drinkService.listWithSpec(drink);
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

}
