package com.bbsuper.freemarker;

import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

/**
 * 扩展FreeMarkerViewResolver
 * @author yinyuqiao
 * 2017年6月22日 下午2:05:28
 */
public class FreeMarkerViewResolverExt extends FreeMarkerViewResolver {
	public String getPrefix() {
		return "/WEB-INF/views/default/";
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Class requiredViewClass() {
		return FreeMarkerViewExt.class;
	}
}
