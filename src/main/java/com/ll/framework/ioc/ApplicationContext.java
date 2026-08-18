package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.standard.util.Ut;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;   // 추가
import java.lang.reflect.Parameter;     // 추가
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Object> beans = new HashMap<>();
    private final Map<String, Class<?>> beanClasses = new HashMap<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        for (Class<?> cls : componentClasses) {
            if (cls.isInterface() || cls.isAnnotation()) continue;
            String beanName = Ut.str.lcfirst(cls.getSimpleName());
            beanClasses.put(beanName, cls);
        }
    }

    public <T> T genBean(String beanName) {
        if (beans.containsKey(beanName)) {
            return (T) beans.get(beanName);
        }
        Class<?> cls = beanClasses.get(beanName);
        if (cls == null) return null;

        try {
            Constructor<?> constructor = cls.getConstructors()[0];       // 생성자 얻기
            Parameter[] parameters = constructor.getParameters();        // 주문서 읽기

            Object[] args = new Object[parameters.length];               // 재료 담을 배열
            for (int i = 0; i < parameters.length; i++) {
                String dependencyName = Ut.str.lcfirst(parameters[i].getType().getSimpleName());
                args[i] = genBean(dependencyName);                       // 각 재료를 재귀 조달
            }

            Object bean = constructor.newInstance(args);                 // 재료 넣어 생성
            beans.put(beanName, bean);
            return (T) bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}