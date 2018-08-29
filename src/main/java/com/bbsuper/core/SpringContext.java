package com.bbsuper.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Spring上下文
 * @author yinyuqiao
 * 2017年6月22日 下午2:07:17
 */
@Component
@Lazy(value = false)
public class SpringContext implements ApplicationContextAware {
	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	/**
	 * 根据类型获取对象
	 * @param clazz
	 * @return Object
	 */
	public static Object getBean(Class<?> clazz) {
		try {
			return context.getBean(clazz);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 根据名称获取对象
	 * @param name
	 * @return Object
	 */
	public static Object getBean(String name) {
		try {
			return context.getBean(name);
		} catch (Exception e) {
			return null;
		}
	}

}
