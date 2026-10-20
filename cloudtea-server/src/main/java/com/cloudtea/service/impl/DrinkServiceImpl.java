package com.cloudtea.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.cloudtea.constant.MessageConstant;
import com.cloudtea.constant.StatusConstant;
import com.cloudtea.dto.DrinkDTO;
import com.cloudtea.dto.DrinkPageQueryDTO;
import com.cloudtea.entity.Drink;
import com.cloudtea.entity.DrinkSpec;
import com.cloudtea.exception.DeletionNotAllowedException;
import com.cloudtea.mapper.DrinkSpecMapper;
import com.cloudtea.mapper.DrinkMapper;
import com.cloudtea.mapper.ComboDrinkMapper;
import com.cloudtea.result.PageResult;
import com.cloudtea.service.DrinkService;
import com.cloudtea.vo.DrinkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DrinkServiceImpl implements DrinkService {
    @Autowired
    private DrinkMapper drinkMapper;
    @Autowired
    private DrinkSpecMapper drinkSpecMapper;
    @Autowired
    private ComboDrinkMapper comboDrinkMapper;
    @Override
    @Transactional
    public void saveWithSpec(DrinkDTO drinkDTO) {
        Drink drink=new Drink();
        BeanUtils.copyProperties(drinkDTO,drink);
        //向饮品表插入1条数据
        drinkMapper.insert(drink);
        //获取饮品id
        Long drinkId = drink.getId();
        //将规格数据设置饮品id
        //向规格表插入n条数据
        List<DrinkSpec> specs = drinkDTO.getSpecs();
        //将饮品id设置到规格数据中
        if(specs!=null&&specs.size()>0){
            specs.forEach(ds->ds.setDrinkId(drinkId));
        }
        drinkSpecMapper.insertBatch(specs);
    }
    /**
     * 分页查询饮品
     */
    @Override
    public PageResult pageQuery(DrinkPageQueryDTO drinkPageQueryDTO) {
        PageHelper.startPage(drinkPageQueryDTO.getPage(),drinkPageQueryDTO.getPageSize());
        Page<DrinkVO> page = drinkMapper.pageQuery(drinkPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    /**
     * 批量删除饮品
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前饮品是否能够删除--是否存在上架中的饮品？
        for (Long id : ids) {
            Drink drink = drinkMapper.getById(id);
            if(drink.getStatus()== StatusConstant.ENABLE){
                //当前饮品是上架中的饮品，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DRINK_ON_SALE);
            }
        }
        //判断当前饮品是否能够删除--是否被组合套餐关联了？
        List<Long> comboIds = comboDrinkMapper.getComboIdsByDrinkId(ids);
        if(comboIds!=null&&comboIds.size()>0){
            //当前饮品被组合套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DRINK_BE_RELATED_BY_COMBO);
        }

        /*//删除饮品表中的饮品数据
        for (Long id : ids) {
            drinkMapper.deleteById(id);
            //删除饮品关联的规格数据
            drinkSpecMapper.deleteByDrinkId(id);
        }*/
        //根据饮品id集合批量删除饮品数据和规格数据
        drinkMapper.deleteByIds(ids);
        //根据饮品id集合批量删除组合套餐关联的饮品数据
        comboDrinkMapper.deleteByDrinkIds(ids);


    }
    /**
     * 根据饮品id查询饮品详情
     */
    @Override
    public DrinkVO getByIdWithSpec(Long id) {
        //根据饮品id查询饮品数据
        Drink drink = drinkMapper.getById(id);
        //根据饮品id查询饮品数据关联的规格数据
        List<DrinkSpec> specs = drinkSpecMapper.getByDrinkId(id);
        //将饮品数据和规格数据封装到饮品VO中
        DrinkVO drinkVO = new DrinkVO();
        BeanUtils.copyProperties(drink,drinkVO);
        drinkVO.setSpecs(specs);
        return drinkVO;
    }
    /**
     * 更新饮品和规格
     */
    @Override
    @Transactional
    public void updateWithSpec(DrinkDTO drinkDTO) {
        Drink drink = new Drink();
        BeanUtils.copyProperties(drinkDTO,drink);
        //修改饮品表基本信息
        drinkMapper.updateById(drink);
        //删除原有规格数据
        drinkSpecMapper.deleteByDrinkId(drink.getId());
        //重新插入规格数据
        List<DrinkSpec> specs = drinkDTO.getSpecs();
        if(specs!=null&&specs.size()>0){
            specs.forEach(ds->ds.setDrinkId(drinkDTO.getId()));
            //向规格表插入n条数据
            drinkSpecMapper.insertBatch(specs);
        }


    }
    /**
     * 根据分类id查询饮品
     */
    @Override
    public List<Drink> list(Long categoryId) {
        Drink drink = Drink.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return drinkMapper.list(drink);
    }

    /**
     * 条件查询饮品和规格
     * @param drink
     * @return
     */
    public List<DrinkVO> listWithSpec(Drink drink) {
        List<Drink> drinkList = drinkMapper.list(drink);

        List<DrinkVO> drinkVOList = new ArrayList<>();

        for (Drink d : drinkList) {
            DrinkVO drinkVO = new DrinkVO();
            BeanUtils.copyProperties(d,drinkVO);

            //根据饮品id查询对应的规格
            List<DrinkSpec> specs = drinkSpecMapper.getByDrinkId(d.getId());

            drinkVO.setSpecs(specs);
            drinkVOList.add(drinkVO);
        }

        return drinkVOList;
    }

    /**
     * 饮品上架下架
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Drink drink = Drink.builder()
                .id(id)
                .status(status)
                .build();
        drinkMapper.updateById(drink);
    }
}
