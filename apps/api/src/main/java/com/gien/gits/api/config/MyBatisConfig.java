package com.gien.gits.api.config;

import com.gien.gits.adapter.persistence.common.handler.ProductSchemeListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.StringListJsonTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.UuidListJsonTypeHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis 配置 — 扫描 foundation 层 Mapper 接口 + business 层通配。
 *
 * <p>仅在 {@code gits.persistence.mode=mybatis} 时激活。</p>
 *
 * <p>包结构遵循 {@code mybatis-integration-spec.md} 两层架构：</p>
 * <ul>
 *   <li>foundation/{module}/mapper — 领域基建 CRUD</li>
 *   <li>business/*.{subsystem}/mapper — 跨实体业务查询（按需创建）</li>
 * </ul>
 *
 * <p>TypeHandler 注册策略：</p>
 * <ul>
 *   <li>{@code common.typehandler} 包：InstantTypeHandler、UuidTypeHandler — 通过 type-handlers-package 自动扫描注册</li>
 *   <li>{@code common.handler} 包：StringListJsonTypeHandler、UuidListJsonTypeHandler、
 *       JsonTypeHandler、EnumTypeHandler — 通过 ConfigurationCustomizer 手动注册，
 *       避免自动扫描时多个 List 类型处理器冲突或泛型类无无参构造函数的问题</li>
 * </ul>
 */
@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "mybatis")
@MapperScan({
    "com.gien.gits.adapter.persistence.foundation.ontology.mapper",
    "com.gien.gits.adapter.persistence.foundation.action.mapper",
    "com.gien.gits.adapter.persistence.foundation.engagement.mapper",
    "com.gien.gits.adapter.persistence.foundation.journey.mapper",
    "com.gien.gits.adapter.persistence.business.*.mapper"
})
public class MyBatisConfig {

    /**
     * 手动注册 handler 包中的 TypeHandler。
     *
     * <p>这些 TypeHandler 不能通过 type-handlers-package 自动扫描注册，原因：</p>
     * <ul>
     *   <li>StringListJsonTypeHandler / UuidListJsonTypeHandler 都继承 BaseTypeHandler&lt;List&lt;?&gt;&gt;，
     *       自动扫描时会冲突（后注册的覆盖先注册的）</li>
     *   <li>JsonTypeHandler / EnumTypeHandler 是泛型类，没有无参构造函数</li>
     * </ul>
     *
     * <p>ConfigurationCustomizer 在 MybatisAutoConfiguration 创建 SqlSessionFactory 时调用，
     *    在 XML 映射解析之前执行，确保 XML 中能正确引用这些 TypeHandler。</p>
     */
    @Bean
    ConfigurationCustomizer mybatisTypeHandlerCustomizer() {
        return configuration -> {
            var typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            typeHandlerRegistry.register(new StringListJsonTypeHandler());
            typeHandlerRegistry.register(new UuidListJsonTypeHandler());
            typeHandlerRegistry.register(new ProductSchemeListTypeHandler());
        };
    }
}
