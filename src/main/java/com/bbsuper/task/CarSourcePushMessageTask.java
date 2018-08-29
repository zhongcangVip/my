package com.bbsuper.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bbsuper.common.Constants;
import com.bbsuper.common.push.AndroidNotification;
import com.bbsuper.common.push.PushClient;
import com.bbsuper.common.push.android.AndroidListcast;
import com.bbsuper.common.push.android.AndroidUnicast;
import com.bbsuper.common.push.ios.IOSListcast;
import com.bbsuper.common.util.CollectionGroupUtil;
import com.bbsuper.common.util.StringUtil;
import com.bbsuper.model.api.BaRouteGoodsMaching;
import com.bbsuper.model.enums.SourceTypeEnum;
import com.bbsuper.model.manager.BaGoodsInfo;
import com.bbsuper.model.manager.GoodsInfo;
import com.bbsuper.service.api.base.SyncLogService;
import com.bbsuper.service.api.driver.BaReleaseLineService;
import com.bbsuper.service.manager.BaUserService;
import com.bbsuper.service.manager.GoodsInfoService;
import com.bbsuper.service.vip.PushMessageService;
import com.bbsuper.service.vip.goodsSource.GoodsSourceService;
import com.google.common.collect.Maps;

/**
 * 车源匹配-消息推送定时器
 * @author yinyuqiao
 * 2017年12月12日 下午2:33:37
 */
@Component
@Controller
@RequestMapping(value="cc")
public class CarSourcePushMessageTask {
	private static Logger logger = Logger.getLogger(CarSourcePushMessageTask.class);
	private static final String DEVICETYPE_IOS_NOTIFY = "ios_notify_push";
	private static final String DEVICETYPE_IOS_MESSAGE = "ios_message_push";
	private static final String DEVICETYPE_ANDROID_NOTIFY = "android_notify_push";
	@Autowired
	private PushMessageService pushMessageService;
	@Autowired
	private BaUserService baUserService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private BaReleaseLineService baReleaseLineService;
	@Autowired
	private SyncLogService synclogservice;
	
	private static PropertiesConfiguration properties;
	
	private ConcurrentMap<String,Object>  tokenMap=null;
	static{
		try {
			properties = new PropertiesConfiguration("config.properties");
        } catch (ConfigurationException e) {
            logger.error("配置文件[config.properties]不存在");
        }
	}
	
