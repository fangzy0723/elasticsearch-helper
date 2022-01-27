package com.example.elasticsearchhepler.annotations;

import com.example.elasticsearchhepler.enums.ESFieldType;

import java.lang.annotation.*;

/**
 * ES对象属性注解
 * 作用在成员变量，标记为文档的字段，并制定映射属性；
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface ESField {

    /**
     * mapping中的字段名,为空时使用实体属性名
     */
    String name();
    /**
     * 属性类型
     *
     * @return
     */
    ESFieldType type() default ESFieldType.keyword;

    /**
     * 是否索引
     *
     * @return
     */
    boolean index() default true;

    /**
     * 是否存储 默认不存储
     *
     * @return
     */
    boolean store() default false;

    /**
     * 指定字段使用搜索时的分词
     *
     * @return
     */
    String searchAnalyzer() default "";

    /**
     * 分词器名称（索引分词/搜索分词）
     *
     * @return
     */
    String analyzer() default "";
}

