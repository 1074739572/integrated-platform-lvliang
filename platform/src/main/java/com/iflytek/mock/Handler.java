package com.iflytek.mock;

import com.iflytek.mock.handler.TypeHandlerFactory;

/**
 * @author
 */
public class Handler {

    /**
     * @param template 属性值（即数据模板）
     * @param name 属性名
     * @param context 数据上下文，生成后的数据
     * @return
     */
    public static Object gen(Object template, String name, Context context) {
        name = name == null ? "" : name;
        context = context == null ? new Context() : context;
        Rule rule = Parser.parseRule(name);
        Class type =  template != null?Parser.parseType(template):null;
        Options options = new Options(type, template, name, parseName(name), rule, context);
        return TypeHandlerFactory.getTypeHandler(type).handle(options);
    }

    private static String parseName(String name) {
        int index = name.indexOf("|");
        return index < 0 ? name : name.substring(0, index);
    }
}
