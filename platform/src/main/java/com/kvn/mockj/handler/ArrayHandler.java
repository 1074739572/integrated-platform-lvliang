package com.kvn.mockj.handler;

import com.alibaba.fastjson.JSONArray;
import com.kvn.mockj.Context;
import com.kvn.mockj.Function;
import com.kvn.mockj.Handler;
import com.kvn.mockj.Options;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author
 */
public class ArrayHandler implements TypeHandler {

    @Override
    public Class[] support() {
        return new Class[]{JSONArray.class};
    }

    @Override
    public Object handle(Options options) {
        //如果是数组形式，随机返回多条
        if (options.getRule().getParameters() == null || options.getRule().getParameters().isEmpty()) {
            JSONArray jsonArray = new JSONArray();

            Integer length = Function.arraySize != null?Function.$integer(Function.arraySize):1;
            for (int size = 0; size < length;size ++){
                JSONArray templates = (JSONArray) options.getTemplate();
                for (int i = 0; i < templates.size(); i++) {
                    Context context = new Context();
                    jsonArray.add(Handler.gen(templates.get(i), i + "", context));
                }
            }
            return jsonArray;
        }


        // 'method|1': ['GET', 'POST', 'HEAD', 'DELETE']
        if (options.getRule().getMin() != null && options.getRule().getMin() == 1 && options.getRule().getMax() == null) {
            JSONArray ja = (JSONArray) Handler.gen(options.getTemplate(), null, options.getContext());
            return ja.get(RandomUtils.nextInt(0, ja.size()));
        }

        // 'data|+1': [{}, {}]
        if (StringUtils.isNotEmpty(options.getRule().getParameters().get(1))) {
            JSONArray ja = (JSONArray) Handler.gen(options.getTemplate(), null, options.getContext());
            int index = options.getContext().getOrderIndex() >= ja.size() ? 0 : options.getContext().getOrderIndex();
            options.getContext().setOrderIndex(index + 1);
            return ja.get(index);
        }


        // 'data|1-10': [{}]
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < options.getRule().getCount(); i++) {
            // 'data|1-10': [{}, {}]
            JSONArray templates = (JSONArray) options.getTemplate();
            JSONArray ja = new JSONArray();
            for (int j = 0; j < templates.size(); j++) {
                ja.add(Handler.gen(templates.get(j), ja.size() + "", options.getContext()));
            }
            jsonArray.add(ja);
        }

        return jsonArray;
    }
}
