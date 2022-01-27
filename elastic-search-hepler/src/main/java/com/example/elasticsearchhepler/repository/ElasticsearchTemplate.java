package com.example.elasticsearchhepler.repository;

import com.example.elasticsearchhepler.entity.BaseEsEntity;
import com.example.elasticsearchhepler.entity.BulkResponseResult;
import com.example.elasticsearchhepler.exception.BizException;
import com.github.pagehelper.PageInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch基础功能组件
 */
public interface ElasticsearchTemplate<T extends BaseEsEntity, M> {

    /**
     * 插入或更新文档，全量覆盖
     * id不存在插入文档
     * id存在更新文档
     * @param t 数据对象
     * @return true：插入成功 false：插入失败
     * @throws BizException 自定义异常
     */
    boolean saveOrUpdateCover(T t) throws BizException;

    /**
     * 批量执行插入或修改操作，全量覆盖
     * id不存在插入文档
     * id存在更新文档
     * @param list
     * @return
     * @throws BizException
     */
    List<BulkResponseResult> saveOrUpdateCover(List<T> list) throws BizException;

    /**
     * 根据id删除
     *
     * @param id
     */
    boolean deleteById(M id, Class<T> clazz) throws BizException;

    /**
     * 批量删除
     *
     * @param list
     */
    List<BulkResponseResult> deleteByIds(List<M> list,Class<T> clazz) throws BizException;

    /**
     * 根据查询条件删除
     *
     * @param queryBuilder 查询条件实体
     * @param clazz 索引实体
     * @return
     * @throws BizException
     */
    Long deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws BizException;

    /**
     * 根据ID查询
     *
     * @param id
     * @param clazz
     * @return
     * @throws BizException
     */
    T getById(M id, Class<T> clazz) throws BizException;

    /**
     * 根据ID列表批量查询
     *
     * @param ids
     * @param clazz
     * @return
     * @throws BizException
     */
    List<T> mGetById(List<M> ids, Class<T> clazz) throws BizException;

    /**
     * id数据是否存在
     *
     * @param id
     * @param clazz
     * @return
     */
    boolean exists(M id, Class<T> clazz) throws BizException;

    /**
     * 根据id修改，只修改字段值不为null的字段
     *
     * @param t
     */
    boolean updateById(T t) throws BizException;

    /**
     * 查询并更新数据
     *
     * @param queryBuilder 查询参数
     * @param updateParams 更新参数(如果有多个条件、条件之间是并且关系)
     * @param clazz       索引实体
     * @return 修改条数
     * @throws Exception
     */
    Long updateByQuery(QueryBuilder queryBuilder, Class<T> clazz,Map<String,Object> updateParams) throws BizException;

    /**
     * 根据查询条件查询  返回原始结果集，需要自行处理
     *
     * @param builder
     * @return
     * @throws Exception
     */
    SearchResponse searchOrigin(SearchSourceBuilder builder, Class<T> clazz) throws BizException;

    /**
     * 根据查询条件统计总数
     * @param builder
     * @param clazz
     * @return
     * @throws BizException
     */
    Long countTotal(QueryBuilder builder, Class<T> clazz) throws BizException;

    /**
     * 根据查询条件查询,查询指定分页范围的数据集,不包含分页信息
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    List<T> searchList(SearchSourceBuilder queryBuilder, Class<T> clazz) throws BizException;

    /**
     * 根据查询条件分页查询,包含分页信息
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    PageInfo<T> searchPage(SearchSourceBuilder queryBuilder,int pageNo ,int pageSize, Class<T> clazz) throws BizException;

    /**
     * 搜索建议 使用completion方式
     * 此种方式构建成本较高，数据采用FST存储在堆内存
     * _source的大小会影响性能。为了节省一些网络开销，可以使用源过滤从_source中过滤掉不必要的字段，以最小化_source的大小
     * 支持前缀、模糊、正则查询
     * 使用这个接口前置条件是fieldName 字段必须有名为completion  字段类型为 "type":"completion"的子字段
     * {
     *   "mappings": {
     *     "properties": {
     *       "user_title": {
     *         "type": "text",
     *         "analyzer": "ik_max_word",
     *         "search_analyzer": "ik_max_word",
     *         "fields": {
     *           "completion":{
     *             "type":"completion"
     *           }
     *         }
     *       }
     *     }
     *   }
     * }
     * @param fieldName
     * @param fieldValue
     * @param clazz
     * @return
     * @throws Exception
     */
    List<String> completionSuggest(String fieldName, String fieldValue, Class<T> clazz) throws BizException;

    /**
     * 搜索建议 使用search_as_you_type 方式
     * 支持前缀补全和中缀补全
     * 使用这个接口前置条件是fieldName 字段必须有名为search_as_you_type  字段类型为 "type":"search_as_you_type"的子字段
     * {
     *   "mappings": {
     *     "properties": {
     *       "user_title": {
     *         "type": "text",
     *         "analyzer": "ik_max_word",
     *         "search_analyzer": "ik_max_word",
     *         "fields": {
     *           "search_as_you_type":{
     *             "type":"search_as_you_type"
     *           }
     *         }
     *       }
     *     }
     *   }
     * }
     * @param fieldName
     * @param fieldValue
     * @param clazz
     * @return
     * @throws Exception
     */
    List<String> completionSearchAsYouType(String fieldName, String fieldValue, Class<T> clazz) throws BizException;

    /**
     * scroll方式查询(默认了保留时间为DEFAULT_SCROLL_TIME)
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId, Class<T> clazz) throws BizException;

    /**
     * scroll方式查询
     *
     * @param queryBuilder
     * @param clazz
     * @param time         保留小时
     * @return
     * @throws Exception
     */
    Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId, Class<T> clazz, long time) throws BizException;

    /**
     * 使用searchAfter的方式查询
     * @param queryBuilder
     * @param clazz
     * @param time
     * @return
     * @throws BizException
     */
    List<T> searchAfter(SearchSourceBuilder queryBuilder, Class<T> clazz, long time) throws BizException;
}
