package com.bbsuper.task.api;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bbsuper.model.api.IntegralDetail;
import com.bbsuper.model.api.util.CConstants;
import com.bbsuper.service.api.base.BaInviteCodeService;
import com.bbsuper.service.api.base.IntegralService;
import com.bbsuper.service.manager.BaUserService;
@Component
@Controller
public class UserWeekIntegralRankingTask {
	private static Logger logger = Logger.getLogger(UserWeekIntegralRankingTask.class);
	@Resource
	private IntegralService integralService ;
	@Resource
	private BaUserService baUserService;
	@Autowired
	private BaInviteCodeService  baInviteCodeService;
	@RequestMapping(value="/userWeekIntegralRankingTask",method = RequestMethod.GET)
	public void execute() {
		long beginTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "userWeekIntegralRankingTask.startUp☆开始☆获取积分信息");
		logger.info("当前线程名称为:" + Thread.currentThread().getName());
		try {
			userWeekIntegralRankingTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long endTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "积分排名统计完成，耗时：[ " + (endTimestamp - beginTimestamp) +" ]毫秒。");
	}
	/**
	 * 
	 * @title 统计一周积分排名
	 * @introduce 每周星期天0点更新
	 * @author wangkai
	 * @date   2017年08月25日
	 * @return 
	 */
	public void userWeekIntegralRankingTask(){
		try{
			Map<String,Object> param =  new HashMap<String,Object>();
			integralService.deleteUserIntegralRanking(param);
			List<IntegralDetail> integralRankingList = integralService.staticUserIntegralRanking(param);
			for(IntegralDetail integralDetail  : integralRankingList){
				param.put("userId", integralDetail.getUserId());
				param.put("userName", integralDetail.getUserName());
				param.put("avoteUrl", integralDetail.getAvoteUrl());
				param.put("integral", integralDetail.getTotalIntegral());
				param.put("rank", integralDetail.getRank());
				integralService.insertUserIntegralRanking(param);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
