package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Bean;            // 추가
import com.ll.framework.ioc.annotations.Component;
import com.ll.framework.ioc.annotations.Configuration;   // 추가
import com.ll.standard.util.Ut;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;                         // 추가
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Object> beans = new HashMap<>();
    private final Map<String, Class<?>> beanClasses = new HashMap<>();
    private final Map<String, Method> beanMethods = new HashMap<>();   // 추가

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections reflections = new Reflections(basePackage);

        for (Class<?> cls : reflections.getTypesAnnotatedWith(Component.class)) {
            if (cls.isInterface() || cls.isAnnotation()) continue;
            beanClasses.put(Ut.str.lcfirst(cls.getSimpleName()), cls);
        }

        // 추가: @Configuration 클래스의 @Bean 메서드 등록
        for (Class<?> configClass : reflections.getTypesAnnotatedWith(Configuration.class)) {
            for (Method method : configClass.getMethods()) {
                if (method.isAnnotationPresent(Bean.class)) {
                    beanMethods.put(method.getName(), method);
                }
            }
        }
    }

    public <T> T genBean(String beanName) {
        if (beans.containsKey(beanName)) {
            return (T) beans.get(beanName);
        }

        try {
            Object bean;

            if (beanMethods.containsKey(beanName)) {                  // 갈래 A: @Bean 메서드
                Method method = beanMethods.get(beanName);
                Object configInstance = genBean(Ut.str.lcfirst(method.getDeclaringClass().getSimpleName()));

                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    args[i] = genBean(parameters[i].getName());       // 타입 아닌 "이름"으로 조달
                }

                bean = method.invoke(configInstance, args);           // 인자 넣어 메서드 실행
            } else {                                                  // 갈래 B: @Component 클래스 (2강)
                Class<?> cls = beanClasses.get(beanName);
                if (cls == null) return null;
                Constructor<?> constructor = cls.getConstructors()[0];
                Parameter[] parameters = constructor.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    args[i] = genBean(Ut.str.lcfirst(parameters[i].getType().getSimpleName()));
                }
                bean = constructor.newInstance(args);
            }

            beans.put(beanName, bean);                                // 공통: 저장 + 반환
            return (T) bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}