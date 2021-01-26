package com.iflytek.mock.handler;

import com.iflytek.mock.Options;
import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;

/**
 * @author
 */
public class NumberHandler implements TypeHandler {


    @Override
    public Class[] support() {
        return new Class[]{Integer.class, BigDecimal.class};
    }

    @Override
    public Object handle(Options options) {
        if (options.getRule().isDecimal()) {
            String[] parts = options.getTemplate().toString().split("\\.");
            // 'float1|.1-10': 10,
            // 'float2|1-100.1-10': 1,
            // 'float3|999.1-10': 1,
            // 'float4|.3-10': 123.123,
            parts[0] = options.getRule().isRange() ? options.getRule().getCount().toString() : parts[0];
            if (parts.length == 1) {
                parts = new String[]{parts[0], ""};
            }

            // 如果预设值的小数位长度足够
            if (parts[1].length() >= options.getRule().getDcount()) {
                parts[1] = parts[1].substring(0, options.getRule().getDcount());
            } else {
                while (parts[1].length() < options.getRule().getDcount()) {
                    parts[1] += RandomUtils.nextInt(1, 10);
                }
            }

            return new BigDecimal(String.join(".", parts));
        }


        // 'grade1|1-100': 1,
        if (options.getRule().isRange()) {
            return options.getRule().getCount();
        }

        return options.getTemplate();
    }


}
