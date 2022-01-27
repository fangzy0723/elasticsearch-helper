package com.example.elasticsearchhepler.entity;



import com.example.elasticsearchhepler.annotations.ESField;
import com.example.elasticsearchhepler.annotations.ESID;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseEsEntity implements Serializable {
    private static final long serialVersionUID = 3739635447774514531L;
    /**
     * 业务数据主键
     */
    @ESID
    @ESField(name = "id")
    private String id;
}