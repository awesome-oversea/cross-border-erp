package com.aidotnet.erp.common.persistence.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.time.Instant;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(() -> {
            String tenantId = TenantContext.getTenantId();
            return new StringValue(tenantId != null ? tenantId : "");
        }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", Instant.class, Instant.now());
                this.strictInsertFill(metaObject, "updatedAt", Instant.class, Instant.now());
                this.strictInsertFill(metaObject, "tenantId", String.class, TenantContext.getTenantId());
                this.strictInsertFill(metaObject, "createdBy", String.class, TenantContext.getTenantId());
                this.strictInsertFill(metaObject, "updatedBy", String.class, TenantContext.getTenantId());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", Instant.class, Instant.now());
                this.strictUpdateFill(metaObject, "updatedBy", String.class, TenantContext.getTenantId());
            }
        };
    }
}
