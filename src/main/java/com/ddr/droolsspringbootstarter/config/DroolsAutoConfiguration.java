package com.ddr.droolsspringbootstarter.config;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@Slf4j
@Configuration
@ConditionalOnClass(KieContainer.class)
@EnableConfigurationProperties(DroolsProperties.class)
public class DroolsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(KieFileSystem.class)
    public KieFileSystem kieFileSystem(DroolsProperties properties) {
        KieFileSystem kieFileSystem = KieServices.Factory.get().newKieFileSystem();
        for (Resource ruleFile : getRuleFiles(properties)) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(properties.getRulePath() + ruleFile.getFilename(), "UTF-8"));
        }
        return kieFileSystem;
    }

    @Bean
    @ConditionalOnMissingBean(KieContainer.class)
    public KieContainer kieContainer(DroolsProperties properties) {
        KieRepository kieRepository = KieServices.Factory.get().getRepository();
        kieRepository.addKieModule(kieRepository::getDefaultReleaseId);
        KieBuilder kieBuilder = KieServices.Factory.get().newKieBuilder(kieFileSystem(properties));
        kieBuilder.buildAll();
        return KieServices.Factory.get().newKieContainer(kieRepository.getDefaultReleaseId());
    }

    @Bean
    @ConditionalOnMissingBean(KieBase.class)
    public KieBase kieBase(DroolsProperties properties) {
        return kieContainer(properties).getKieBase();
    }

    @Bean(destroyMethod = "dispose")
    @ConditionalOnMissingBean(KieSession.class)
    public KieSession kieSession(DroolsProperties properties) {
        return kieContainer(properties).newKieSession();
    }

    @Bean
    @ConditionalOnMissingBean(KModuleBeanFactoryPostProcessor.class)
    public KModuleBeanFactoryPostProcessor kModuleBeanFactoryPostProcessor() {
        return new KModuleBeanFactoryPostProcessor();
    }

    /**
     * 获取规则文件
     *
     * @return
     */
    private Resource[] getRuleFiles(DroolsProperties properties) {
        Resource[] resources = new Resource[0];
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            resources = resourcePatternResolver.getResources("classpath*:" + properties.getRulePath() + "**/*.*");
        } catch (IOException e) {
            log.error("加载规则文件失败", e);
        }
        return resources;
    }
}