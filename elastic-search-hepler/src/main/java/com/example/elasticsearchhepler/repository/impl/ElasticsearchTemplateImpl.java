package com.example.elasticsearchhepler.repository.impl;

import com.alibaba.fastjson.JSON;
import com.example.elasticsearchhepler.entity.BaseEsEntity;
import com.example.elasticsearchhepler.entity.BulkResponseResult;
import com.example.elasticsearchhepler.exception.BizException;
import com.example.elasticsearchhepler.repository.ElasticsearchTemplate;
import com.example.elasticsearchhepler.utils.ElasticSearchHelpUtils;
import com.example.elasticsearchhepler.utils.EmptyUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Elasticsearch 增删改查基础功能
 **/
@Component
@Slf4j
public class ElasticsearchTemplateImpl<T extends BaseEsEntity, M> implements ElasticsearchTemplate<T, M> {
    //非分页，默认的查询条数
    private static final int DEFALT_PAGE_SIZE = 200;
    //搜索建议默认条数
    private static final int COMPLETION_SUGGESTION_SIZE = 10;
    //SCROLL 查询上下文有效时间 默认给10分钟
    private static final long DEFAULT_SCROLL_TIME = 10;
    //SCROLL查询 每页默认条数
    private static final int DEFAULT_SCROLL_PERPAGE = 100;
    //默认编码
    private static final String DEFAULT_CHARSET = "UTF-8";
    //使用scroll查询返回结果的map中scrollId的key
    public static final String SCROLL_ID = "scrollId";
    //使用scroll查询返回结果的map中结果集的可以
    public static final String SCROLL_SEARCH_RESULT = "result";

    @Resource
    private RestHighLevelClient client;

    @Resource
    private ElasticSearchHelpUtils<T> elasticSearchHelpUtils;

