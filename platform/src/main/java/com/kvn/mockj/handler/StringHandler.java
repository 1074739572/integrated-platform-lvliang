package com.kvn.mockj.handler;

import com.kvn.mockj.Options;

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
        String sR = "";

        if (options.getRule().getCount() == null) {
            sR += options.getTemplate();
        } else {
            for (int i = 0; i < options.getRule().getCount(); i++) {
                sR += options.getTemplate();
            }
        }

        options.setTemplate(sR);
        //调取自定义方法
        return PlaceholderHandler.doGenerate(options);
    }
}
