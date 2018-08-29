package com.bbsuper.task;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.bbsuper.common.llpay.config.PartnerConfig;
import com.bbsuper.common.llpay.vo.PaymentInfo;
import com.bbsuper.common.util.RandomUtil;
import com.bbsuper.model.manager.BaDriverGoodsOrderSteps;
import com.bbsuper.model.manager.BaDriverGoodsPayDetail;
import com.bbsuper.model.manager.BaGoodsInfo;
import com.bbsuper.model.manager.BaOrderManagerDto;
import com.bbsuper.model.manager.BaUser;
import com.bbsuper.model.manager.BaWallet;
import com.bbsuper.model.manager.BaWalletBill;
import com.bbsuper.service.api.base.BaReleaseGoodsService;
import com.bbsuper.service.api.bussiness.OrderStepsService;
import com.bbsuper.service.api.bussiness.ReceiveNotifyService;
import com.bbsuper.service.api.driver.BaDriverOrderService;
import com.bbsuper.service.api.owner.BaAgentOrderService;
import com.bbsuper.service.manager.BaUserService;
import com.bbsuper.service.manager.BaWalletBillInfoService;
import com.bbsuper.service.manager.WalletService;

@Component
public class BaOrderBaseTask {
	private static Logger logger = Logger.getLogger(BaOrderBaseTask.class);
	@Resource
	protected  BaDriverOrderService badriverorderservice;
	@Resource
	protected BaReleaseGoodsService bareleasegoodsservice;
	@Resource
	protected BaUserService bauserservice;
	@Resource
	protected ReceiveNotifyService receivenotifyservice;
	@Resource
	protected BaWalletBillInfoService billservice;
	@Resource
	protected WalletService walletservice;
	@Resource
	protected OrderStepsService orderstepsservice;
	@Resource
	protected  BaAgentOrderService baagentorderservice;
	/**
	 * @title
	 * @introduce 更新订单流程环节时间
	 * @author wangkai
	 * @date   2017年01月07日
	 * @param curBaDriverGoodsOrder
	 * @return  Response
	 */
	protected boolean updateBaOrderStepsTime(BaOrderManagerDto curBaDriverGoodsOrder,String stepsCode){
		BaDriverGoodsOrderSteps updateDriverLoadingSteps = new BaDriverGoodsOrderSteps();
		updateDriverLoadingSteps.setOrderNo(curBaDriverGoodsOrder.getOrderId()); //订单编号
		updateDriverLoadingSteps.setStepsCode(stepsCode); //环节编码
		updateDriverLoadingSteps.setCreatetime(new Date(System.currentTimeMillis()));
    	boolean updateBaOrderStepsTimeFlag = false;
    	updateBaOrderStepsTimeFlag = orderstepsservice.updateBaOrderStepsTime(updateDriverLoadingSteps);
    	return updateBaOrderStepsTimeFlag;
	}
	/**
	 * @title
	 * @introduce 生成订单下一环节数据
	 * @author wangkai
	 * @date   2017年01月07日
	 * @param curBaDriverGoodsOrder
	 * @return  Response
	 */
	protected boolean createBaDriverGoodsOrderSteps(BaOrderManagerDto curBaDriverGoodsOrder,String stepsCode,String stepsName,String StepsRemark){
		BaDriverGoodsOrderSteps baDriverGoodsOrderSteps = new BaDriverGoodsOrderSteps();
    	baDriverGoodsOrderSteps.setOrderNo(curBaDriverGoodsOrder.getOrderId());  //订单编号
    	baDriverGoodsOrderSteps.setOrderType(curBaDriverGoodsOrder.getOrderType()); //订单类型
    	baDriverGoodsOrderSteps.setStepsCode(stepsCode);
    	baDriverGoodsOrderSteps.setStepsName(stepsName);
		baDriverGoodsOrderSteps.setStepsRemark(StepsRemark);
		baDriverGoodsOrderSteps.setCreatetime(new Date(System.currentTimeMillis()));
		BaDriverGoodsOrderSteps curBaDriverGoodsOrderSteps = orderstepsservice.queryBaOrderStepsByOrderIdAndStepsCode(baDriverGoodsOrderSteps);
		boolean createOrderStepsFlag = false;
		if(curBaDriverGoodsOrderSteps!=null){
			createOrderStepsFlag = orderstepsservice.updateBaOrderSteps(baDriverGoodsOrderSteps);
        }else{
        	createOrderStepsFlag = orderstepsservice.saveBaOrderSteps(baDriverGoodsOrderSteps);
        }
		return createOrderStepsFlag;
	}
	/***
	 * @title
	 * @introduce 生成APP支付基础数据
	 * @author wangkai
	 * @date   2017年01月07日
	 * @param baDriverGoodsPayDetail
	 * @param paymentType  缴纳类型
	 * @return
	 */
	protected PaymentInfo getAppPayBasicData(BaDriverGoodsPayDetail baDriverGoodsPayDetail){
		PaymentInfo paymentinfo = new PaymentInfo();
		paymentinfo.setSign_type(PartnerConfig.SIGN_TYPE); //签名方式 RSA或MD5
		paymentinfo.setOid_partner(PartnerConfig.OID_PARTNER);   // 商户编号
		paymentinfo.setBusi_partner(PartnerConfig.BUSI_PARTNER); // 业务类型，连连支付根据商户业务为商户开设的业务类型； （101001：虚拟商品销售、109001：实物商品销售、108001：外部账户充值）
		paymentinfo.setNo_order(baDriverGoodsPayDetail.getOrderBusiid()); //商户系统唯一订单号
		paymentinfo.setDt_order(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())); //商户订单时间
		return paymentinfo;
	}
	/***
	 * @title
	 * @introduce 保存账单数据
	 * @author wangkai
	 * @date   2017年01月07日
	 * @param curBaDriverGoodsOrder
	 * @param baWallet
	 * @return
	 */
	protected boolean createBaWalletBill(BaUser curBaUser,BaOrderManagerDto curBaDriverGoodsOrder,BaWallet baWallet,String payType,String payAmount,String payDetail,String orderStatus){
		java.text.DecimalFormat decimalFormat =new java.text.DecimalFormat("#.00"); 
		//保存司机账单
		BaWalletBill baWalletBill = new BaWalletBill();
		baWalletBill.setUserid(curBaUser.getUserid()+"");
		baWalletBill.setUserType(Integer.parseInt(curBaUser.getUsertype()));
		baWalletBill.setUserName(curBaUser.getRealname());
		baWalletBill.setMobile(curBaUser.getMobile());
		baWalletBill.setPayBusiid(RandomUtil.getWalletBillByUUId());
		baWalletBill.setPayType(payType);
		baWalletBill.setPayBankCard("叭叭物流商户号");
		baWalletBill.setPayAmount(payAmount == null?"":payAmount.substring(0, 1)+decimalFormat.format(Double.parseDouble(payAmount.substring(1))));
        baWalletBill.setPayDetails(payDetail);
        baWalletBill.setOrderNo(curBaDriverGoodsOrder.getOrderId());
        baWalletBill.setOrderStatus(orderStatus);
        BaGoodsInfo queryBaGoodsInfo = bareleasegoodsservice.queryBaGoodsInfoById(Integer.parseInt(curBaDriverGoodsOrder.getGoodId()));
        baWalletBill.setGoodsInfo((queryBaGoodsInfo.getStartAddressProvince()==null?"":queryBaGoodsInfo.getStartAddressProvince()) +
        		(queryBaGoodsInfo.getStartAddressCity()==null?"":queryBaGoodsInfo.getStartAddressCity()) +
        		(queryBaGoodsInfo.getStartAddressArea()==null?"":queryBaGoodsInfo.getStartAddressArea()) + "至"
        		+(queryBaGoodsInfo.getEndAddressProvince()==null?"":queryBaGoodsInfo.getEndAddressProvince()) + 
        				(queryBaGoodsInfo.getEndAddressCity()==null?"":queryBaGoodsInfo.getEndAddressCity()) 
        						+ (queryBaGoodsInfo.getEndAddressArea() == null?"":queryBaGoodsInfo.getEndAddressArea()));
        baWalletBill.setStatus("2");
		baWalletBill.setCurWalletBlance(baWallet.getWalletBalance()); //设置当前钱包余额
		baWalletBill.setTradingTime(new Date(System.currentTimeMillis()));
		return billservice.saveBaWalletBill(baWalletBill) > 0;
	}
	
	/***
	 * @title
	 * @introduce 超时30分钟，未确认的情况，自动取消订单
	 * @author wangkai
	 * @date   2017年01月13日
	 * @return
	 */
	//@Scheduled(cron = "0 0/10 * * * ?") 
	public void orderExpired(){
		logger.info("orderExpired" + "----->>>>>取消订单");
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		List<BaOrderManagerDto>  baDriverGoodsOrderList = badriverorderservice.queryBaExpiredOrder();
	    for(BaOrderManagerDto curBaDriverGoodsOrder : baDriverGoodsOrderList){
	    	String orderType = curBaDriverGoodsOrder.getOrderType(); //0-司机确认货主订单 1-司机确认经纪人订单 2-货主委托司机运输 3-经纪人委托司机运输
	    	/**
			 * 0-司机确认货主订单 1-司机确认经纪人订单 
			 */
	    	if(orderType.endsWith("0") || orderType.endsWith("1") ){
	    	  //查询司机信息
	    	  BaUser baUser = new BaUser();
	    	  baUser.setUserid(Integer.parseInt(curBaDriverGoodsOrder.getDriverId()));
	    	  BaUser curBaUser = bauserservice.getBaUserById(baUser.getUserid()+"");
	          //退还司机保证金到司机钱包
	    	  if("1".equals(curBaDriverGoodsOrder.getIsdriverpaymargin())){
	    		  //查询司机钱包余额
					BaWallet baWallet = new BaWallet();
					baWallet.setUserid(curBaUser.getUserid()+"");
					baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
					baWallet.setStatus("2");
					BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
					//更新司机钱包余额 
					if(curBaWallet!=null){
						if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
							//司机保证金+钱包余额
							baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getDriverpaymoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
						}else{
							//第一次设置
							baWallet.setWalletBalance(curBaDriverGoodsOrder.getDriverpaymoney());
						}
						walletservice.updateWalletBalance(baWallet);
					}else{
						baWallet.setWalletBalance(curBaDriverGoodsOrder.getDriverpaymoney());
						walletservice.saveWalletBalance(baWallet);
					}
					//保存账单
					String payType = "1"; //退款
					String payAmount = "+"+curBaDriverGoodsOrder.getDriverpaymoney();
					BaUser driverUser = new BaUser();
				    driverUser.setUserid(Integer.parseInt(curBaDriverGoodsOrder.getDriverId())); 
					String payDetail = "叭叭物流退还保证金"+curBaDriverGoodsOrder.getDriverpaymoney()+"元";
					String orderStatus= "超时30分钟自动取消订单";
					boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
					if(saveBillFlag){
						logger.info("保存账单成功!");
					}
	    	  }
	    	  //司机若交了信息费,退信息费到司机钱包
	    	  if(StringUtils.isNotEmpty(curBaDriverGoodsOrder.getInformationmoney())){
	      		//查询司机钱包余额
				BaWallet baWallet = new BaWallet();
				baWallet.setUserid(curBaUser.getUserid()+"");
				baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
				baWallet.setStatus("2");
				BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
				//更新司机钱包余额 
				if(curBaWallet!=null){
					if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
						//司机信息费+钱包余额
						baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getInformationmoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
					}else{
						//第一次设置
						baWallet.setWalletBalance(curBaDriverGoodsOrder.getInformationmoney());
					}
					walletservice.updateWalletBalance(baWallet);
				}else{
					baWallet.setWalletBalance(curBaDriverGoodsOrder.getInformationmoney());
					walletservice.saveWalletBalance(baWallet);
				}
				//保存账单
				String payType = "1"; //退款
				String payAmount = "+"+curBaDriverGoodsOrder.getInformationmoney();
				BaUser driverUser = new BaUser();
			    driverUser.setUserid(Integer.parseInt(curBaDriverGoodsOrder.getDriverId())); 
				String payDetail = "叭叭物流退还信息费"+curBaDriverGoodsOrder.getInformationmoney()+"元";
				String orderStatus= "超时30分钟自动取消订单";
				boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
				if(saveBillFlag){
					logger.info("保存账单成功!");
				}
	    	  }
	    	  //设置当前交易状态为交易关闭
	    	  String stepsRemak = "";
	    	  if(orderType.endsWith("0") || orderType.endsWith("1") ){
	    		  if(orderType.endsWith("0")){
	    			  stepsRemak = "货主已超过30分钟未处理订单，订单取消。若支付了保证金，保证金将退回到钱包";
	    		  }
	    		  if(orderType.endsWith("1")){
	    			  stepsRemak = "经纪人已超过30分钟未处理订单，订单取消。若支付了保证金，保证金将退回到钱包";
	    		  }
	    	  }
	          boolean createOrderStepsFlag = createBaDriverGoodsOrderSteps(curBaDriverGoodsOrder, "10", "交易关闭", stepsRemak);
	    	  if(createOrderStepsFlag){
	    			logger.info("保存订单环节成功!");
	    	  }else{
	    			logger.info("保存订单环节失败!");
	    	  }
	          //交易纠关闭
	    	  curBaDriverGoodsOrder.setOrderStatus("10"); //交易关闭
	    	  boolean flag = badriverorderservice.alertDriverConfirmOrderNextNodeStatus(curBaDriverGoodsOrder);
	    	  if(flag){
	    		  logger.info("交易关闭");
	    	  }
	    	}
	    	
	    	/**
			 * 2-货主委托司机运输 
			 * author:songqiuming
			 */
	    	if(orderType.endsWith("2") ){
	    		//查询货主信息
				BaUser baUser =new BaUser();
				BaGoodsInfo curBaGoodsInfo  = bareleasegoodsservice.queryBaGoodsInfoById(Integer.parseInt(curBaDriverGoodsOrder.getGoodId()));
				baUser.setUserid(curBaGoodsInfo.getUserId()); //用户id 货主
				BaUser curBaUser = bauserservice.getBaUserById(baUser.getUserid()+"");
				//退还货主保证金到货主钱包
				if(curBaUser !=null && "1".equals(curBaDriverGoodsOrder.getIsgoodspaymargin())){
					//查询货主钱包余额
					BaWallet baWallet = new BaWallet();
					baWallet.setUserid(curBaUser.getUserid()+"");
					baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
					baWallet.setStatus("2");
					BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
					//更新货主钱包余额 
					if(curBaWallet!=null){
						if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
							//货主保证金+钱包余额
							baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getGoodspaymoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
						}else{
							//第一次设置
							baWallet.setWalletBalance(curBaDriverGoodsOrder.getGoodspaymoney());
						}
						walletservice.updateWalletBalance(baWallet);
					}else{
						baWallet.setWalletBalance(curBaDriverGoodsOrder.getGoodspaymoney());
						walletservice.saveWalletBalance(baWallet);
					}
					//保存账单
					String payType = "1"; //退款
					String payAmount = "+"+curBaDriverGoodsOrder.getGoodspaymoney();
					String payDetail = "叭叭物流退还保证金"+curBaDriverGoodsOrder.getGoodspaymoney()+"元";
					String orderStatus= "超时30分钟自动取消订单";
					boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
					if(saveBillFlag){
						logger.info("保存账单成功!");
					}
				}
				//保存【交易成功环节订单】
		    	boolean createOrderStepsFlag = createBaDriverGoodsOrderSteps(curBaDriverGoodsOrder, "10", "交易关闭", "司机已超过30分钟未处理订单，订单取消。若支付了保证金，保证金将退回到钱包");
				if(createOrderStepsFlag){
					logger.info("保存订单环节成功!");
				}else{
					logger.info("保存订单环节失败!");
				}
		    	//更新流程为交易关闭
				curBaDriverGoodsOrder.setOrderStatus("10");
				boolean flag = badriverorderservice.alertDriverConfirmOrderNextNodeStatus(curBaDriverGoodsOrder);
				if(flag){
					logger.info("交易关闭----->>>流程结束");
				}
	    	}
	    	
	    	/**
			 * 3-经纪人委托司机运输 
			 * author:songqiuming
			 */
			if(orderType.endsWith("3") ){
				//查询经纪人信息
				BaUser baUser =new BaUser();
				BaGoodsInfo curBaGoodsInfo = bareleasegoodsservice.queryBaGoodsInfoById(Integer.parseInt(curBaDriverGoodsOrder.getGoodId()));
				if(null == curBaGoodsInfo){
					continue;
				}
				baUser.setUserid(curBaGoodsInfo.getUserId()); //用户id 经纪人
				BaUser curBaUser = bauserservice.getBaUserById(baUser.getUserid()+"");
				//退还经纪人保证金到经纪人钱包
				if("1".equals(curBaDriverGoodsOrder.getIsagentpaymargin())){
					//查询经纪人钱包余额
					BaWallet baWallet = new BaWallet();
					baWallet.setUserid(curBaUser.getUserid()+"");
					baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
					baWallet.setStatus("2");
					BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
					//更新经纪人钱包余额 
					if(curBaWallet!=null){
						if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
							//经纪人保证金+钱包余额
							baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getAgentpaymoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
						}else{
							//第一次设置
							baWallet.setWalletBalance(curBaDriverGoodsOrder.getAgentpaymoney());
						}
						walletservice.updateWalletBalance(baWallet);
					}else{
						baWallet.setWalletBalance(curBaDriverGoodsOrder.getAgentpaymoney());
						walletservice.saveWalletBalance(baWallet);
					}
					//保存账单
					String payType = "1"; //退款
					String payAmount = "+"+curBaDriverGoodsOrder.getAgentpaymoney();
					String payDetail = "叭叭物流退还保证金"+curBaDriverGoodsOrder.getAgentpaymoney()+"元";
					String orderStatus= "超时30分钟自动取消订单";
					boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
					if(saveBillFlag){
						logger.info("保存账单成功!");
					}
				}
				//保存【交易成功环节订单】
		    	boolean createOrderStepsFlag = createBaDriverGoodsOrderSteps(curBaDriverGoodsOrder, "10", "交易关闭", "司机已超过30分钟未处理订单，订单取消。若支付了保证金，保证金将退回到钱包");
				if(createOrderStepsFlag){
					logger.info("保存订单环节成功!");
				}else{
					logger.info("保存订单环节失败!");
				}
		    	//更新流程为交易关闭
				curBaDriverGoodsOrder.setOrderStatus("10");
				boolean flag = badriverorderservice.alertDriverConfirmOrderNextNodeStatus(curBaDriverGoodsOrder);
				if(flag){
					logger.info("交易关闭----->>>流程结束");
				}
			}
	    }
	}
	/***
	 * @title
	 * @introduce 货主退款倒计时7天
	 * @author wangkai
	 * @date   2017年03月03日
	 * @return
	 */
	//@Scheduled(cron = "0 0/10 * * * ?") 
	public void confirmRefundExpired(){
		logger.info("confirmRefundExpired" + "----->>>>>货主退款倒计时7天");
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		List<BaOrderManagerDto>  baDriverGoodsOrderList = badriverorderservice.queryConfirmRefundExpired();
	    for(BaOrderManagerDto curBaDriverGoodsOrder : baDriverGoodsOrderList){
	    	createBaDriverGoodsOrderSteps(curBaDriverGoodsOrder, "9", "交易纠纷", "交易纠纷");
	    	curBaDriverGoodsOrder.setOrderStatus("9");
			boolean flag = badriverorderservice.alertDriverConfirmOrderNextNodeStatus(curBaDriverGoodsOrder);
			System.out.println(curBaDriverGoodsOrder.getOrderId() +  "进入交易纠纷");
	    }
	}
	/***
	 * @title
	 * @introduce 司机装货倒计时7天
	 * @author wangkai
	 * @date   2017年03月03日
	 * @return
	 */
	//@Scheduled(cron = "0 0/10 * * * ?") 
	public void confirmDriverNotLoadingGoods(){
		logger.info("confirmDriverNotLoadingGoods" + "----->>>>>经纪人装货倒计时7天");
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		List<BaOrderManagerDto>  baDriverGoodsOrderList = badriverorderservice.queryDriverLoadingGooodsExpired();
	    for(BaOrderManagerDto curBaDriverGoodsOrder : baDriverGoodsOrderList){
	    	//1,若司机有缴纳保证金,退换保证金
			if("1".equals(curBaDriverGoodsOrder.getIsdriverpaymargin())){
				//司机保证金转入司机钱包
				BaUser baUser  = new BaUser();
				baUser.setUserid(Integer.parseInt(curBaDriverGoodsOrder.getDriverId()));
				BaUser curBaUser = bauserservice.getBaUserById(baUser.getUserid()+"");
				//查询当前钱包余额
				BaWallet baWallet = new BaWallet();
				baWallet.setUserid(curBaUser.getUserid()+"");
				baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
				baWallet.setStatus("2");
				BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
				//3,更新司机钱包余额 
				if(curBaWallet!=null){
					if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
						//司机保证金+钱包余额
						baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getDriverpaymoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
					}else{
						//第一次设置
						baWallet.setWalletBalance(curBaDriverGoodsOrder.getDriverpaymoney());
					}
					walletservice.updateWalletBalance(baWallet);
				}else{
					baWallet.setWalletBalance(curBaDriverGoodsOrder.getDriverpaymoney());
					walletservice.saveWalletBalance(baWallet);
				}
				String payType = "1"; //退款
				String payAmount = "+"+curBaDriverGoodsOrder.getDriverpaymoney();
				String payDetail = "【叭叭物流】退入保证金 :"+curBaDriverGoodsOrder.getDriverpaymoney()+"元";
				String orderStatus= "司机确认装货";
				boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
				logger.info("保存账单成功!");
			}
			//若经纪人缴纳了保证金
			if("1".equals(curBaDriverGoodsOrder.getIsagentpaymargin())){
				//保证金转入经纪人钱包
				int userId = bareleasegoodsservice.queryBaGoodsInfoById(Integer.parseInt(curBaDriverGoodsOrder.getGoodId())).getUserId(); //经纪人id
				BaUser baUser  = new BaUser();
				baUser.setUserid(userId);
				BaUser curBaUser = bauserservice.getBaUserById(baUser.getUserid()+"");
				//查询经纪人钱包余额
				BaWallet baWallet = new BaWallet();
				baWallet.setUserid(curBaUser.getUserid()+"");
				baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
				baWallet.setStatus("2");
				BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
				//更新经纪人钱包余额 
				if(curBaWallet!=null){
					if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
						//经纪人保证金+钱包余额
						baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getAgentpaymoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
					}else{
						//第一次设置
						baWallet.setWalletBalance(curBaDriverGoodsOrder.getAgentpaymoney());
					}
					//更新钱包余额
					walletservice.updateWalletBalance(baWallet);
				}else{
					//第一次设置钱包余额
					baWallet.setWalletBalance(curBaDriverGoodsOrder.getAgentpaymoney());
					walletservice.saveWalletBalance(baWallet);
				}
				//保存经纪人账单
				String payType = "1"; //退款
				String payAmount = "+"+curBaDriverGoodsOrder.getAgentpaymoney();
				String payDetail = "【叭叭物流】退入保证金 :"+curBaDriverGoodsOrder.getAgentpaymoney()+"元";
				String orderStatus= "司机确认装货";
				boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
				logger.info("保存账单成功!");
			}
			//2,生成经纪人信息费账单数据
			int userId = bareleasegoodsservice.queryBaGoodsInfoById(Integer.parseInt(curBaDriverGoodsOrder.getGoodId())).getUserId(); //经纪人id
			BaUser baUser  = new BaUser();
			baUser.setUserid(userId);
			BaUser curBaUser = bauserservice.getBaUserById(baUser.getUserid()+"");
			//查询经纪人当前钱包
			BaWallet baWallet = new BaWallet();
			baWallet.setUserid(curBaUser.getUserid()+"");
			baWallet.setUserType(Integer.parseInt(curBaUser.getUsertype()));
			baWallet.setStatus("2");
			BaWallet curBaWallet = walletservice.queryWalletBalance(baWallet);
			//3,更新经纪人钱包余额 
			if(curBaWallet!=null){
				if(StringUtils.isNotEmpty(curBaWallet.getWalletBalance())){
					//信息费+钱包余额
					baWallet.setWalletBalance(decimalFormat.format(Double.parseDouble(curBaDriverGoodsOrder.getInformationmoney()) + Double.parseDouble(curBaWallet.getWalletBalance())) +"");//更新余额
				}else{
					//第一次设置
					baWallet.setWalletBalance(curBaDriverGoodsOrder.getInformationmoney());
				}
				walletservice.updateWalletBalance(baWallet);
			}else{
				baWallet.setWalletBalance(curBaDriverGoodsOrder.getInformationmoney());
				walletservice.saveWalletBalance(baWallet);
			}
			//4,保存账单
			String payType = "4"; //收入
			String payAmount = "+"+curBaDriverGoodsOrder.getInformationmoney();
			BaUser driverUser = new BaUser();
		    driverUser.setUserid(Integer.parseInt(curBaDriverGoodsOrder.getDriverId())); 
			String payDetail = "司机【"+ bauserservice.getBaUserById(curBaDriverGoodsOrder.getDriverId()).getRealname()+"】转入信息费"+curBaDriverGoodsOrder.getInformationmoney()+"元";
			String orderStatus= "司机确认装货";
			boolean saveBillFlag = createBaWalletBill(curBaUser,curBaDriverGoodsOrder,baWallet,payType,payAmount,payDetail,orderStatus);
			logger.info("保存账单成功!");
			//5,更新流程为交易成功
			curBaDriverGoodsOrder.setOrderStatus("8");
			boolean flag = badriverorderservice.alertDriverConfirmOrderNextNodeStatus(curBaDriverGoodsOrder);
			//6,更新【司机确认装货时间】
	    	boolean updateBaOrderStepsTimeFlag = updateBaOrderStepsTime(curBaDriverGoodsOrder,"2");
	        //7,保存【交易成功环节订单】
	    	boolean createOrderStepsFlag = createBaDriverGoodsOrderSteps(curBaDriverGoodsOrder, "8", "交易成功", "司机已到达收货地并确认收货,交易成功");
			if(createOrderStepsFlag){
				logger.info("保存订单环节成功!");
			}else{
				logger.info("保存订单环节失败!");
			}
	    }
	}
}