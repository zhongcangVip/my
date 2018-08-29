package com.bbsuper.task;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.bbsuper.model.api.util.CConstants;
import com.bbsuper.service.api.base.BaReleaseGoodsService;
@Component
@Controller
public class RefreshGoodsInfoTask {
private static Logger logger = Logger.getLogger(RefreshGoodsInfoTask.class);
	@Resource
	private BaReleaseGoodsService bareleasegoodsservice;
	@RequestMapping(value="/refreshGoodsInfoTaskStartUp",method = RequestMethod.GET)
	public void execute() {
		long beginTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "refreshCommonRouteTaskStartUp.startUp☆开始☆获取货源信息");
		logger.info("当前线程名称为:" + Thread.currentThread().getName());
		try {
			refreshGoodsInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long endTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "线路刷新完成，耗时：[ " + (endTimestamp - beginTimestamp) +" ]毫秒。");
	}
	
	@RequestMapping(value="/closeGoodsInfoTaskStartUp",method = RequestMethod.GET)
	public void executeCloseGoods() {
		long beginTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "executeCloseGoods☆开始☆关闭货源");
		logger.info("当前线程名称为:" + Thread.currentThread().getName());
		try {
			closeGoodsInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long endTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "关闭货源完成，耗时：[ " + (endTimestamp - beginTimestamp) +" ]毫秒。");
	}
	
	/**
	 * @title 刷新货主货源信息
	 * @introduce 两小时刷新一次
	 * @author wangkai
	 * @date   2017年08月25日
	 * @return 
	 */
	public void refreshGoodsInfo(){
		try{
			bareleasegoodsservice.refreshGoodsInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @title 刷新货主货源信息
	 * @introduce 两小时刷新一次
	 * @author wangkai
	 * @date   2017年08月25日
	 * @return 
	 */
	public void closeGoodsInfo(){
		try{
			bareleasegoodsservice.closeGoodsInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
