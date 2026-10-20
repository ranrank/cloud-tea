package com.cloudtea.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.cloudtea.constant.StatusConstant;
import com.cloudtea.dto.ComboDTO;
import com.cloudtea.dto.ComboPageQueryDTO;
import com.cloudtea.entity.Drink;
import com.cloudtea.entity.Combo;
import com.cloudtea.entity.ComboDrink;
import com.cloudtea.exception.DeletionNotAllowedException;
import com.cloudtea.exception.ComboEnableFailedException;
import com.cloudtea.mapper.DrinkMapper;
import com.cloudtea.mapper.ComboDrinkMapper;
import com.cloudtea.mapper.ComboMapper;
import com.cloudtea.result.PageResult;
import com.cloudtea.service.ComboService;
import com.cloudtea.vo.DrinkItemVO;
import com.cloudtea.vo.ComboVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ComboServiceImpl implements ComboService {
    @Autowired
    private ComboMapper comboMapper;
    @Autowired
    private ComboDrinkMapper comboDrinkMapper;
    @Autowired
    private DrinkMapper drinkMapper;

    @Override
    @Transactional
    public void saveWithDrink(ComboDTO comboDTO) {
        Combo combo = new Combo();
        BeanUtils.copyProperties(comboDTO, combo);
        //新增组合套餐
        comboMapper.insert(combo);
        //获取组合套餐id
        Long comboId = combo.getId();
        List<ComboDrink> comboDrinkList = comboDTO.getComboDrinks();
        comboDrinkList.forEach(comboDrink -> {
            comboDrink.setComboId(comboId);
        });
        //新增组合套餐饮品关系
        comboDrinkMapper.insertBatch(comboDrinkList);


    }

    @Override
    public PageResult page(ComboPageQueryDTO comboPageQueryDTO) {
        PageHelper.startPage(comboPageQueryDTO.getPage(), comboPageQueryDTO.getPageSize());
        Page<ComboVO> page = comboMapper.pageQuery(comboPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());

    }
    /**
     * 批量删除组合套餐
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        ids.forEach(id ->{
            Combo combo = comboMapper.getById(id);
            if(combo.getStatus() == StatusConstant.ENABLE){
                //上架状态的组合套餐不能删除
                throw new DeletionNotAllowedException("上架状态的组合套餐不能删除");
            }
        });
        ids.forEach(id -> {
            //删除组合套餐
            comboMapper.deleteById(id);
            //删除组合套餐饮品关系
            comboDrinkMapper.deleteByComboId(id);
        });

    }

    @Override
    public ComboVO getById(Long id) {
        Combo combo = comboMapper.getById(id);
        List<ComboDrink> comboDrinkList = comboDrinkMapper.getByComboId(id);
        ComboVO comboVO = new ComboVO();
        BeanUtils.copyProperties(combo, comboVO);
        comboVO.setComboDrinks(comboDrinkList);
        return comboVO;
    }
    /**
     * 更新组合套餐
     * @param comboDTO
     */
    @Override
    @Transactional
    public void update(ComboDTO comboDTO) {
        Combo combo = new Combo();
        BeanUtils.copyProperties(comboDTO, combo);
        //更新组合套餐
        comboMapper.update(combo);
        //组合套餐id
        Long comboId = combo.getId();
        //删除组合套餐饮品关系
        comboDrinkMapper.deleteByComboId(comboId);
        //新增组合套餐饮品关系
        List<ComboDrink> comboDrinkList = comboDTO.getComboDrinks();
        comboDrinkList.forEach(comboDrink -> {
            comboDrink.setComboId(comboId);
        });
        //新增组合套餐饮品关系
        comboDrinkMapper.insertBatch(comboDrinkList);
    }
    /**
     * 更新组合套餐状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //上架组合套餐时判断组合套餐内是否有下架饮品
        if(status == StatusConstant.ENABLE){
            //查询组合套餐内是否有下架饮品
            List<Drink> drinkList = drinkMapper.getByComboId(id);

            if(drinkList!=null&&drinkList.size()>0){
                drinkList.forEach(drink -> {
                    if(drink.getStatus() == StatusConstant.DISABLE){
                        //有下架饮品，不能上架
                        throw new ComboEnableFailedException("有下架饮品，不能上架");
                    }
                });
            }
        }

        Combo combo = Combo.builder()
                .id(id)
                .status(status)
                .build();
        //更新组合套餐状态
        comboMapper.update(combo);
    }

    /**
     * 条件查询
     * @param combo
     * @return
     */
    public List<Combo> list(Combo combo) {
        List<Combo> list = comboMapper.list(combo);
        return list;
    }

    /**
     * 根据id查询饮品选项
     * @param id
     * @return
     */
    public List<DrinkItemVO> getDrinkItemById(Long id) {
        return comboMapper.getDrinkItemByComboId(id);
    }
}