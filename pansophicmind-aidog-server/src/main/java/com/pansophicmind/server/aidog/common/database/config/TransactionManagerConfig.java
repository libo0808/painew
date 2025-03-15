package com.pansophicmind.server.aidog.common.database.config;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.pansophicmind.server.aidog.common.database.handler.MyMetaObjectHandler;
import com.pansophicmind.server.database.PansophicmindDataSourceCreator;
import com.pansophicmind.server.database.session.CustomSqlSessionTemplate;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TransactionManagerConfig {

    @Autowired
    private PansophicmindDataSourceCreator pansophicmindDataSourceCreator;

    /*=======================以下配置DataSource和SqlSessionFactory=======================*/

    public static final String MASTER_DATASOURCE = "master";

    @Bean(name = MASTER_DATASOURCE)
    public DataSource masterDB(DynamicDataSourceProperties properties) {
        Map<String, DataSourceProperty> datasource = properties.getDatasource();
        DataSourceProperty dataSourceProperty = datasource.get(MASTER_DATASOURCE);
        return pansophicmindDataSourceCreator.createDruidDataSource(dataSourceProperty);
    }

    @Primary
    @Bean(name = "masterSqlSessionFactory")
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier(MASTER_DATASOURCE) DataSource dataSource) throws Exception {
        return buildSqlSessionFactory(dataSource);
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier(MASTER_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionTemplate")
    public CustomSqlSessionTemplate sqlSessionTemplate(@Qualifier("masterSqlSessionFactory") SqlSessionFactory masterSqlSessionFactory) {
        Map<String, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();
        sqlSessionFactoryMap.put(MASTER_DATASOURCE, masterSqlSessionFactory);
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(masterSqlSessionFactory); // 实例化时默认数据源
        customSqlSessionTemplate.setTargetSqlSessionFactorys(sqlSessionFactoryMap);
        return customSqlSessionTemplate;
    }

    // 设置mybatisPlus 配置
    @Autowired
    @Qualifier("mybatisPlusInterceptor")
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Autowired
    private MyMetaObjectHandler myMetaObjectHandler;

    private SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setVfs(SpringBootVFS.class);
        // 包扫描
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml"));
        // 插件
        bean.setPlugins(mybatisPlusInterceptor);
        // 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(myMetaObjectHandler); // 自动填充
        globalConfig.setDbConfig(new GlobalConfig.DbConfig() // 参数策略
                .setSelectStrategy(FieldStrategy.NOT_EMPTY)
                .setInsertStrategy(FieldStrategy.NOT_NULL)
                .setUpdateStrategy(FieldStrategy.NOT_NULL)
        );
        bean.setGlobalConfig(globalConfig);
        // 自定义枚举包
        bean.setTypeEnumsPackage("com.pansophicmind.server.aidog.*.enums");
        // 配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setDefaultEnumTypeHandler(MybatisEnumTypeHandler.class); // 枚举类型处理
        configuration.setLogImpl(NoLoggingImpl.class); // 不打印SQL日志
        configuration.setCallSettersOnNulls(true); // 设置查询时保留null
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

}
