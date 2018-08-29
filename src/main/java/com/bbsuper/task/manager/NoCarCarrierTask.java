package com.bbsuper.task.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.common.util.DateUtils;
import com.bbsuper.common.util.RandomUtil;
import com.bbsuper.service.manager.NoCarCarrierService;

/**
 * 无车承运人数据上传
 * @author yinyuqiao
 * 2017年12月14日 上午11:22:30
 */
@Component
public class NoCarCarrierTask{
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(NoCarCarrierTask.class);
	@Autowired
	private NoCarCarrierService noCarCarrierService;
	
	public void execute(){
		logger.info("noCarCarriertask start ...");
		try {
			logger.info("noCarCarriertask running...");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -2);
			noCarCarrierService.uploadDataOld("", DateUtils.format(calendar.getTime(), "yyyyMMdd"), new BigDecimal(RandomUtil.getBetweenRandom(149, 200)).intValue());
		} catch (Exception e) {
			logger.error("noCarCarriertask exception", e);
		}
		logger.info("noCarCarriertask end ...");
		
	}

	
}
