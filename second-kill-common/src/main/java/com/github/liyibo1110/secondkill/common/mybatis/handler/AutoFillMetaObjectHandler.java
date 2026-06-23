package com.github.liyibo1110.secondkill.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.github.liyibo1110.secondkill.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis Plus自动填充处理器，作用是给BaseEntity里面的createTime / updateTime / createBy / updateBy / isDeleted字段进行自动填充。
 * @author liyibo
 * @date 2026-06-22 15:14
 */
@Slf4j
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
        strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    /**
     * 用户名就是用户ID，如果没有就是写死的system。
     */
    private String getCurrentUser() {
        Long userId = UserContext.getUserId();
        return userId != null ? String.valueOf(userId) : "system";
    }
}
