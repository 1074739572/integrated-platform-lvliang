package com.iflytek.mock.handler;

import com.iflytek.mock.Options;

/**
 * @author
 */
public interface TypeHandler {
    Class[] support();

    Object handle(Options options);
}
