package com.example.elasticsearchhepler.service;

import com.example.elasticsearchhepler.entity.BulkResponseResult;
import com.example.elasticsearchhepler.exception.BizException;
import com.github.pagehelper.PageInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;
import java.util.Map;

public interface EsBaseService<T> {

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
     * @param ts
     * @return
     * @throws BizException
     */
    List<BulkResponseResult> saveOrUpdateCover(List<T> ts) throws BizException;

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    boolean deleteById(String id) throws BizException;

    /**
     * 根据id集合删除
     * @param ids
     * @return
     */
    List<BulkResponseResult> deleteByIds(List<String> ids) throws BizException;

    /**
     * 根据查询条件删除
     * @param queryBuilder 删除条件实体
     * @return 删除的条数
     * @throws BizException 自定义异常
     */
    Long deleteByQuery(QueryBuilder queryBuilder) throws BizException;

    /**
     * 通过Id获取记录
     *
     * @param id 主键
     * @return
     */
    T getById(String id) throws BizException;

    /**
     * 通过Id集合获取记录
     *
     * @param ids 主键集合
     * @return
     */
    List<T> mGetById(List<String> ids) throws BizException;

    /**
     * 根据id判断是否存在
     *
     * @param id
     * @return
     */
    boolean exists(String id) throws BizException;

    /**
     * 根据id修改，只修改字段值不为null的字段
     * @param t
     * @return
     */
    boolean updateById(T t) throws BizException;

    /**
     * 查询并更新数据
     *
     * @param queryBuilder 查询参数
     * @param updateParams 更新参数(如果有多个条件、条件之间是并且关系)
     * @return 修改条数
     * @throws Exception
     */
    Long updateByQuery(QueryBuilder queryBuilder,Map<String,Object> updateParams) throws BizException;

    /**
     * 根据查询条件查询  返回原始结果集，需要自行处理
     *
     * @param builder
     * @return
     * @throws Exception
     */
    SearchResponse searchOrigin(SearchSourceBuilder builder) throws BizException;

    /**
     * 根据查询条件统计总数
     * @param builder
     * @return
     * @throws BizException
     */
    Long countTotal(QueryBuilder builder) throws BizException;

    /**
     * 根据查询条件查询，查询指定分页范围的数据集,不包含分页信息
     *
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    List<T> searchList(SearchSourceBuilder queryBuilder) throws BizException;

    /**
     * 根据查询条件分页查询,包含分页信息
     *
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    PageInfo<T> searchPage(SearchSourceBuilder queryBuilder,int pageNo ,int pageSize) throws BizException;

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
     * @return
     * @throws Exception
     */
    List<String> completionSuggest(String fieldName, String fieldValue) throws BizException;

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
     * @return
     * @throws Exception
     */
    List<String> completionSearchAsYouType(String fieldName, String fieldValue) throws BizException;

    /**
     * scroll方式查询(默认了保留时间为DEFAULT_SCROLL_TIME)
     * map>result:为结果集
     * map>scrollId:为scrollId
     * @param queryBuilder
     * @return
     * @throws Exception
     */
    Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId) throws BizException;

    /**
     * scroll方式查询
     * map>result:为结果集
     * map>scrollId:为scrollId
     *
     * @param queryBuilder
     * @param time         查询上下文有效时间 设置合理满足查询数据的时间即可,单位是分钟
     * @return
     * @throws Exception
     */
    Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId, long time) throws BizException;
}

