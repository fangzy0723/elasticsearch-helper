package com.example.elasticsearchhepler.config;

import com.example.elasticsearchhepler.annotations.ESDocument;
import com.example.elasticsearchhepler.service.ElasticsearchIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 是否自动创建索引 待定 todo
 * 用于扫描ESDocument注解的类，并自动创建索引mapping
 * 启动时调用
 **/
@Slf4j
//@Component
public class ElasticsearchListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ElasticsearchIndexService elasticsearchIndexService;

    /**
     * 扫描ESMetaData注解的类，并自动创建索引mapping
     * @param applicationReadyEvent
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("222222222【onApplicationEvent 开始执行】");
        if(applicationReadyEvent.getApplicationContext().getParent() != null){//解決二次调用问题
            return;
        }
        Map<String, Object> beans = applicationReadyEvent.getApplicationContext().getBeansWithAnnotation(ESDocument.class);
        beans.forEach((beanName, bean)->{
            try {
                if(!elasticsearchIndexService.exists(bean.getClass())){
                    elasticsearchIndexService.createIndex(bean.getClass());
                }
            } catch (Exception e) {
                log.error("创建索引不成功",e);
            }
        });
    }
}
