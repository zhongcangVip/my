package com.bbsuper.task;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.bbsuper.model.api.util.CConstants;
import com.bbsuper.service.api.driver.BaReleaseLineService;
@Component
@Controller
public class RefreshCommonRouteTask {
private static Logger logger = Logger.getLogger(BaSMSGoodsMathingTask.class);
	@Autowired
	private BaReleaseLineService bareleaselineservice;
	@RequestMapping(value="/refreshCommonRouteTaskStartUp",method = RequestMethod.GET)
	public void execute() {
		long beginTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "refreshCommonRouteTaskStartUp.startUp☆开始☆获取货源信息");
		logger.info("当前线程名称为:" + Thread.currentThread().getName());
		try {
			refreshCommonRoute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long endTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "线路刷新完成，耗时：[ " + (endTimestamp - beginTimestamp) +" ]毫秒。");
	}
	/**
	 * 
	 * @title 刷新司机添加的常用线路
	 * @introduce 每半小时刷新一次
	 * @author wangkai
	 * @date   2017年08月25日
	 * @return 
	 */
	public void refreshCommonRoute(){
		try{
			bareleaselineservice.refreshCommonRoute();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
