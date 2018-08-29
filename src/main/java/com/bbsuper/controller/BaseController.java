package com.bbsuper.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 基类
 * @author yinyuqiao
 * 2017年6月22日 下午2:08:29
 */
public class BaseController{
	public HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
	}

	public HttpSession getSession() {
		return ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest().getSession();
	}

	public ServletContext getServletContext() {
		return ContextLoader.getCurrentWebApplicationContext()
				.getServletContext();
	}

	public int getInt(String name) {
		return getInt(name, 0);
	}

	public int getInt(String name, int defaultValue) {
		String resultStr = getRequest().getParameter(name);
		if (resultStr != null) {
			try {
				return Integer.parseInt(resultStr);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	public BigDecimal getBigDecimal(String name) {
		return getBigDecimal(name, null);
	}

	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue) {
		String resultStr = getRequest().getParameter(name);
		if (resultStr != null) {
			try {
				return BigDecimal.valueOf(Double.parseDouble(resultStr));
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	public String getString(String name) {
		return getString(name, null);
	}

	public String getString(String name, String defaultValue) {
		String resultStr = getRequest().getParameter(name);
		if (resultStr == null || "".equals(resultStr)
				|| "null".equals(resultStr) || "undefined".equals(resultStr)) {
			return defaultValue;
		} else {
			return resultStr;
		}
	}

	public void outPrint(HttpServletResponse response, Object result) {
		try {
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			out.print(result.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 得到内置对象中的值
	 * 
	 * @param <T>
	 * @param key
	 * @param scop
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(String key, String scop) {
		HttpServletRequest req = this.getRequest();
		T t = null;
		if (null == scop) {
			t = (T) req.getAttribute(key);
			if (null == t)
				t = (T) req.getSession().getAttribute(key);
			if (null == t)
				t = (T) getServletContext().getAttribute(key);
		} else {

			if ("request".equals(scop)) {
				t = (T) req.getAttribute(key);
			} else if ("session".equals(scop)) {
				t = (T) req.getSession().getAttribute(key);
			} else if ("application".equals(scop)) {
				t = (T) getServletContext().getAttribute(key);
			}
		}
		return t;
	}

	/**
	 * 放入内置对象
	 * 
	 * @param key
	 * @param obj
	 * @param scop
	 */
	public void setBean(String key, Object obj, String scop) {
		if (null == key) {
			key = "request";
		}
		HttpServletRequest req = this.getRequest();
		if ("request".equals(scop)) {
			req.setAttribute(key, obj);
		} else if ("session".equals(scop)) {
			req.getSession().setAttribute(key, obj);
		} else if ("application".equals(scop)) {
			this.getServletContext().setAttribute(key, obj);
		}
	}

	/**
	 * 转换成json格式
	 * @return
	 */
	protected JsonConfig getDefaultJsonConfig(){
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			@Override
			public Object processObjectValue(String key, Object value, JsonConfig cfg) {
				if(value!=null && value instanceof Date){
					return sdf.format(value);
				}
				return null;
			}
			
			@Override
			public Object processArrayValue(Object value, JsonConfig cfg) {
				if(value!=null && value instanceof Date){
					return sdf.format(value);
				}
				return null;
			}
		});
		return config;
	}
}
