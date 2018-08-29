package com.bbsuper.task.web;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.service.manager.GoodsInfoService;
import com.bbsuper.service.manager.RouteService;

/**
 * 初始化车源、货源浏览量
 * @author yinyuqiao
 * 2017年7月19日 上午10:23:54
 */
@Component
public class InitClickCountTask{
	@Autowired
	private RouteService routeService;
	@Autowired
	private GoodsInfoService goodsInfoService;
	
	/**
	 * 初始化车源浏览量
	 */
	public void executeDriverSource(){
		routeService.updateClickCount(null, true);
	}
	/**
	 * 初始化货源浏览量
	 */
	public void executeOwnerSource(){
		goodsInfoService.updateClickCount(null, true);
	}

	
}
