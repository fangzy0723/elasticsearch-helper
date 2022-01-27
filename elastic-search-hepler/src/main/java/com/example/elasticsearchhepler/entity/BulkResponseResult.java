package com.example.elasticsearchhepler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;

import java.io.Serializable;

/**
 * 批量操作返回结果实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkResponseResult implements Serializable {

    private static final long serialVersionUID = 547402260621429004L;
    //数据在合集中的下标位置
    private Integer indexNum;

    //操作类型
    private DocWriteRequest.OpType opType;

    //索引名
    private String indexName;

    //数据id（有值可认为操作成功）
    private String id;

    //返回结果
    private DocWriteResponse.Result result;

    //是否失败 false:没有失败  true：操作失败
    private boolean failedFlag;

    //失败原因
    private String failureMessage;


}
