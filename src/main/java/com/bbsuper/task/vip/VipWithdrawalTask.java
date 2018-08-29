package com.bbsuper.task.vip;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.common.util.StringUtil;
import com.bbsuper.model.vip.Withdrawal;
import com.bbsuper.model.vip.enums.PayStatusEnum;
import com.bbsuper.service.vip.VipBaofooPayService;
import com.bbsuper.service.vip.WithdrawalService;
import com.bbsuper.service.vip.handler.VipBaofooPayHandler;

/**
 * vip用户提现结果查询定时器
 * @author yinyuqiao
 * 2017年10月18日 下午2:33:55
 */
@Component
public class VipWithdrawalTask{
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(VipWithdrawalTask.class);
	@Autowired
	private WithdrawalService withdrawalService;
	@Autowired
	private VipBaofooPayService vipBaofooPayService;
	@Autowired
	private VipBaofooPayHandler vipBaofooPayHandler;
	
	public void execute(){
		logger.info("task start ...");
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("payStatus", PayStatusEnum.PAYING.toString());
		List<Withdrawal> list = withdrawalService.getWithdrawal(param);
		if(null == list || list.size() == 0){
			return;
		}
		Map<String,Object> result = new HashMap<String, Object>();
		try {
			for(Withdrawal withdrawal: list){
				if(StringUtil.isEmpty(withdrawal.getCode())){
					continue;
				}
				String trans_fee = "0"; //提现手续费
				String state = "0"; //状态-0：转账中；1：转账成功；-1：转账失败；2：转账退款；
				String trans_id = ""; //宝付商户订单号
				result = vipBaofooPayService.queryPaidResult(withdrawal.getCode());
				if(null != result && null != result.get("code") && result.get("code").toString().equals("200")){
					state = null==result.get("state")?"":result.get("state").toString();
					trans_fee = null==result.get("trans_fee")?"":result.get("trans_fee").toString();
					trans_id = null==result.get("trans_no")?"":result.get("trans_no").toString();
				}
				if(state.equals("1")){
					//支付成功
					vipBaofooPayHandler.dowithWalletCash(withdrawal, new BigDecimal(trans_fee), trans_id);
				}else{
					//支付失败
					vipBaofooPayHandler.dowithWalletCashFaild(withdrawal);
				}
			}
		} catch (Exception e) {
			logger.error("task exception", e);
		}
		logger.info("task end ...");
		
	}

	
}
