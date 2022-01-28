package com.example.elasticsearchhepler.service;

import com.alibaba.fastjson.JSON;
import com.example.elasticsearchhepler.entity.TestIndexAutoEntity;
import com.example.elasticsearchhepler.exception.BizException;
import com.example.elasticsearchhepler.utils.EmptyUtil;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestServiceTest {


    @Resource
    private TestIndexAutoService testIndexAutoService;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    void sniffer() throws Exception {

        List<Node> nodes = restHighLevelClient.getLowLevelClient().getNodes();
        System.out.println(nodes);
        Thread.sleep(6000);

        nodes = restHighLevelClient.getLowLevelClient().getNodes();
        System.out.println(nodes);
    }

    @Test
    void save() throws BizException {

        TestIndexAutoEntity entity = new TestIndexAutoEntity();
        entity.setId("3");
        entity.setUserName("北京大学0");
        entity.setUserTitle("北京大学");
        entity.setUserScore(20.22);
        entity.setWeight(50.2F);
        entity.setStudyFlag(true);
        entity.setUserAge(20);
        entity.setRunDistance(1000L);
        entity.setBirthDate(LocalDateTime.now());
        System.out.println(testIndexAutoService.saveOrUpdateCover(entity));

    }

    @Test
    void batchInsert() throws BizException {

        TestIndexAutoEntity entity = new TestIndexAutoEntity();
        entity.setId("4");
        entity.setUserName("北京大学");
        entity.setUserTitle("北京大学");
        entity.setUserScore(20.22);
        entity.setWeight(50.2F);
        entity.setStudyFlag(true);
        entity.setUserAge(20);
        entity.setRunDistance(1000L);
        entity.setBirthDate(LocalDateTime.now());

        TestIndexAutoEntity entity4 = new TestIndexAutoEntity();
        entity4.setId("5");
        entity4.setUserName("北京人民大学");
        entity4.setUserTitle("北京人民大学");
        entity4.setUserScore(20.22);
        entity4.setWeight(50.2F);
        entity4.setStudyFlag(true);
        entity4.setUserAge(20);
        entity4.setRunDistance(1000L);
        entity4.setBirthDate(LocalDateTime.now());

        TestIndexAutoEntity entity5 = new TestIndexAutoEntity();
        entity5.setUserName("北京天安门");
        entity5.setUserTitle("北京天安门");
        entity5.setUserScore(20.22);
        entity5.setWeight(50.2F);
        entity5.setStudyFlag(true);
        entity5.setUserAge(20);
        entity5.setRunDistance(1000L);
        entity5.setBirthDate(LocalDateTime.now());

        TestIndexAutoEntity entity6 = new TestIndexAutoEntity();
        entity6.setUserName("北京人民公园");
        entity6.setUserTitle("北京人民公园");
        entity6.setUserScore(20.22);
        entity6.setWeight(50.2F);
        entity6.setStudyFlag(true);
        entity6.setUserAge(20);
        entity6.setRunDistance(1000L);
        entity6.setBirthDate(LocalDateTime.now());

        List<TestIndexAutoEntity> list = Arrays.asList(entity,entity4,entity5,entity6);

        System.out.println(testIndexAutoService.saveOrUpdateCover(list));

    }

    @Test
    void deleteById() throws BizException {
        System.out.println(testIndexAutoService.deleteById("3"));
    }

    @Test
    void deleteByIds() throws BizException {
        System.out.println(testIndexAutoService.deleteByIds(Arrays.asList("3","4")));
    }


    @Test
    void deleteByQuery() throws BizException {
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("user_name","张三5");
        System.out.println(testIndexAutoService.deleteByQuery(queryBuilder));
    }

    @Test
    void getById() throws BizException {
        System.out.println(testIndexAutoService.getById("cff1k34Brx3CyPdycAwb"));
    }

    @Test
    void mGetById() throws BizException {
        System.out.println(testIndexAutoService.mGetById(Arrays.asList("cff1k34Brx3CyPdycAwb","3")));
    }

    @Test
    void exists() throws BizException {
        System.out.println(testIndexAutoService.exists("cff1k34Brx3CyPdycAwb"));
    }

    @Test
    void updateById() throws BizException {

        TestIndexAutoEntity entity = new TestIndexAutoEntity();
        entity.setId("cff1k34Brx3CyPdycAwb");
        entity.setUserName("张三3");
        entity.setUserTitle("");
        entity.setWeight(53.2F);
        entity.setStudyFlag(true);
        entity.setUserAge(30);
        entity.setRunDistance(3000L);
        entity.setBirthDate(LocalDateTime.now());
        System.out.println(testIndexAutoService.updateById(entity));

    }

    @Test
    void updateByQuery() throws BizException {
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("user_name","张三3");
        Map<String,Object> map = new HashMap<>();
        map.put("user_age",31);
        map.put("user_title","这是title");
        System.out.println(testIndexAutoService.updateByQuery(queryBuilder,map));
    }

    @Test
    void searchOrigin() throws BizException {
        SearchSourceBuilder queryBuilder = new SearchSourceBuilder();
        QueryBuilder query = QueryBuilders.termQuery("user_name","张三3");
        queryBuilder.query(query);
        queryBuilder.size(10);
        queryBuilder.from(0);
        System.out.println(testIndexAutoService.searchOrigin(queryBuilder));
    }

    @Test
    void countTotal() throws BizException {
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("user_name","张三3");
        System.out.println(testIndexAutoService.countTotal(queryBuilder));
    }

    @Test
    void searchList() throws BizException {
        SearchSourceBuilder queryBuilder = new SearchSourceBuilder();
        QueryBuilder query = QueryBuilders.termQuery("user_name","张三3");
        queryBuilder.query(query);
        queryBuilder.size(10);
        queryBuilder.from(0);
        System.out.println(testIndexAutoService.searchList(queryBuilder));
    }

    @Test
    void searchPage() throws BizException {
        SearchSourceBuilder queryBuilder = new SearchSourceBuilder();
        QueryBuilder query = QueryBuilders.termQuery("user_name","张三3");
        queryBuilder.query(query);
        queryBuilder.size(10);
        queryBuilder.from(0);
        queryBuilder.sort("user_name", SortOrder.DESC);
        System.out.println(testIndexAutoService.searchPage(queryBuilder,1,10));
    }


    @Test
    void completionSuggest() throws BizException {
        System.out.println(testIndexAutoService.completionSuggest("user_title","北京"));
    }

    @Test
    void completionSearchAsYouType() throws BizException {
        System.out.println(testIndexAutoService.completionSearchAsYouType("user_title","北大"));
    }

    @Test
    void scroll() throws BizException {

        SearchSourceBuilder queryBuilder = new SearchSourceBuilder();
        QueryBuilder query = QueryBuilders.matchQuery("user_title","北京");
        queryBuilder.query(query);
        queryBuilder.size(1);
        queryBuilder.sort("user_name", SortOrder.DESC);
        Map<String, Object> map = testIndexAutoService.scroll(queryBuilder, "");
        System.out.println(JSON.toJSONString(map));
        while (EmptyUtil.isNotEmpty(map) && EmptyUtil.isNotEmpty(map.getOrDefault("result",""))){
            map = testIndexAutoService.scroll(null, (String) map.getOrDefault("scrollId",""));
            System.out.println(JSON.toJSONString(map));
        }
    }
}
