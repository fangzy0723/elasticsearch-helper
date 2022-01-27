package com.example.elasticsearchhepler.entity;

import com.example.elasticsearchhepler.annotations.ESDocument;
import com.example.elasticsearchhepler.annotations.ESField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ESDocument(indexName = "test_index_auto")
@Component
public class TestIndexAutoEntity extends BaseEsEntity implements Serializable {

    private static final long serialVersionUID = -7298596379026894107L;
    @ESField(name = "user_name")
    private String userName;

    @ESField(name = "user_title")
    private String userTitle;

    @ESField(name = "user_score")
    private double userScore;

    @ESField(name = "weight")
    private float weight;

    @ESField(name = "study_flag")
    private Boolean studyFlag;

    @ESField(name = "user_age")
    private Integer userAge;

    @ESField(name = "run_distance")
    private Long runDistance;

    @ESField(name = "birth_date")
    private LocalDateTime birthDate;

}
