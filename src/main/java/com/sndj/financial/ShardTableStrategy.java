package com.sndj.financial;

public interface ShardTableStrategy {

    public String convert(String originalSQL, Object parameterObject, ShardingTable shardingTable);

    String getNewMsId(String sourceMsID, ShardingTable shardingTable, Object parameterObject);
}
