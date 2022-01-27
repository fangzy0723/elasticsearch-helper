package com.example.elasticsearchhepler.service;

/**
 * ES数据库的新建、删除等基本操作
 **/
public interface ElasticsearchIndexService<T> {
    /**
     * 创建索引
     * @param clazz
     * @throws Exception
     */
    public Boolean createIndex(Class<T> clazz) throws Exception;
    /**
     * 删除索引
     * @param clazz
     * @throws Exception
     */
    public boolean dropIndex(Class<T> clazz) throws Exception;
    /**
     * 索引是否存在
     * @param clazz
     * @throws Exception
     */
    public boolean exists(Class<T> clazz) throws Exception;

}
