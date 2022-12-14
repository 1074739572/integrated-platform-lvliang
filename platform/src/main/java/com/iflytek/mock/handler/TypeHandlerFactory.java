package com.iflytek.mock.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 */
public class TypeHandlerFactory {
    public static Map<Class, TypeHandler> handlerMap = new HashMap<>();

    static {
        TypeHandler[] typeHandlers = new TypeHandler[]{
                new ObjectHandler(),
                new ArrayHandler(),
                new StringHandler(),
                new NumberHandler(),
                new BooleanHandler()
        };

        for (TypeHandler typeHandler : typeHandlers) {
            for (Class clazz : typeHandler.support()) {
                handlerMap.put(clazz, typeHandler);
            }
        }

    }


    public static TypeHandler getTypeHandler(Class clazz) {
        if(handlerMap.containsKey(clazz)){
            return handlerMap.get(clazz);
        }
        else {
            return handlerMap.get(String.class);
        }
    }

}