    /**
     * 插入或更新文档，全量覆盖
     * id不存在插入文档
     * id存在更新文档
     * @param t 数据对象
     * @return true：插入成功 false：插入失败
     * @throws BizException 自定义异常
     */
    @Override
    public boolean saveOrUpdateCover(T t) throws BizException {

        if (EmptyUtil.isEmpty(t)){
            return false;
        }
        IndexRequest request = new IndexRequest(elasticSearchHelpUtils.getIndexName(t));
        request.id(t.getId());
        request.source(JSON.toJSONString(elasticSearchHelpUtils.objectToMap(t)), XContentType.JSON);
        try {
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            if (EmptyUtil.isEmpty(response)){
                return false;
            }
            log.info("文档执行save方法，返回结果：{}",JSON.toJSONString(response));
            return EmptyUtil.isNotEmpty(response.getResult());
        }catch (IOException e){
            log.error("文档id:{} 索引到ES出错 出错原因：",t.getId(),e);
            throw new BizException(0,"索引到ES出错 出错原因："+e.getMessage());
        }
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
    public List<BulkResponseResult> saveOrUpdateCover(List<T> paramList) throws BizException {
        if (EmptyUtil.isEmpty(paramList)) {
            return Collections.emptyList();
        }
        BulkRequest request = new BulkRequest();
        for (T t:paramList) {
            request.add(new IndexRequest(elasticSearchHelpUtils.getIndexName(t))
                    .id(t.getId()).source(JSON.toJSONString(elasticSearchHelpUtils.objectToMap(t)), XContentType.JSON));
        }
        try {
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            log.info("文档执行save 批量操作方法，返回结果：{}",JSON.toJSONString(bulkResponse));
            return elasticSearchHelpUtils.convertBulkResponseResult(bulkResponse);
        } catch (IOException e) {
            log.error("文档批量索引到ES出错 出错原因：",e);
            throw new BizException(0,"文档批量索引到ES出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据id删除
     * id不存在时返回false
     * @param id
     * @return
     * @throws BizException
     */
    @Override
    public boolean deleteById(M id, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(id)){
            return false;
        }
        DeleteRequest deleteRequest = new DeleteRequest(elasticSearchHelpUtils.getIndexName(clazz),id.toString());
        try {
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
            if (EmptyUtil.isEmpty(deleteResponse)){
                return false;
            }
            return deleteResponse.getResult() == DocWriteResponse.Result.DELETED;
        }catch (IOException e){
            log.error("根据id：{}删除文档出错 出错原因：",id,e);
            throw new BizException(0,"删除文档出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据id集合批量删除
     * @param ids
     */
    @Override
    public List<BulkResponseResult> deleteByIds(List<M> ids,Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (M id:ids) {
            bulkRequest.add(new DeleteRequest(elasticSearchHelpUtils.getIndexName(clazz), id.toString()));
        }
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("文档执行批量删除操作，返回结果：{}",JSON.toJSONString(bulkResponse));
            return elasticSearchHelpUtils.convertBulkResponseResult(bulkResponse);
        }catch (IOException e){
            log.error("根据id集合：{}批量删除文档出错 出错原因：",ids,e);
            throw new BizException(0,"批量删除文档出错 出错原因："+e.getMessage());
        }
    }

    /**
     * Delete by Query 根据查询结果删除
     * 返回结果：删除了多少条记录
     * @param queryBuilder 查询条件实体
     * @param clazz 索引实体
     * @return
     * @throws BizException
     */
    @Override
    public Long deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(queryBuilder)){
            return 0L;
        }
        DeleteByQueryRequest request = new DeleteByQueryRequest(elasticSearchHelpUtils.getIndexName(clazz));
        request.setQuery(queryBuilder);
        // 默认情况下，DeleteByQueryRequest可以批量处理1000个文档
        // 可以使用setBatchSize更改批处理大小。
        request.setBatchSize(10000);
        // 默认情况下，版本冲突会中止DeleteByQueryRequest进程
        // 下面设置版本冲突时继续
        request.setConflicts("proceed");
        try {
            BulkByScrollResponse bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
            return bulkResponse.getDeleted();
        } catch (IOException e) {
            log.error("根据查询条件删除文档出错出错 出错原因：",e);
            throw new BizException(0,"根据查询条件删除文档出错出错 出错原因："+e.getMessage());
        }
    }

    @Override
    public T getById(M id, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(id)){
            return null;
        }
        GetRequest getRequest = new GetRequest(elasticSearchHelpUtils.getIndexName(clazz), id.toString());
        try {
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            if (EmptyUtil.isEmpty(getResponse) || !getResponse.isExists()) {
                return null;
            }
            log.info("map:"+getResponse.getSourceAsMap());
            log.info("String:"+getResponse.getSourceAsString());
            return JSON.parseObject(getResponse.getSourceAsString(), clazz);
        } catch (IOException e) {
            log.error("根据id：{}查询文档出错 出错原因：",id,e);
            throw new BizException(0,"根据ID查询文档出错 出错原因："+e.getMessage());
        }
    }

    @Override
    public List<T> mGetById(List<M> ids, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        MultiGetRequest request = new MultiGetRequest();
        for (M m:ids) {
            request.add(new MultiGetRequest.Item(elasticSearchHelpUtils.getIndexName(clazz),m.toString()));
        }

        try {
            MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
            if (EmptyUtil.isEmpty(response)){
                return Collections.emptyList();
            }
            List<T> list = new ArrayList<>();
            for (int i = 0; i < response.getResponses().length; i++) {
                MultiGetItemResponse item = response.getResponses()[i];
                GetResponse getResponse = item.getResponse();
                if (getResponse.isExists()) {
                    log.info("map:"+getResponse.getSourceAsMap());
                    log.info("String:"+getResponse.getSourceAsString());
                    list.add(JSON.parseObject(getResponse.getSourceAsString(), clazz));
                }
            }
            return list;
        }catch (IOException e){
            log.error("根据ids：{}查询文档出错 出错原因：",ids,e);
            throw new BizException(0,"批量查询文档出错 出错原因："+e.getMessage());
        }
    }

    @Override
    public boolean exists(M id, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(id)){
            return false;
        }
        GetRequest getRequest = new GetRequest(elasticSearchHelpUtils.getIndexName(clazz), id.toString());
        try {
            return client.exists(getRequest, RequestOptions.DEFAULT);
        }catch (IOException e){
            log.error("根据id：{}查询文档是否存在 出错原因：",id,e);
            throw new BizException(0,"根据ID查询文档是否存在出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据id修改，值为空会被修改成空值、默认值
     * @param t
     * @return
     * @throws BizException
     */
    @Override
    public boolean updateById(T t) throws BizException {
        if (EmptyUtil.isEmpty(t) || EmptyUtil.isEmpty(t.getId())){
            return false;
        }
        UpdateRequest updateRequest = new UpdateRequest(elasticSearchHelpUtils.getIndexName(t),t.getId());
        updateRequest.doc(JSON.toJSONString(elasticSearchHelpUtils.objectToMap(t)), XContentType.JSON);
        try {
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            log.info("文档执行修改方法，返回结果：{}",JSON.toJSONString(updateResponse));
            return EmptyUtil.isNotEmpty(updateResponse.getResult());
        }catch (IOException e){
            log.error("根据id：{}修改文档出错 出错原因：",t.getId(),e);
            throw new BizException(0,"修改文档出错出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据查询条件更新数据
     *
     * @param queryBuilder 查询参数
     * @param updateParams    更新参数(如果有多个条件、条件之间是并且关系)
     * @param clazz       索引实体
     * @return 修改的条数
     * @throws Exception
     */
    @Override
    public Long updateByQuery(QueryBuilder queryBuilder, Class<T> clazz,Map<String,Object> updateParams) throws BizException {

        if (EmptyUtil.isEmpty(queryBuilder) || EmptyUtil.isEmpty(updateParams)){
            return 0L;
        }

        UpdateByQueryRequest request = new UpdateByQueryRequest(elasticSearchHelpUtils.getIndexName(clazz));
        request.setQuery(queryBuilder);
        request.setConflicts("proceed");
        request.setBatchSize(10000);

        List<String> updateList = new ArrayList<>();
        updateParams.forEach((k,v)->{
            if (v instanceof String){
                updateList.add("ctx._source." + k + "='" + v+"'");
            }else{
                updateList.add("ctx._source." + k + "=" + v);
            }
        });
        String updateSource = String.join(";", updateList);

        request.setScript(new Script(ScriptType.INLINE, "painless", updateSource, Collections.emptyMap()));
        try {
            BulkByScrollResponse bulkByScrollResponse = client.updateByQuery(request, RequestOptions.DEFAULT);
            return bulkByScrollResponse.getUpdated();
        } catch (IOException e) {
            log.error("根据查询条件修改文档出错 出错原因：",e);
            throw new BizException(0,"根据查询条件修改文档出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据查询条件查询  返回原始结果集，需要自行处理
     *
     * @param builder
     * @return
     * @throws Exception
     */
    @Override
    public SearchResponse searchOrigin(SearchSourceBuilder builder, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(builder)){
            return null;
        }
        SearchRequest request = new SearchRequest(elasticSearchHelpUtils.getIndexName(clazz));
        request.source(builder);
        try {
            return client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("根据查询条件查询文档出错 出错原因：",e);
            throw new BizException(0,"根据查询条件查询文档出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据查询条件统计总数
     *
     * @param builder
     * @return
     */
    public Long countTotal(QueryBuilder builder, Class<T> clazz) throws BizException {
        try {
            CountRequest countRequest = new CountRequest(new String[]{elasticSearchHelpUtils.getIndexName(clazz)},builder);
            CountResponse count = client.count(countRequest, RequestOptions.DEFAULT);
            return count.getCount();
        } catch (IOException e) {
            log.error("根据查询条件统计总数出错 出错原因：",e);
            throw new BizException(0,"根据查询条件统计总数出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据查询条件查询,查询指定分页范围的数据集,不包含分页信息
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    @Override
    public List<T> searchList(SearchSourceBuilder queryBuilder, Class<T> clazz) throws BizException {
        if (EmptyUtil.isEmpty(queryBuilder)){
            return Collections.emptyList();
        }

        SearchRequest searchRequest = new SearchRequest(elasticSearchHelpUtils.getIndexName(clazz));
        searchRequest.source(queryBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (EmptyUtil.isEmpty(hits)){
                return Collections.emptyList();
            }
            List<T> list = new ArrayList<>();
            for (SearchHit hit : hits) {
                list.add(JSON.parseObject(hit.getSourceAsString(), clazz));
            }
            return list;
        }catch (IOException e){
            log.error("根据查询条件查询结果出错 出错原因：",e);
            throw new BizException(0,"根据查询条件查询结果出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 根据查询条件分页查询,包含分页信息
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    @Override
    public PageInfo<T> searchPage(SearchSourceBuilder queryBuilder,int pageNo ,int pageSize , Class<T> clazz) throws BizException {

        SearchRequest searchRequest = new SearchRequest(elasticSearchHelpUtils.getIndexName(clazz));
        searchRequest.source(queryBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();

            List<T> list = new ArrayList<>();
            if (EmptyUtil.isNotEmpty(searchHits)) {
                for (SearchHit hit : searchHits) {
                    list.add(JSON.parseObject(hit.getSourceAsString(), clazz));
                }
            }

            PageInfo<T> pageInfo = new PageInfo<>();
            pageInfo.setList(list);
            pageInfo.setPageNum(pageNo);
            pageInfo.setPageSize(pageSize);
            pageInfo.setTotal(hits.getTotalHits().value);
            pageInfo.setPages(elasticSearchHelpUtils.getTotalPages(hits.getTotalHits().value, pageSize));
            return pageInfo;
        }catch (IOException e){
            log.error("根据查询条件查询结果出错 出错原因：",e);
            throw new BizException(0,"根据查询条件查询结果出错 出错原因："+e.getMessage());
        }

    }

    @Override
    public List<String> completionSuggest(String fieldName, String fieldValue, Class<T> clazz) throws BizException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();

        //prefix 使用前缀查询
        //skipDuplicates:true 过滤掉重复内容
        CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder(fieldName+".completion").skipDuplicates(true).prefix(fieldValue);
        completionSuggestionBuilder.size(COMPLETION_SUGGESTION_SIZE);
        //建议名，同一个suggest下可以多个建议名
        suggestBuilder.addSuggestion("suggest_" + fieldName, completionSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);

        SearchRequest searchRequest = new SearchRequest(elasticSearchHelpUtils.getIndexName(clazz));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            Suggest suggest = searchResponse.getSuggest();
            CompletionSuggestion completionSuggestion = suggest.getSuggestion("suggest_" + fieldName);
            List<String> list = new ArrayList<>();
            for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
                for (CompletionSuggestion.Entry.Option option : entry) {
                    list.add(option.getText().string());
                }
            }
            return list;
        }catch (IOException e){
            log.error("使用completionSuggest搜索推荐内容出错 搜索字段名：{} 搜索字段值：{} 错误原因：{}",fieldName,fieldValue,e);
            throw new BizException(0,"搜索推荐内容出错 搜索字段名："+fieldName+" 搜索字段值："+fieldValue+" 出错原因："+e.getMessage());
        }

    }

    @Override
    public List<String> completionSearchAsYouType(String fieldName, String fieldValue, Class<T> clazz) throws BizException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //类型使用bool_prefix,多个词之间是and关系
        String[] fields = new String[]{fieldName+".search_as_you_type",fieldName+".search_as_you_type._2gram",fieldName+".search_as_you_type._3gram"};
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(fieldValue,fields).operator(Operator.AND).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX);
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(COMPLETION_SUGGESTION_SIZE);

        SearchRequest searchRequest = new SearchRequest(elasticSearchHelpUtils.getIndexName(clazz));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();

            if (EmptyUtil.isEmpty(searchHits)){
                return Collections.emptyList();
            }
            List<String> list = new ArrayList<>();
            for (SearchHit hit:searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                list.add(sourceAsMap.getOrDefault(fieldName,"").toString());
            }

            return list.stream().filter(EmptyUtil::isNotEmpty).distinct().collect(Collectors.toList());
        }catch (IOException e){
            log.error("使用completionSearchAsYouType搜索推荐内容出错 搜索字段名：{} 搜索字段值：{} 错误原因：{}",fieldName,fieldValue,e);
            throw new BizException(0,"搜索推荐内容出错 搜索字段名："+fieldName+" 搜索字段值："+fieldValue+" 出错原因："+e.getMessage());
        }
    }

    @Override
    public Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId, Class<T> clazz) throws BizException {
        return scroll(queryBuilder,scrollId ,clazz, DEFAULT_SCROLL_TIME);
    }

    @Override
    public Map<String,Object> scroll(SearchSourceBuilder queryBuilder,String scrollId, Class<T> clazz, long time) throws BizException {
        if (EmptyUtil.isEmpty(queryBuilder) && EmptyUtil.isEmpty(scrollId)) {
            return Collections.emptyMap();
        }
        Map<String,Object> map = null;
        if (EmptyUtil.isEmpty(scrollId)){
            //第一次查询使用scroll查询没有scrollId,需要调用创建scrollId的请求
            //把ES服务端返回的scrollId返回给调用方，查询后面的数据时只需要传入scrollId即可
            map = scrollSearchNoScrollId(queryBuilder, clazz, time);
        }else{
            //根据之前返回的scrollId查询下一页数据
            map = scrollSearchHaveScrollId(scrollId, clazz, time);
        }
        //判断是否需要清除scrollId,需要清除则执行清除操作
        clearScrollId(map);
        return map;
    }

    /**
     * 没有scrollId时,创建scrollId并返回第一个页的数据
     * @param queryBuilder
     * @param clazz
     * @param time
     * @return
     * @throws BizException
     */
    private Map<String,Object> scrollSearchNoScrollId(SearchSourceBuilder queryBuilder, Class<T> clazz, long time) throws BizException{
        //scroll请求每次都会生成一个新的scroll_id,大量请求时会创建很多的scroll_id，因此上下文有效时间不宜过长，否则会长时间占用内存和文件句柄
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(time));
        SearchRequest searchRequest = new SearchRequest(elasticSearchHelpUtils.getIndexName(clazz));
        searchRequest.scroll(scroll);
        searchRequest.source(queryBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            return getMapResult(searchResponse,clazz);
        }catch (IOException e) {
            log.error("使用Scroll查询内容出错 错误原因：",e);
            throw new BizException(0,"使用Scroll查询内容出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 有scrollId时,使用scrollId查询后面页数的数据集
     * @param scrollId
     * @param clazz
     * @param time
     * @return
     * @throws BizException
     */
    private Map<String,Object> scrollSearchHaveScrollId(String scrollId, Class<T> clazz, long time) throws BizException{
        try {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            //scroll请求每次都会生成一个新的scroll_id,大量请求时会创建很多的scroll_id，因此上下文有效时间不宜过长，否则会长时间占用内存和文件句柄
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(time));
            scrollRequest.scroll(scroll);
            SearchResponse searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            return getMapResult(searchResponse,clazz);
        }catch (IOException e){
            log.error("使用Scroll_ID查询内容出错 错误原因：",e);
            throw new BizException(0,"使用Scroll_ID查询内容出错 出错原因："+e.getMessage());
        }
    }

    /**
     * 将查询到的结果集组装到map中返回
     * @param searchResponse
     * @param clazz
     * @return
     */
    private Map<String,Object> getMapResult(SearchResponse searchResponse,Class<T> clazz){
        Map<String,Object> map = new HashMap<>();

        if (EmptyUtil.isEmpty(searchResponse)){
            map.put(SCROLL_SEARCH_RESULT, null);
            map.put(SCROLL_ID, null);
            return map;
        }
        List<T> list = new ArrayList<>();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        if (EmptyUtil.isEmpty(searchHits)) {
            map.put(SCROLL_SEARCH_RESULT, null);
            map.put(SCROLL_ID, null);
            return map;
        }
        for (SearchHit hit : searchHits) {
            list.add(JSON.parseObject(hit.getSourceAsString(), clazz));
        }
        //结果集
        map.put(SCROLL_SEARCH_RESULT, list);
        //scrollId,查询后面页数据使用
        map.put(SCROLL_ID, searchResponse.getScrollId());
        return map;
    }

    /**
     * 如果结果集为空将scrollId清除
     * @param map
     */
    private void clearScrollId(Map<String,Object> map){
        if (EmptyUtil.isEmpty(map)){
            return;
        }
        if (EmptyUtil.isNotEmpty(map.getOrDefault(SCROLL_SEARCH_RESULT,null))){
            return;
        }
        if (EmptyUtil.isEmpty(map.getOrDefault(SCROLL_ID,null))){
            return;
        }
        try {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId((String) map.get(SCROLL_ID));
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean succeeded = clearScrollResponse.isSucceeded();
            log.info("succeeded:{}",succeeded);
        }catch (Exception e){
            //出错不能影响查询结果，捕获异常即可
            log.error("清除scrollId出错");
        }
    }

    /**
     * 使用searchAfter的方式查询
     * @param queryBuilder
     * @param clazz
     * @param time
     * @return
     * @throws BizException
     */
    @Override
    public List<T> searchAfter(SearchSourceBuilder queryBuilder, Class<T> clazz, long time) throws BizException {
        if (EmptyUtil.isEmpty(queryBuilder)) {
            return Collections.emptyList();
        }
        return null;
    }
}
