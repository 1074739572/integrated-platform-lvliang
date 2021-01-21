package com.iflytek.mock.handler;

import com.iflytek.mock.Options;

/**
 * @author
 */
public class StringHandler implements TypeHandler {

    @Override
    public Class[] support() {
        return new Class[]{String.class};
    }

    @Override
    public Object handle(Options options) {
        String sr = "";

        if (options.getRule().getCount() == null) {
            sr += options.getTemplate();
        } else {
            for (int i = 0; i < options.getRule().getCount(); i++) {
                sr += options.getTemplate();
            }
        }

        options.setTemplate(sr);
        //调取自定义方法
        return PlaceholderHandler.doGenerate(options);
    }
}
