package com.github.liyibo1110.secondkill.base.sharding;

import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * sk_order表复合分片算法，共有三层路由：
 * 1、优先userId。
 * 2、其次orderNo的末位。
 * 3、最后只能广播兜底。
 * @author liyibo
 * @date 2026-06-23 14:52
 */
public class OrderComplexShardingAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {

    private static final int SHARDING_COUNT = 4;

    @Override
    public void init(Properties props) {
        // do nothing
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<Comparable<?>> shardingValue) {
        String logicTableName = shardingValue.getLogicTableName();
        Map<String, Collection<Comparable<?>>> columnValues = shardingValue.getColumnNameAndShardingValuesMap();

        // 第一层：SQL中如果有user_id，则按userId % 4路由
        Collection<Comparable<?>> userIdValues = columnValues.get("user_id");
        if (userIdValues != null && !userIdValues.isEmpty()) {
            return userIdValues.stream()
                    .map(val -> ((Number)val).longValue())
                    .map(userId -> logicTableName + "_" + (int)(userId % SHARDING_COUNT))
                    .collect(Collectors.toSet());
        }

        // 第二层：SQL中如果有order_no，取末位当作分片标识
        Collection<Comparable<?>> orderNoValues = columnValues.get("order_no");
        if (orderNoValues != null && !orderNoValues.isEmpty()) {
            return orderNoValues.stream()
                    .map(val -> (String) val)
                    .map(orderNo -> logicTableName + "_" + getShardingIndex(orderNo))
                    .collect(Collectors.toSet());
        }

        // 第三层：兜底广播到所有分片
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
