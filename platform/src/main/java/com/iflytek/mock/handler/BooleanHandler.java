package com.iflytek.mock.handler;

import com.iflytek.mock.Options;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author
 */
public class BooleanHandler implements TypeHandler {


    @Override
    public Class[] support() {
        return new Class[]{Boolean.class};
    }

    @Override
    public Object handle(Options options) {
        return RandomUtils.nextInt(0, 2) == 1;
    }

}
