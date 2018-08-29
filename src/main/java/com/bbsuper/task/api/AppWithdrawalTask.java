package com.bbsuper.task.api;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.common.util.StringUtil;
import com.bbsuper.model.app.AppWithdrawal;
import com.bbsuper.model.vip.enums.PayStatusEnum;
import com.bbsuper.service.api.util.BaofooPayHandler;
import com.bbsuper.service.app.AppBaofooPayService;
import com.bbsuper.service.app.AppWithdrawalService;

/**
 * app用户提现结果查询定时器
 * @author yinyuqiao
 * 2017年12月19日 下午5:31:09
 */
@Component
public class AppWithdrawalTask{
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(AppWithdrawalTask.class);
	@Autowired
	private AppWithdrawalService appWithdrawalService;
	@Autowired
	private AppBaofooPayService appBaofooPayService;
	@Autowired
	private BaofooPayHandler baofooPayHandler;
	
	public void execute(){
		logger.info("task start ...");
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("payStatus", PayStatusEnum.PAYING.toString());
		List<AppWithdrawal> list = appWithdrawalService.getAppWithdrawal(param);
		if(null == list || list.size() == 0){
			return;
		}
		Map<String,Object> result = new HashMap<String, Object>();
		try {
			for(AppWithdrawal withdrawal: list){
				if(StringUtil.isEmpty(withdrawal.getCode())){
					continue;
				}
				String trans_fee = "0"; //提现手续费
				String state = "0"; //状态-0：转账中；1：转账成功；-1：转账失败；2：转账退款；
				String trans_id = withdrawal.getCode(); //宝付商户订单号
				result = appBaofooPayService.queryPaidResult(trans_id);
				if(null != result && null != result.get("code") && result.get("code").toString().equals("200")){
					state = null==result.get("state")?"":result.get("state").toString();
					trans_fee = null==result.get("trans_fee")?"":result.get("trans_fee").toString();
				}
				if(state.equals("1")){
					//支付成功
					baofooPayHandler.dowithAppWalletCash(withdrawal, new BigDecimal(trans_fee), trans_id);
				}else{
					//支付失败
					baofooPayHandler.dowithAppWalletCashFaild(withdrawal);
				}
			}
		} catch (Exception e) {
			logger.error("task exception", e);
		}
		logger.info("task end ...");
		
	}

	
}
