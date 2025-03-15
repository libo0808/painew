package com.pansophicmind.server.aidog.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.pansophicmind.server.aidog.base.Base;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 创建时间
    private final static String CREATE_TIME = "createTime";
    // 更新时间
    private final static String UPDATE_TIME = "updateTime";
    // 创建人信息
    private final static String CREATOR_ID = "creatorId";
    private final static String CREATOR_NAME = "creatorName";
    // 最后操作人信息
    private final static String LAST_OPERATOR_ID = "lastOperatorId";
    private final static String LAST_OPERATOR_NAME = "lastOperatorName";

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Object object = metaObject.getOriginalObject();
        if (object instanceof Base) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            this.setFieldValByName(CREATE_TIME, timestamp, metaObject);
            this.setFieldValByName(UPDATE_TIME, timestamp, metaObject);
        }
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Object object = metaObject.getOriginalObject();
        if (object instanceof Base) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            this.setFieldValByName(UPDATE_TIME, timestamp, metaObject);
        }
    }

}
