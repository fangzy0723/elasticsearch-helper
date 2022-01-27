package com.example.elasticsearchhepler.utils;

import com.example.elasticsearchhepler.annotations.ESDocument;
import com.example.elasticsearchhepler.annotations.ESField;
import com.example.elasticsearchhepler.entity.BulkResponseResult;
import com.example.elasticsearchhepler.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Component
@Slf4j
public class ElasticSearchHelpUtils<T> {

    public ESDocument getESDocumentByEntity(T t) throws BizException{
        ESDocument document = t.getClass().getAnnotation(ESDocument.class);
        if (EmptyUtil.isEmpty(document)) {
            throw new BizException(0,t.getClass().getSimpleName() + "无ESDocument注解");
        }
        return document;
    }

    public ESDocument getESDocumentByClass(Class<T> clazz) throws BizException{
        ESDocument document = clazz.getAnnotation(ESDocument.class);
        if (EmptyUtil.isEmpty(document)) {
            throw new BizException(0,clazz.getSimpleName() + "无ESDocument注解");
        }
        return document;
    }

    /**
     * 从类的注解中获取索引名和别名，优先使用别名
     * @param t
     * @return
     * @throws BizException
     */
    public String getIndexName(T t) throws BizException{
        ESDocument document = getESDocumentByEntity(t);
        String indexName = document.indexName();
        String indexAliasesName = document.indexAliasesName();
        if (EmptyUtil.isEmpty(indexName)&&EmptyUtil.isEmpty(indexAliasesName)){
            throw new BizException(0,t.getClass().getSimpleName()+"类 索引名和索引别名不能同时为空");
        }
        return EmptyUtil.isNotEmpty(indexAliasesName)?indexAliasesName:indexName;
    }

    /**
     * 从类的注解中获取索引名和别名，优先使用别名
     * @param clazz
     * @return
     * @throws BizException
     */
    public String getIndexName(Class<T> clazz) throws BizException{
        ESDocument document = getESDocumentByClass(clazz);
        String indexName = document.indexName();
        String indexAliasesName = document.indexAliasesName();
        if (EmptyUtil.isEmpty(indexName)&&EmptyUtil.isEmpty(indexAliasesName)){
            throw new BizException(0,clazz.getSimpleName()+"类 索引名和索引别名不能同时为空");
        }
        return EmptyUtil.isNotEmpty(indexAliasesName)?indexAliasesName:indexName;
    }

    /**
     * 文档批量执行结果实体转换
     * @param bulkResponse
     * @return
     */
    public List<BulkResponseResult> convertBulkResponseResult(BulkResponse bulkResponse){
        if (EmptyUtil.isEmpty(bulkResponse)){
            return Collections.emptyList();
        }
        List<BulkResponseResult> resultList = new ArrayList<>();
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            BulkResponseResult bulkResponseResult = new BulkResponseResult();
            bulkResponseResult.setIndexNum(bulkItemResponse.getItemId());
            bulkResponseResult.setOpType(bulkItemResponse.getOpType());
            bulkResponseResult.setIndexName(bulkItemResponse.getResponse().getIndex());
            bulkResponseResult.setId(bulkItemResponse.getResponse().getId());
            bulkResponseResult.setResult(bulkItemResponse.getResponse().getResult());
            bulkResponseResult.setFailedFlag(bulkItemResponse.isFailed());
            bulkResponseResult.setFailureMessage(bulkItemResponse.getFailureMessage());
            resultList.add(bulkResponseResult);
        }
        return resultList;
    }

    /**
     * 实体转map
     * @return
     */
    public Map<String,Object> objectToMap(T t){

        Map<String,Object> map = new HashMap<>();

        if (EmptyUtil.isEmpty(t)){
            return map;
        }
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field:fields){
            field.setAccessible(true);
            ESField esField = field.getAnnotation(ESField.class);
            try {
                ////实体属性注解中没有指定mapping中的字段名则使用实体中的属性名
                String fieldModifier = Modifier.toString(field.getModifiers());
                if (fieldModifier.contains("static") || fieldModifier.contains("final")){
                    continue;
                }
                if (EmptyUtil.isEmpty(esField)){
                    map.put(field.getName(),field.get(t));
                }else{
                    map.put(esField.name(),field.get(t));
                }
            } catch (IllegalAccessException e) {
                log.info("实体转map 获取类名：{}中字段：{}的属性值出错,出错原因",t.getClass().getSimpleName(),field.getName(),e);
            }
        }
        Field[] superFields = t.getClass().getSuperclass().getDeclaredFields();
        for (Field field:superFields){
            field.setAccessible(true);
            ESField esField = field.getAnnotation(ESField.class);
            String fieldModifier = Modifier.toString(field.getModifiers());
            if (fieldModifier.contains("static") || fieldModifier.contains("final")){
                continue;
            }
            try {
                ////实体属性注解中没有指定mapping中的字段名则使用实体中的属性名
                if (EmptyUtil.isEmpty(esField)){
                    map.put(field.getName(),field.get(t));
                }else{
                    map.put(esField.name(),field.get(t));
                }
            } catch (IllegalAccessException e) {
                log.info("实体转map 获取类名：{}中字段：{}的属性值出错,出错原因",t.getClass().getSuperclass().getSimpleName(),field.getName(),e);
            }
        }
        return map;
    }

    /**
     * 实体转map
     * @return
     */
    public T mapToObject(){

        return null;
    }

    /**
     * 根据总条数和每页条数 计算总页数
     * @param totalSize 总条数
     * @param pageSize 每页条数
     * @return 总页数
     */
    public int getTotalPages(long totalSize, int pageSize) {
        return totalSize == 0L ? 0 : (Integer.parseInt((totalSize % Long.parseLong(pageSize + "") == 0 ? totalSize / Long.parseLong(pageSize + "") : totalSize / Long.parseLong(pageSize + "") + 1L) + ""));
    }
}
