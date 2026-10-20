package com.cloudtea.config;

import com.cloudtea.properties.AliOssProperties;
import com.cloudtea.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*配置类，用于创建AliOssUtil对象*/
@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties ossProperties) {
        log.info("开始创建阿里云文件上传工具类对象：{}", ossProperties);
        return new AliOssUtil(ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                ossProperties.getBucketName());
    }
}
