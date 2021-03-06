package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.BrandCategoryMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author jinluying
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Override
    @Transactional
    public Result<JSONObject> addCategory(CategoryEntity entity) {

        //通过新增节点的pid将父节点的parent状态改为1
        CategoryEntity parentCategory = new CategoryEntity();
        parentCategory.setId(entity.getParentId());
        parentCategory.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentCategory);

        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {

        List<CategoryEntity> byBrandId =  categoryMapper.getByBrandId(brandId);
        return this.setResultSuccess(byBrandId);
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByIdList(String cidStr) {

        List<Integer> cidList = Arrays.asList(cidStr.split(","))
                .stream().map(cid -> Integer.parseInt(cid))
                        .collect(Collectors.toList());


        List<CategoryEntity> list = categoryMapper.selectByIdList(cidList);
        return this.setResultSuccess(list);
    }

    @Override
    @Transactional
    public Result<JsonObject> delCategory(Integer id) {

        //通过当前id查询
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        //通过id查询当前节点是否存在
        if(ObjectUtil.isNull(categoryEntity)){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前id不存在");
        }
        //通过id查询当前节点是否为父级节点
        if(categoryEntity.getIsParent() == 1){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前节点为父节点,不能删除");
        }

        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("cid3",categoryEntity.getId());

        List<SpuEntity> spuEntityList = spuMapper.selectByExample(example);
        if(spuEntityList.size()>0){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前分类被商品绑定,不能删除");
        }

        Integer count = categoryMapper.getByCategoryId(id);
        if(count > 0){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前分类被品牌绑定,不能删除");
        }

        Integer count1 = categoryMapper.getSepcGroup(id);
        if(count1 > 0){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前分类被规格组绑定,不能删除");
        }

        this.editIsParent(categoryEntity);

        categoryMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> editCategory(CategoryEntity entity) {

        categoryMapper.updateByPrimaryKeySelective(entity);

        return this.setResultSuccess();
    }

    public void editIsParent(CategoryEntity categoryEntity){

        //构建条件查询 通过当前被删除节点的parentid查询数据
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());

        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        //判断查询结果 只有一条数据
        if(list.size() == 1){
            //将父节点的isParent状态改为0
            CategoryEntity parentCategory = new CategoryEntity();
            parentCategory.setId(categoryEntity.getParentId());
            parentCategory.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentCategory);
        }
    }
}
