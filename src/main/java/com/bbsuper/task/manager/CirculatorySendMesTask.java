package com.bbsuper.task.manager;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.common.Constants;
import com.bbsuper.common.util.SendMessageUtil;
import com.bbsuper.model.manager.CyclicMessage;
import com.bbsuper.service.manager.GroupMessageService;
import com.google.common.collect.Maps;

/**
 * 循环发送信息
 *
 * @author pengjiang
 * 2018年04月17日 14:17
 */
@Component
public class CirculatorySendMesTask {

    private static final Logger logger = LoggerFactory.getLogger(CirculatorySendMesTask.class);

    @Autowired
    private GroupMessageService groupMessageService;

    public void sendMessage() {
        logger.info("sendMessage start ...");
        try {
            logger.info("CirculatorySendMessageTask running ...");
            int count = SendMessageUtil.queryBalance();
            if (count <= Constants.MIN_MESSAGE_COUNT) {
                logger.info("短信余额不足" + Constants.MIN_MESSAGE_COUNT);
            } else {
            	SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            	SimpleDateFormat formatHH = new SimpleDateFormat("HH");
        		Date date = new Date();
        		String time = format.format(date);
        		int min  = Integer.valueOf(time.substring(time.length()-2));
        		logger.info("sendMessage running ...min:"+min);
            	Map<String, Object> data = Maps.newHashMap();
            	data.put("timingTime", getWeekDay());
            	data.put("time", formatHH.format(date)+":");
            	//如果整点跑 则查询整点和上个小时的时间
            	if(min==0) {
            		data.put("time", time);
            		int topTime = Integer.valueOf(formatHH.format(date));
            		data.put("topTime", String.valueOf(topTime-1)+":");
            		//过滤上一个整点小时（如：15:00）
            		data.put("topTimeHH", String.valueOf(topTime-1)+":00");
            	}
                List<CyclicMessage> list = groupMessageService.getCyclicTimingMessage(data);
                logger.info("sendMessage running ...list:"+list.size());
                for (CyclicMessage cyclicMessage : list) {
                	count = SendMessageUtil.queryBalance();
                    if (count <= Constants.MIN_MESSAGE_COUNT) {
                        logger.info("短信余额不足" + Constants.MIN_MESSAGE_COUNT);
                        break;
                    }
                	//校验是否在半小时内
                	String timngTime = cyclicMessage.getTimingTime();
                	int timngTimemin  = Integer.valueOf(timngTime.substring(timngTime.length()-2));
                	logger.info("sendMessage running ...list min:"+min);
                	if (min==0) {
                		logger.info("sendMessage running ...list timngTimemin:"+timngTimemin);
	            		if(timngTimemin>30 && timngTimemin<=59 || timngTimemin==0) {
	            			logger.info("sendMessage running ...list timngTimemin2:"+timngTimemin);
	            			int result = SendMessageUtil.sendMessage(cyclicMessage.getMobiles(), cyclicMessage.getContent());
	            			sendMessage(cyclicMessage, result);
	            		}
            		}else{
            			logger.info("sendMessage running ... timngTimemin:"+timngTimemin);
            			//循环发送的时间
                		if(timngTimemin>0 && timngTimemin<=30) {
                			logger.info("sendMessage running ... timngTimemin:"+timngTimemin);
                			int result = SendMessageUtil.sendMessage(cyclicMessage.getMobiles(), cyclicMessage.getContent());
                			sendMessage(cyclicMessage, result);
                		}
                	}
                }
            }
        } catch (Exception e) {
            logger.error("sendMessage exception", e);
        }
        logger.info("sendMessage end ...");
    }

    private void sendMessage(CyclicMessage cyclicMessage, int result) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("title", cyclicMessage.getTitle());
        params.put("content", cyclicMessage.getContent());
        params.put("mobiles", cyclicMessage.getMobiles());
        params.put("pushCount", cyclicMessage.getPushCount());
        //2:成功  3:失败
        params.put("pushStatus", result == 200?"2":"3");
        params.put("pushTime", null);
        params.put("timingStatus", "2");
        params.put("timingTime", new Date());
        params.put("publisherId", cyclicMessage.getPublisherId());
        params.put("publisherName", cyclicMessage.getPublisherName());
        params.put("createTime", new Date());
        params.put("authStatus", false);
        //发送成功或失败都写入群发表中
        groupMessageService.insert(params);
    }
    
	private String getWeekDay() {
		Date today = new Date();
		Calendar c=Calendar.getInstance();
		c.setTime(today);
		int week=c.get(Calendar.DAY_OF_WEEK)-1;
		String weekDay="";
		switch(week) {
		    case 0:weekDay = "周日";
		    return weekDay;
		    case 1:weekDay = "周一";
		    return weekDay;
		    case 2:weekDay = "周二";
		    return weekDay;
		    case 3:weekDay = "周三";
		    return weekDay;
		    case 4:weekDay = "周四";
		    return weekDay;
		    case 5:weekDay = "周五";
		    return weekDay;
		    case 6:weekDay = "周六";
		    return weekDay;
		}
		return weekDay;
	}

}
