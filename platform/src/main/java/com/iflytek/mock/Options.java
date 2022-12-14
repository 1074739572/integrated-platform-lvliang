package com.iflytek.mock;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author
 */
@AllArgsConstructor
@Data
public class Options {
    /**
     * 属性值类型
     */
    Class type;
    /**
     * 属性值模板
     */
    Object template;
    /**
     * 属性名 + 生成规则
     */
    String name;
    /**
     * 属性名
     */
    String parsedName;
    /**
     * 解析后的生成规则
     */
    Rule rule;
    /**
     * 相关上下文
     */
    Context context;
}
