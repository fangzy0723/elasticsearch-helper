package com.example.elasticsearchhepler.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.elasticsearchhepler.annotations.ESDocument;
import com.example.elasticsearchhepler.annotations.ESField;
import com.example.elasticsearchhepler.enums.ESFieldType;
import com.example.elasticsearchhepler.service.ElasticsearchIndexService;
import com.example.elasticsearchhepler.utils.ElasticSearchHelpUtils;
import com.example.elasticsearchhepler.utils.EmptyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * 索引数据库的创建、删除、存在
 **/
@Service
@Slf4j
public class ElasticsearchIndexServiceImpl<T> implements ElasticsearchIndexService<T> {

    @Resource
    private RestHighLevelClient client;

    @Resource
    private ElasticSearchHelpUtils<T> elasticSearchHelpUtils;

    /**
     * 判断索引是否存在
     * @param clazz
     * @return
     * @throws Exception
     */
    @Override
    public boolean exists(Class<T> clazz) throws Exception{
        GetIndexRequest request = new GetIndexRequest(elasticSearchHelpUtils.getIndexName(clazz));
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }
    //////////////////////////以上接口确认///////////////////////////////////










    @Override
    public Boolean createIndex(Class<T> clazz) throws Exception{
        ESDocument document = clazz.getAnnotation(ESDocument.class);
        if(document == null){
            throw new RuntimeException(clazz.getName() + " 无ESDocument注解");
        }
        String indexName = document.indexName();
        String indexType = document.type();
        Short numberOfShards = document.shards();//分片
        Short numberOfReplicas = document.replicas();//副本数目
        String refreshInterval = document.refreshInterval();//刷新间隔
        Boolean createIndex = document.createIndex();//是否创建索引
        String indexStoreType = document.indexStoreType();//索引文件存储类型
        Boolean useServerConfiguration = document.useServerConfiguration();//使用服务器配置

        if(!createIndex){
            return false;
        }
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        //settings
        Settings.Builder builder = Settings.builder();
        builder.put("index.number_of_shards", numberOfShards);//分片
        builder.put("index.number_of_replicas", numberOfReplicas);//副本
        builder.put("index.refresh_interval", refreshInterval);//刷新间隔

        //定义过滤器和分词器 （也可以不定义，使用默认的分词器）
        //builder.put("analysis.filter.autocomplete_filter.type","edge_ngram");//自动补全过滤器autocomplete_filter
        //builder.put("analysis.filter.autocomplete_filter.min_gram",1);//最少一个字符
        //builder.put("analysis.filter.autocomplete_filter.max_gram",20);//最大20个字符
        //builder.put("analysis.analyzer.autocomplete.type","custom");//自定义分词器 autocomplete
        //builder.put("analysis.analyzer.autocomplete.tokenizer","standard");//使用标准分词器 standard
        //builder.putList("analysis.analyzer.autocomplete.filter",new String[]{"lowercase","autocomplete_filter"});//标准分词后 过滤器再分词

        request.settings(builder);//设置settings

        //组装properties
        JSONObject propertiesJson = new JSONObject();
        Field [] fields = FieldUtils.getAllFields(clazz);
        for (Field field : fields) {
            if(field == null || field.getName().equals("serialVersionUID")){
                continue;
            }
            field.setAccessible(true);
            JSONObject fieldJson = new JSONObject();
            ESField esField = field.getAnnotation(ESField.class);
            if(esField != null){
                fieldJson.put("type", esField.type().toString().toLowerCase());
                if(!esField.index()){
                    fieldJson.put("index", esField.index());
                }
                if(EmptyUtil.isNotEmpty(esField.analyzer())){
                    fieldJson.put("analyzer", esField.analyzer());
                }
                if(EmptyUtil.isNotEmpty(esField.searchAnalyzer())){
                    fieldJson.put("search_analyzer", esField.searchAnalyzer());
                }
            }else{
                fieldJson.put("type", ESFieldType.keyword.toString().toLowerCase());
            }
            propertiesJson.put(field.getName(), fieldJson);
        }
        //拼接mapping信息
        JSONObject indexTypeJson = new JSONObject();
        indexTypeJson.put("properties", propertiesJson);
        JSONObject mappingJson = new JSONObject();
        mappingJson.put(indexType, indexTypeJson);
        //设置 mapping
        request.mapping(indexType, mappingJson.toJSONString(), XContentType.JSON);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        //返回的CreateIndexResponse允许检索有关执行的操作的信息，如下所示：是否所有节点都已确认请求
        boolean acknowledged = createIndexResponse.isAcknowledged();
        log.info("============索引库indexName：{}创建结果acknowledged：{}", indexName, acknowledged);
        return acknowledged;
    }

    @Override
    public boolean dropIndex(Class<T> clazz) throws Exception {
        DeleteIndexRequest request = new DeleteIndexRequest(elasticSearchHelpUtils.getIndexName(clazz));
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }


}
