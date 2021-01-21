package com.iflytek.mock;

import lombok.Data;

/**
 * @author
 */
@Data
public class Context {

    /**
     * 'data|+1': [{}, {}]
     */
    private int orderIndex;

    /**
     * "number|+1": 202
     * 此规则的初始自增值（即：202）
     */
    private Integer incInitValue;

}
