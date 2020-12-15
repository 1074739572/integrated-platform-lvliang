package com.kvn.mockj.handler;

import com.kvn.mockj.Options;

/**
 * @author
 */
public interface TypeHandler {
    Class[] support();

    Object handle(Options options);
}
