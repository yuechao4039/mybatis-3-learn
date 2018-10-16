package com.sndj.financial;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = ShardingTable.ShardingTableList.class)
public @interface ShardingTable {

    // 是否分表
    public boolean isShard() default true;

    // 表名
    public String tableName() default "";

    // 字段名
    public String[] fieldNames() default "";

    // 获取分表策略
    public Class strategy() default ConcatenateStrategy.class;

    /**
     * Defines several {@code @ShardingTable} annotations on the same element.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({TYPE, METHOD})
    public @interface ShardingTableList {
        ShardingTable[] value();
    }


}


