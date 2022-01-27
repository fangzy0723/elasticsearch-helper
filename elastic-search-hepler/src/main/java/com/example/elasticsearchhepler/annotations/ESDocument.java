package com.example.elasticsearchhepler.annotations;

import java.lang.annotation.*;

/**
 * ES对象实体注解
 * 注解作用在类上，标记实体类为文档对象
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ESDocument {

    //索引库名称 必须配置  和indexAliasesName 不能同时为空
    String indexName();

    //索引别名 查询时刻使用别名查询  和indexName 不能同时为空
    String indexAliasesName() default "";





    //类型 可不配置 7.x开始弱化了这个概念 每个索引只有一个type是： _doc   待定是否删除 todo
    String type() default "_doc";

    //是否使用服务器配置  待定是否删除 todo
    boolean useServerConfiguration() default false;

    //默认分片数5  待定是否删除 todo
    short shards() default 5;

    //默认副本数1  待定是否删除 todo
    short replicas() default 1;

    //刷新间隔  待定是否删除 todo
    String refreshInterval() default "1s";

    //索引文件存储类型  待定是否删除 todo
    String indexStoreType() default "fs";

    //是否创建索引  待定是否删除 todo
    boolean createIndex() default true;
}
