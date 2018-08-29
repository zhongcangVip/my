package com.bbsuper.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bbsuper.common.util.SendMessageUtil;
import com.bbsuper.model.api.BaCommonRoute;
import com.bbsuper.model.api.BaMatchGoods;
import com.bbsuper.model.api.BaSendSmsLog;
import com.bbsuper.model.api.util.CConstants;
import com.bbsuper.model.manager.BaGoodsInfo;
import com.bbsuper.model.manager.BaUser;
import com.bbsuper.service.api.base.BaReleaseGoodsService;
import com.bbsuper.service.api.base.SyncLogService;
import com.bbsuper.service.api.driver.BaReleaseLineService;
import com.bbsuper.service.manager.BaUserService;
@Component
@RequestMapping("/app/business/baGoodsMathing")
public class BaSMSGoodsMathingTask {
	private static Logger logger = Logger.getLogger(BaSMSGoodsMathingTask.class);
	
	public static final String  USERNAME = "bbsp";
	
	public static final String  PASSWORD = "babasuper911";
	
	@Resource
	private BaReleaseGoodsService bareleasegoodsservice;

	@Autowired
	private BaReleaseLineService bareleaselineservice;

	@Autowired
	private BaUserService bauserservice;
	
	@Autowired
	private SyncLogService synclogservice;
	
