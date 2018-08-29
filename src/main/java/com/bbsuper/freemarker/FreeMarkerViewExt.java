package com.bbsuper.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import freemarker.template.SimpleHash;
/**
 * Freemarker模板
 * @author yinyuqiao
 * 2017年6月22日 下午2:05:36
 */
public class FreeMarkerViewExt extends FreeMarkerView {
	@Override
	protected SimpleHash buildTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
		model.put("ctx", request.getContextPath());
		model.put("base", request.getContextPath() + "/themes/default");
		return super.buildTemplateModel(model, request, response);
	}
}
