package com.example.elasticsearchhepler.service.impl;

import com.example.elasticsearchhepler.entity.BulkResponseResult;
import com.example.elasticsearchhepler.entity.TestIndexAutoEntity;
import com.example.elasticsearchhepler.exception.BizException;
import com.example.elasticsearchhepler.repository.ElasticsearchTemplate;
import com.example.elasticsearchhepler.service.TestIndexAutoService;
import com.github.pagehelper.PageInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TestIndexAutoServiceImpl implements TestIndexAutoService {

    @Resource
    private ElasticsearchTemplate<TestIndexAutoEntity,String> elasticsearchTemplate;

    /**
     * 插入或更新文档，全量覆盖
     * id不存在插入文档
     * id存在更新文档
     * @param param 数据对象
     * @return true：插入成功 false：插入失败
     * @throws BizException 自定义异常
     */
    @Override
    public boolean saveOrUpdateCover(TestIndexAutoEntity param) throws BizException {
        return elasticsearchTemplate.saveOrUpdateCover(param);
    }

    /**
     * 批量执行插入或修改操作，全量覆盖
     * id不存在插入文档
     * id存在更新文档
     * @param paramList
     * @return
     * @throws BizException
     */
    @Override
    public List<BulkResponseResult> saveOrUpdateCover(List<TestIndexAutoEntity> paramList) throws BizException {
        return elasticsearchTemplate.saveOrUpdateCover(paramList);
    }

    @Override
    public boolean deleteById(String id) throws BizException {
        return elasticsearchTemplate.deleteById(id,TestIndexAutoEntity.class);
    }

    @Override
    public List<BulkResponseResult> deleteByIds(List<String> ids) throws BizException {
        return elasticsearchTemplate.deleteByIds(ids,TestIndexAutoEntity.class);
    }

    @Override
    public Long deleteByQuery(QueryBuilder queryBuilder) throws BizException {
        return elasticsearchTemplate.deleteByQuery(queryBuilder,TestIndexAutoEntity.class);
    }

    @Override
    public TestIndexAutoEntity getById(String id) throws BizException {
        return elasticsearchTemplate.getById(id,TestIndexAutoEntity.class);
    }

    @Override
    public List<TestIndexAutoEntity> mGetById(List<String> ids) throws BizException {
        return elasticsearchTemplate.mGetById(ids,TestIndexAutoEntity.class);
    }

    @Override
    public boolean exists(String id) throws BizException {
        return elasticsearchTemplate.exists(id,TestIndexAutoEntity.class);
    }

    /**
     * 根据id修改，只修改字段值不为null的字段
     *
     * @param param
     */
    @Override
    public boolean updateById(TestIndexAutoEntity param) throws BizException {
        return elasticsearchTemplate.updateById(param);
    }

    /**
     * 根据查询条件修改
     * @param queryBuilder 查询参数
     * @param updateParams 更新参数(如果有多个条件、条件之间是并且关系)
     * @return
     * @throws BizException
     */
    @Override
    public Long updateByQuery(QueryBuilder queryBuilder,Map<String,Object> updateParams) throws BizException {
        return elasticsearchTemplate.updateByQuery(queryBuilder,TestIndexAutoEntity.class,updateParams);
    }

    @Override
    public List<TestIndexAutoEntity> searchList(SearchSourceBuilder queryBuilder) throws BizException {
        return elasticsearchTemplate.searchList(queryBuilder,TestIndexAutoEntity.class);
    }

    @Override
    public PageInfo<TestIndexAutoEntity> searchPage(SearchSourceBuilder queryBuilder, int pageNo, int pageSize) throws BizException {
        return elasticsearchTemplate.searchPage(queryBuilder,pageNo,pageSize,TestIndexAutoEntity.class);
    }

    @Override
    public List<String> completionSuggest(String fieldName, String fieldValue) throws BizException {
        return elasticsearchTemplate.completionSuggest(fieldName,fieldValue,TestIndexAutoEntity.class);
    }

    @Override
    public List<String> completionSearchAsYouType(String fieldName, String fieldValue) throws BizException {
        return elasticsearchTemplate.completionSearchAsYouType(fieldName,fieldValue,TestIndexAutoEntity.class);
    }

    @Override
    public Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId) throws BizException {
        return elasticsearchTemplate.scroll(queryBuilder,scrollId,TestIndexAutoEntity.class);
    }

    @Override
    public Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId,long time) throws BizException {
        return elasticsearchTemplate.scroll(queryBuilder,scrollId,TestIndexAutoEntity.class,time);
    }

    /**
     * 根据查询条件查询原始结果,不分页
     * @param builder
     * @return
     * @throws BizException
     */
    @Override
    public SearchResponse searchOrigin(SearchSourceBuilder builder) throws BizException {
        return elasticsearchTemplate.searchOrigin(builder,TestIndexAutoEntity.class);
    }

    /**
     * 根据查询条件统计总条数
     * @param builder
     * @return
     * @throws BizException
     */
    @Override
    public Long countTotal(QueryBuilder builder) throws BizException {
        return elasticsearchTemplate.countTotal(builder,TestIndexAutoEntity.class);
    }



}
