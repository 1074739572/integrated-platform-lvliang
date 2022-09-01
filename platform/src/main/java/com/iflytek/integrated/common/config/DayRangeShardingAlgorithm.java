package com.iflytek.integrated.common.config;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 * 天范围分片的标准策略
 */
public class DayRangeShardingAlgorithm implements RangeShardingAlgorithm<Date> {
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Date> shardingValue) {
        Collection<String> result = new LinkedHashSet<>();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取分片键值
        Range<Date> valueRange = shardingValue.getValueRange();

        //获取日期范围值
        Date start = valueRange.lowerEndpoint();

        Date end = valueRange.upperEndpoint();

        //如果起止日期有一个没有  则返回所有分片表
        if (start == null || start == null) {
            return availableTargetNames;
        }

        Calendar cal = Calendar.getInstance();
        while (start.getTime() <= end.getTime()) {
            String formatBeg = sdf.format(start);
            for (String availableTargetName : availableTargetNames) {
                if (availableTargetName.endsWith(formatBeg.substring(8,10))) {
                    result.add(availableTargetName);
                    break;
                }
            }
            cal.setTime(start);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            start = cal.getTime();
        }


        return result;
    }
}