	/**
	 * @title 推送给App
	 * @introduce 推送启动入口
	 * @author wangkai
	 * @date 2016年11月15日
	 * @return 
	 */
	@RequestMapping(value="xx")
	public void execute() {
		logger.info("推送消息--begin--");
		logger.error("友盟推送模式：" + properties.getString("umeng_mode"));
		/**
		 * tokenMap 每次执行的时候重新初始货，用来存放用户的token，判断该 token 是否发送过消息。。每次每个token只发送一次
		 */
		tokenMap=new ConcurrentHashMap<String, Object>();
		List<Integer> sendUserList = Collections.synchronizedList(new ArrayList<Integer>()); //已推送的用户
		List<Map<String,Object>> goodsList = pushMessageService.getMatchGoods(); //最新货源
		logger.error("CarSourcePushMessageTask 货源信息:"+goodsList.size());
		if(null == goodsList || goodsList.isEmpty()){
			return;
		}
		logger.error("CarSourcePushMessageTask TokensMap"+tokenMap);
		for(Map<String,Object> obj: goodsList){
			taskExecutor.execute(() -> {
				logger.info("当前线程名称为:" + Thread.currentThread().getName());
	            this.machingGoodsForDriver(obj, sendUserList);
	        });
		}
	}
	/**
	 * @title 推送给App
	 * @introduce 匹配货源信息,消息推送给Android设备
	 * @author wangkai
	 * @date   2016年11月15日
	 * @return
	 */
	private void machingGoodsForDriver(Map<String,Object> obj, List<Integer> sendUserList){
		String goodsId = null==obj.get("id")?"":obj.get("id").toString();
		String vipSourceCode = null==obj.get("vipSourceCode")?"":obj.get("vipSourceCode").toString();
		String startAddressProvinceId = null==obj.get("startAddressProvinceId")?"":obj.get("startAddressProvinceId").toString();
		String startAddressCityId = null==obj.get("startAddressCityId")?"":obj.get("startAddressCityId").toString();
		String startAddressAreaId = null==obj.get("startAddressAreaId")?"":obj.get("startAddressAreaId").toString();
		String endAddressProvinceId = null==obj.get("endAddressProvinceId")?"":obj.get("endAddressProvinceId").toString();
		String endAddressCityId = null==obj.get("endAddressCityId")?"":obj.get("endAddressCityId").toString();
		String endAddressAreaId = null==obj.get("endAddressAreaId")?"":obj.get("endAddressAreaId").toString();
		
		String startAddressProvince = null==obj.get("startAddressProvince")?"":obj.get("startAddressProvince").toString();
		String startAddressCity = null==obj.get("startAddressCity")?"":obj.get("startAddressCity").toString();
		String startAddressArea = null==obj.get("startAddressArea")?"":obj.get("startAddressArea").toString();
		String endAddressProvince = null==obj.get("endAddressProvince")?"":obj.get("endAddressProvince").toString();
		String endAddressCity = null==obj.get("endAddressCity")?"":obj.get("endAddressCity").toString();
		String endAddressArea = null==obj.get("endAddressArea")?"":obj.get("endAddressArea").toString();
		String goodsTraffic = null==obj.get("goodsTraffic")?"":obj.get("goodsTraffic").toString();
		String vehicleType = null==obj.get("vehicleType")?"":obj.get("vehicleType").toString();
		String vehicleLength = null==obj.get("vehicleLength")?"":obj.get("vehicleLength").toString();
		String mobile = null==obj.get("mobile")?"":obj.get("mobile").toString();
		//推送内容
		String message = startAddressProvince + ("".equals(startAddressCity)?"":"-" + startAddressCity)+("".equals(startAddressArea)?"":"-" + startAddressArea)
				+ " 到 " + endAddressProvince + ("".equals(endAddressCity)?"":"-"+endAddressCity) + ("".equals(endAddressArea)?"":"-"+endAddressArea)
				+ "(" + ("".equals(goodsTraffic)?"":goodsTraffic+"吨/") + ("".equals(vehicleLength)?"":vehicleLength+"米") + ("".equals(vehicleType)?"":"/"+vehicleType) + ")," + "联系电话:" + mobile; 
		Map<String,Object> param = Maps.newHashMap();
		if(StringUtil.isNotEmpty(startAddressProvinceId) && StringUtil.isNotEmpty(startAddressCityId) && StringUtil.isNotEmpty(startAddressAreaId)){
			param.put("startType", 3); //区
			param.put("start_address_provinceID", startAddressProvinceId);
			param.put("start_address_cityID", startAddressCityId);
			param.put("start_address_areaID", startAddressAreaId);
		}else if(StringUtil.isNotEmpty(startAddressProvinceId) && StringUtil.isNotEmpty(startAddressCityId)){
			param.put("startType", 2); //市
			param.put("start_address_provinceID", startAddressProvinceId);
			param.put("start_address_cityID", startAddressCityId);
		}else if(StringUtil.isNotEmpty(startAddressProvinceId)){
			param.put("startType", 1); //省
			param.put("start_address_provinceID", startAddressProvinceId);
		}
		if(StringUtil.isNotEmpty(endAddressProvinceId) && StringUtil.isNotEmpty(endAddressCityId) && StringUtil.isNotEmpty(endAddressAreaId)){
			param.put("endType", 3); //区
			param.put("end_address_provinceID", endAddressProvinceId);
			param.put("end_address_cityID", endAddressCityId);
			param.put("end_address_areaID", endAddressAreaId);
		}else if(StringUtil.isNotEmpty(endAddressProvinceId) && StringUtil.isNotEmpty(endAddressCityId)){
			param.put("endType", 2); //市
			param.put("end_address_provinceID", endAddressProvinceId);
			param.put("end_address_cityID", endAddressCityId);
		}else if(StringUtil.isNotEmpty(endAddressProvinceId)){
			param.put("endType", 1); //省
			param.put("end_address_provinceID", endAddressProvinceId);
		}
		param.put("device_tokens", "44"); //安卓
		List<Map<String,Object>> androidMatchedList = pushMessageService.getRouteByGoodAddress(param);
		logger.error("CarSourcePushMessageTask android 用户信息:"+androidMatchedList==null?0:androidMatchedList.size());
		param.put("device_tokens", "64"); //IOS
		List<Map<String,Object>> iosMatchedList = pushMessageService.getRouteByGoodAddress(param);
		if(null != androidMatchedList && androidMatchedList.size() > 0){
			//安卓推送
			this.androidPush(androidMatchedList, sendUserList, message, goodsId, vipSourceCode);
		}
		if(null != iosMatchedList && iosMatchedList.size() > 0){
			//IOS推送
			this.iosPush(iosMatchedList, sendUserList, message, goodsId, vipSourceCode);
		}
	}
	/**
	 * 安卓消息推送
	 * @param androidMatchedList 匹配的线路
	 * @param sendUserList 已推送人员
	 * @param message 消息内容
	 * @param goodsId 货源ID
	 * @param vipSourceCode VIP货源ID
	 */
	@SuppressWarnings("unchecked")
	private void androidPush(List<Map<String,Object>> androidMatchedList, List<Integer> sendUserList, String message, String goodsId, String vipSourceCode){
		List<List<Map<String,Object>>> subList = CollectionGroupUtil.groupListByQuantity(androidMatchedList, 500);
		PushClient pushClient = new PushClient();
		AndroidListcast listCast = null;
		try {
			listCast = new AndroidListcast(Constants.DRIVER_ANDROID_APP_KEY, Constants.DRIVER_ANDROID_APP_MASTER_SECRET);
			listCast.setTicker(Constants.TICKER);
			listCast.setTitle(Constants.TITLE_DRIVER);
			listCast.setText(message);
			if(StringUtil.isNotEmpty(properties.getString("umeng_mode")) && properties.getString("umeng_mode").equals("TEST_MODE")){
				listCast.setTestMode();
            }else{
            	listCast.setProductionMode();
            }
			listCast.goAppAfterOpen();
			listCast.setAfterOpenAction(AndroidNotification.AfterOpenAction.go_activity);
			listCast.setActivity(Constants.DRIVER_GOODS_DETAIL_ACTIVITY);
			listCast.setSound("babasupersound");
			listCast.setExtraField("sourceCode", goodsId);
			listCast.setExtraField("sourceStatus", "newpush");
			if(StringUtil.isNotEmpty(vipSourceCode)){
//				listCast.setExtraField("sourceType", SourceTypeEnum.VIP_GOODS_SOURCE.getCode()+"");
				listCast.setExtraField("detailType","2");
			}else{
//				listCast.setExtraField("sourceType", SourceTypeEnum.GOODS_SOURCE.getCode()+"");
				listCast.setExtraField("detailType","1");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//通知-批量推送
		for(List<Map<String,Object>> list: subList){
			StringBuilder notice_userIds = new StringBuilder(""); //用户ID-通知
			StringBuilder notice_tokens = new StringBuilder(""); //识别码-通知
			for(Map<String,Object> obj: list){
				String userId = obj.get("userId").toString(); //用户ID
				String device_tokens = obj.get("device_tokens").toString(); //设备推送识别码
				logger.error("CarSourcePushMessageTask Tokens"+tokenMap);
				//AhwwGR2KnNDsVeUy4KE4cPfvIlO27quvBsD0yICrDLTg
//				device_tokens="AhwwGR2KnNDsVeUy4KE4cPfvIlO27quvBsD0yICrDLTg";
				/**
				 * 如果此token在MAP 中已经存在，说明此次任务中已推送过消息，此时不再推送。如果不存在，则继续
				 */
				if(tokenMap.get(device_tokens)!=null){
					continue;
				}else{
					tokenMap.put(device_tokens, device_tokens);
				}
				if(!sendUserList.contains(Integer.parseInt(userId))){
					sendUserList.add(Integer.parseInt(userId));
					if(notice_userIds.length() > 0){
						notice_userIds.append(",").append(userId);
					}else{
						notice_userIds.append(userId);
					}
					if(notice_tokens.length() > 0){
						notice_tokens.append(",").append(device_tokens);
					}else{
						notice_tokens.append(device_tokens);
					}
				}
			}
			logger.error("CarSourcePushMessageTask notice_tokens"+notice_tokens);
			if(notice_userIds.length() > 0 && notice_tokens.length() > 0){
				try {
					listCast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);//通知
					listCast.setDeviceTokens(notice_tokens.toString()); //最多500个，以“,”隔开
					boolean flag = pushClient.send(listCast);
					if(flag){
						synclogservice.synchroBaPushMessageLog(goodsId, notice_userIds.toString(), message, DEVICETYPE_ANDROID_NOTIFY, "1");
					}else{
						synclogservice.synchroBaPushMessageLog(goodsId, notice_userIds.toString(), message, DEVICETYPE_ANDROID_NOTIFY, "0");
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("CarSourcePushMessageTask Exception"+e.getMessage());
					synclogservice.synchroBaPushMessageLog(goodsId, notice_userIds.toString(), message, DEVICETYPE_ANDROID_NOTIFY, "0");
				}
			}
		}
		//消息-单个推送
		AndroidUnicast androidUnicast = null;
		try {
			androidUnicast = new AndroidUnicast(Constants.DRIVER_ANDROID_APP_KEY, Constants.DRIVER_ANDROID_APP_MASTER_SECRET);
			androidUnicast.setTicker(Constants.TICKER);
			androidUnicast.setTitle(Constants.TITLE_DRIVER);
			androidUnicast.setText(message);
			if(StringUtil.isNotEmpty(properties.getString("umeng_mode")) && properties.getString("umeng_mode").equals("TEST_MODE")){
				androidUnicast.setTestMode();
            }else{
            	androidUnicast.setProductionMode();
            }
			androidUnicast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);//通知
			androidUnicast.goAppAfterOpen();
			androidUnicast.setAfterOpenAction(AndroidNotification.AfterOpenAction.go_activity);
			androidUnicast.setActivity(Constants.DRIVER_GOODS_DETAIL_ACTIVITY);
			androidUnicast.setSound("babasupersound");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("CarSourcePushMessageTask Exception"+e.getMessage());
		}
		for(Map<String,Object> obj: androidMatchedList){
			String routeId = obj.get("id").toString(); //线路ID
			String device_tokens = obj.get("device_tokens").toString(); //设备推送识别码
//			device_tokens="AhwwGR2KnNDsVeUy4KE4cPfvIlO27quvBsD0yICrDLTg";
			/**
			 * 如果此token在MAP 中已经存在，说明此次任务中已推送过消息，此时不再推送。如果不存在，则继续
			 */
			logger.error("CarSourcePushMessageTask Tokens"+device_tokens+"||"+tokenMap);
			if(tokenMap.get(device_tokens)!=null){
				continue;
			}else{
				tokenMap.put(device_tokens, device_tokens);
			}
			try {
	            androidUnicast.setDeviceToken(device_tokens);
//	            Map<String,Integer> custom = new HashMap<String,Integer>();
//	            custom.put("sourceType", -1);
//	            custom.put("routeId", Integer.parseInt(routeId));
//	            custom.put("goodsId", Integer.parseInt(goodsId));
//	            androidUnicast.setCustomField(JSONObject.fromObject(custom).toString());
	            if(StringUtil.isNotEmpty(vipSourceCode)){
	            	androidUnicast.setExtraField("detailType", "2");
	            }else{
	            	androidUnicast.setExtraField("detailType", "1");
	            }
	            androidUnicast.setExtraField("sourceStatus", "newpush");//用来区分委托
	            androidUnicast.setExtraField("sourceType", "1");
	            androidUnicast.setExtraField("sourceCode", goodsId);
	            pushClient.send(androidUnicast);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * IOS消息推送
	 * @param iosMatchedList 匹配的线路
	 * @param sendUserList 已推送人员
	 * @param message 消息内容
	 * @param goodsId 货源ID
	 * @param vipSourceCode VIP货源ID
	 */
	@SuppressWarnings("unchecked")
	private void iosPush(List<Map<String,Object>> iosMatchedList, List<Integer> sendUserList, String message, String goodsId, String vipSourceCode){
		List<List<Map<String,Object>>> subList = CollectionGroupUtil.groupListByQuantity(iosMatchedList, 500);
		PushClient pushClient = new PushClient();
		IOSListcast listCast = null;
		try {
			listCast = new IOSListcast(Constants.DRIVER_IOS_APP_KEY, Constants.DRIVER_IOS_APP_MASTER_SECRET);
			listCast.setAlert(message);
			listCast.setBadge(1);
			if(StringUtil.isNotEmpty(properties.getString("umeng_mode")) && properties.getString("umeng_mode").equals("TEST_MODE")){
				listCast.setTestMode();
            }else{
            	listCast.setProductionMode();
            }
			listCast.setSound("babasupersound.wav");
			listCast.setCustomizedField("sourceCode", goodsId);
			if(StringUtil.isNotEmpty(vipSourceCode)){
				listCast.setCustomizedField("sourceType", SourceTypeEnum.VIP_GOODS_SOURCE.getCode()+"");
			}else{
				listCast.setCustomizedField("sourceType", SourceTypeEnum.GOODS_SOURCE.getCode()+"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(List<Map<String,Object>> list: subList){
			StringBuilder message_userIds = new StringBuilder(""); //用户ID-消息
			List<BaRouteGoodsMaching> message_machList = new ArrayList<BaRouteGoodsMaching>(); //匹配信息-消息
			StringBuilder notice_userIds = new StringBuilder(""); //用户ID-通知
			StringBuilder notice_tokens = new StringBuilder(""); //识别码-通知
			for(Map<String,Object> obj: list){
				String userId = obj.get("userId").toString(); //用户ID
				String device_tokens = obj.get("device_tokens").toString(); //设备推送识别码
				if(tokenMap.get(device_tokens)!=null){
					continue;
				}else{
					tokenMap.put(device_tokens, device_tokens);
				}
				if(message_userIds.length() > 0){
					message_userIds.append(",").append(userId);
				}else{
					message_userIds.append(userId);
				}
				BaRouteGoodsMaching baRouteGoodsMaching = new BaRouteGoodsMaching();
		        baRouteGoodsMaching.setRouteId(obj.get("id").toString());
		        baRouteGoodsMaching.setGoodsId(goodsId);
		        baRouteGoodsMaching.setGoodsAddress(message);
		        message_machList.add(baRouteGoodsMaching);
				if(!sendUserList.contains(Integer.parseInt(userId))){
					sendUserList.add(Integer.parseInt(userId));
					if(notice_userIds.length() > 0){
						notice_userIds.append(",").append(userId);
					}else{
						notice_userIds.append(userId);
					}
					if(notice_tokens.length() > 0){
						notice_tokens.append(",").append(device_tokens);
					}else{
						notice_tokens.append(device_tokens);
					}
				}
			}
			if(null != message_machList && message_machList.size() > 0){
				try {
					boolean flag = baReleaseLineService.batchRouteGoodsMaching(message_machList);
					if(flag){
						synclogservice.synchroBaPushMessageLog(goodsId, message_userIds.toString(), message, DEVICETYPE_IOS_MESSAGE, "1");
					}else{
						synclogservice.synchroBaPushMessageLog(goodsId, message_userIds.toString(), message, DEVICETYPE_IOS_MESSAGE, "0");
					}
				} catch (Exception e) {
					e.printStackTrace();
					synclogservice.synchroBaPushMessageLog(goodsId, message_userIds.toString(), message, DEVICETYPE_IOS_MESSAGE, "0");
				}
			}
			if(notice_userIds.length() > 0 && notice_tokens.length() > 0){
				try {
					listCast.setDeviceTokens(notice_tokens.toString()); //最多500个，以“,”隔开
					boolean flag = pushClient.send(listCast);
					if(flag){
						synclogservice.synchroBaPushMessageLog(goodsId, notice_userIds.toString(), message, DEVICETYPE_IOS_NOTIFY, "1");
					}else{
						synclogservice.synchroBaPushMessageLog(goodsId, notice_userIds.toString(), message, DEVICETYPE_IOS_NOTIFY, "0");
					}
				} catch (Exception e) {
					e.printStackTrace();
					synclogservice.synchroBaPushMessageLog(goodsId, notice_userIds.toString(), message, DEVICETYPE_IOS_NOTIFY, "0");
				}
			}
		}
	}
	
}
