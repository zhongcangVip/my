package com.bbsuper.task.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bbsuper.common.domain.Result;
import com.bbsuper.model.enums.UserTypeEnum;
import com.bbsuper.service.app.AppBillService;
import com.bbsuper.service.manager.BaUserService;
import com.google.common.collect.Lists;

/**
 * 定时更新用户交易量
 * @author liwei
 * @date: 2018年6月15日 上午10:50:15
 *
 */
@Component
public class UpdateUserDealCountTask {
	
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(UpdateUserDealCountTask.class);
	
	@Resource
    private AppBillService appBillService;
	
	@Resource
	private BaUserService baUserService;
	
	/**
	 * 统计数据：测试环境500个用户共耗时12s
	 */
	public void updateDealCount(){
		updateDealCountByDay(2);
	}
	public void updateDealCountAll(){
		updateDealCountByDay(null);
	}
	
	public void updateDealCountByDay(Integer day){
		logger.info("updateDealCount start ...");
		long start = System.currentTimeMillis();
		//查询2天内有新运单的用户
		List<Map<String, Object>> users = queryNewBillUser(day);
		
		logger.info("updateDealCount query user end ,time:{}ms,count:{}",System.currentTimeMillis()-start,users.size());
		//查询用户的交易量
		List<Map<String, Object>> userDeal = queryUserDealCount(users);
		
		logger.info("updateDealCount query dealCount end ,time:{}ms",System.currentTimeMillis()-start);
		//批量更新用户交易量
		baUserService.batchUpdateDealCount(userDeal);
		
		logger.info("updateDealCount end ,time:{}ms",System.currentTimeMillis()-start);
	}
	
	private List<Map<String, Object>> queryUserDealCount(List<Map<String, Object>> users) {
		users.forEach((u)->{
			UserTypeEnum userType = 0==(int)u.get("usertype")?UserTypeEnum.DRIVER:UserTypeEnum.OWNER;
			Result<Integer> successCount = appBillService.getSuccessCount(String.valueOf(u.get("userid")), userType);
			u.put("count", successCount.getData()==null?0:successCount.getData());
			
		});
		return users;
	}

	private List<Map<String, Object>> queryNewBillUser(Integer i) {
		List<Map<String, Object>> userList = appBillService.queryNewBillUser(i);
		List<Map<String, Object>> users = Lists.newArrayList();
		userList.forEach(u->{
			Map<String, Object> driver = new HashMap<>();
			driver.put("userid", u.get("fdriverId"));
			driver.put("usertype",0);
			Map<String, Object> owner = new HashMap<>();
			owner.put("userid", u.get("fownerId"));
			owner.put("usertype",1);
			users.add(driver);
			users.add(owner);
		});
		logger.info("updateDealCount userCount:{}",users.size());
		//去重后用户
		List<Map<String, Object>> distinctUser = Lists.newArrayList();
		users.forEach(m->{
			boolean distinct = true;
			for(Map<String, Object> d:distinctUser){
				if(d.get("userid").equals(m.get("userid"))){
					distinct = false;
				}
			}
			if(distinct){
				distinctUser.add(m);
			}
		});
		return distinctUser;
	}
}