	/**
	 * 
	 * @title 推送给App
	 * @introduce 推送启动入口,10分钟扫描一次
	 * @author wangkai
	 * @date   2016年11月15日
	 * @return 
	 */ 
	@RequestMapping(value="/startUp",method = RequestMethod.POST)
	// @Scheduled(cron = "0 0/10 * * * ?") 
	public void execute() {
		long beginTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "BaSMSGoodsMathingController.startUp☆开始☆获取货源信息");
		logger.info("当前线程名称为:" + Thread.currentThread().getName());
		try {
			machingGoodsForDriver();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long endTimestamp = System.currentTimeMillis();
		logger.info(CConstants.LOG_PREFIX + "短信下发完成，耗时：[ " + (endTimestamp - beginTimestamp) +" ]毫秒。");
	}
	/**
	 * 
	 * @title 推送给App
	 * @introduce 匹配货源信息,短信发送给司机
	 * @author wangkai
	 * @date   2016年11月14日
	 * @return 
	 */
	public void machingGoodsForDriver(){
		BaGoodsInfo baGoodsInfo =  new BaGoodsInfo();
		List<BaGoodsInfo> baGoodsInfoList = bareleasegoodsservice.queryMatchGoodsInfo(baGoodsInfo);
		/**每次轮询读取50条货源信息,读取未下发的标识***/
		for(int i=0;i<baGoodsInfoList.size();i++){
			BaGoodsInfo curBaGoodsInfo = baGoodsInfoList.get(i);
			/**获取货源地址**/
			String goodsAddress = curBaGoodsInfo.getStartAddressProvince() + (curBaGoodsInfo.getStartAddressCity() == null?"-":("-"+curBaGoodsInfo.getStartAddressCity()))
			+(curBaGoodsInfo.getStartAddressArea() ==null?"-":("-"+curBaGoodsInfo.getStartAddressArea())) +"-"+curBaGoodsInfo.getEndAddressProvince()+
			(curBaGoodsInfo.getEndAddressCity()==null?"-":("-"+curBaGoodsInfo.getEndAddressCity())) +(curBaGoodsInfo.getEndAddressArea()==null?"-":("-"+curBaGoodsInfo.getEndAddressArea()));
			logger.info(CConstants.LOG_PREFIX + "货源地址:" +  goodsAddress);
			BaMatchGoods baMatchGoods = new BaMatchGoods();
			/**当前货源id**/
			baMatchGoods.setGoodsId(curBaGoodsInfo.getId()); 
			/**当前货源地址**/
			baMatchGoods.setAddress(goodsAddress);
			/**根据货源地址匹配线路,每次匹配50条线路,若有线路,一直查询,若没有结束循环**/
			List<BaCommonRoute> baCommonRouteList = bareleaselineservice.queryReleaseLinesByGoodAddress(baMatchGoods);
			/**若一直有匹配线路,就一直轮询**/
			while(baCommonRouteList.size() > 0){
				for(int j=0;j<baCommonRouteList.size();j++){
					/**获取具体的一条线路信息**/
					BaCommonRoute curBaCommonRoute =  baCommonRouteList.get(j);
					if(curBaCommonRoute!=null){
						try {
							BaUser baUser =new BaUser();
							baUser.setUserid(curBaCommonRoute.getUserId());
							/**查询出线路对应的手机号码**/
							Map<String,Object> param = new HashMap<String,Object>();
							param.put("mobile", curBaCommonRoute.getMobile());
							BaUser curBaUser = bauserservice.getOneBaUser(param);
							String mobile = curBaUser.getMobile();
							logger.info(CConstants.LOG_PREFIX + mobile);
							StringBuffer contentBuffer = new StringBuffer();
							contentBuffer.append("尊敬的叭叭物流用户,");
							contentBuffer.append("现有起始地:(").append(curBaGoodsInfo.getStartAddressProvince() +
							(curBaGoodsInfo.getStartAddressCity()==null?"":"-"+curBaGoodsInfo.getStartAddressCity()) +(curBaGoodsInfo.getStartAddressArea()==null?"":"-"+curBaGoodsInfo.getStartAddressArea())+"),")
							.append("目的地:(").append(curBaGoodsInfo.getEndAddressProvince()+
							(curBaGoodsInfo.getEndAddressCity()==null?"":"-"+curBaGoodsInfo.getEndAddressCity()) +(curBaGoodsInfo.getEndAddressArea()==null?"":"-"+curBaGoodsInfo.getEndAddressArea())).append(")的货源信息,")
							.append("[货源详情<货品类型:"+(curBaGoodsInfo.getGoodsTypeName()==null?"不限":curBaGoodsInfo.getGoodsTypeName()) + ",货品名称:"+(curBaGoodsInfo.getGoodsName()==null ?"不限":curBaGoodsInfo.getGoodsName()))
							.append(",装货日期:").append(curBaGoodsInfo.getGoodsStartDate()).append(",货物重量:").append(curBaGoodsInfo.getGoodsTraffic()).append("吨")
							.append(",货物体积:").append(curBaGoodsInfo.getGoodsBulk()).append("L").append(",运费金额:").append((curBaGoodsInfo.getFreighCharges()==null?"待议":curBaGoodsInfo.getFreighCharges()+"元"))
							.append(">,请登陆叭叭物流司机端app查看,请不要把货物信息泄露给其他人。");
							logger.info(CConstants.LOG_PREFIX + "短信发送内容" +  contentBuffer.toString());
							String content = contentBuffer.toString();
							
							// 单条发送
							int result = SendMessageUtil.sendMessage(mobile,mobile);
					        if (result == 200) {
					            BaSendSmsLog baSendSmsLog =  new BaSendSmsLog();
					            baSendSmsLog.setGoodId(curBaGoodsInfo.getId()+"");
					            baSendSmsLog.setUserId(curBaUser.getUserid()+"");
					            baSendSmsLog.setMobile(curBaUser.getMobile());
					            baSendSmsLog.setSendContent(content);
					            baSendSmsLog.setSendStatus("1");
					            syncSmsSendLog(baSendSmsLog);
					        } else{
					        	BaSendSmsLog baSendSmsLog =  new BaSendSmsLog();
					            baSendSmsLog.setGoodId(curBaGoodsInfo.getId()+"");
					            baSendSmsLog.setUserId(curBaUser.getUserid()+"");
					            baSendSmsLog.setMobile(curBaUser.getMobile());
					            baSendSmsLog.setSendContent(content);
					            baSendSmsLog.setSendStatus("999"); //异常
					            syncSmsSendLog(baSendSmsLog);
					        }
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				//查询当前该货源是否下发完所有用户
				baCommonRouteList = bareleaselineservice.queryReleaseLinesByGoodAddress(baMatchGoods);
				if(baCommonRouteList.size() <= 0){
			        break; //结束当前轮询,进入到下一条货源匹配
				}
			}
			updateSendSmsFlag(curBaGoodsInfo); //该条货源已经匹配完所有线路,更新货源扫描标识为下发完成
		}
	}
	/**
	 * @title 推送给App接口
	 * @introduce 更新货源短信标识
	 * @author wangkai
	 * @date   2016年11月14日
	 * @return 
	 */
	public void updateSendSmsFlag(BaGoodsInfo baGoodsInfo){
		bareleasegoodsservice.updateSendSmsFlag(baGoodsInfo);
	}
	/**
	 * @title 推送给App接口
	 * @introduce 匹配货源信息,短信发送给司机
	 * @author wangkai
	 * @date   2016年11月14日
	 * @return 
	 */
	public void syncSmsSendLog(BaSendSmsLog baSendSmsLog){
		synclogservice.synchroBaSendSmsLog(baSendSmsLog);
	}
	
	public static void main(String[] args) {
		new BaSMSGoodsMathingTask().execute();
	}

}
