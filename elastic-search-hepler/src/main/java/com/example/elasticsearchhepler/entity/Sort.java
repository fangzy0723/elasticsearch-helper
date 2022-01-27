package com.example.elasticsearchhepler.entity;



import com.example.elasticsearchhepler.enums.SortEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sort implements Serializable {

    private static final long serialVersionUID = -3304075232976302096L;
    private String fieldName;
    private SortEnum order;
}
