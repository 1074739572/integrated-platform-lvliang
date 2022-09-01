package com.iflytek.integrated.common.config;

import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class DayPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Date> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
        Date value = shardingValue.getValue();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(value);
        if(StringUtils.isEmpty(format)){
            return new ArrayList<String>(availableTargetNames).get(0);
        }

        return shardingValue.getLogicTableName().concat("_").concat(format.substring(8,10));
    }
}
