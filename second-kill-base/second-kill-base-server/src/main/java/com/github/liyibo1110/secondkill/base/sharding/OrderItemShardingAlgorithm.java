package com.github.liyibo1110.secondkill.base.sharding;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * sk_order_item表标准分片算法，从order_no末位取分片标识进行路由
 * @author liyibo
 * @date 2026-06-23 14:49
 */
@Slf4j
public class OrderItemShardingAlgorithm implements StandardShardingAlgorithm<String> {

    private static final int SHARDING_COUNT = 4;

    @Override
    public void init(Properties props) {
        //  do nothing
    }

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        // 根据订单编号，拼接出最终的物理表名
        String orderNo = shardingValue.getValue();
        int index = getShardingIndex(orderNo);
        return shardingValue.getLogicTableName() + "_" + index;
    }

    /**
     * 这个是对应WHERE里面的范围查询，例如WHERE order_no BETWEEN ? AND ?
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
        // 范围查询将广播到所有的分片
        return new ArrayList<>(availableTargetNames);
    }

    /**
     * 根据订单编号，生成分片下标。
     */
    private int getShardingIndex(String orderNo) {
        char lastChar = orderNo.charAt(orderNo.length() - 1);
        return Character.getNumericValue(lastChar) % SHARDING_COUNT;
    }
}
