package com.iflytek.integrated.common.config;

import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.ArrayList;
import java.util.Collection;

public class DayPreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String value = shardingValue.getValue();
        if(StringUtils.isEmpty(value)){
            return new ArrayList<String>(availableTargetNames).get(0);
        }

        return shardingValue.getLogicTableName().concat("_").concat(value.substring(8,10));
    }
}
