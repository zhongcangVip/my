package com.bbsuper.task.manager;


import com.bbsuper.common.Constants;
import com.bbsuper.common.util.ResultUtil;
import com.bbsuper.common.util.SendMessageUtil;
import com.bbsuper.model.manager.GroupMessage;
import com.bbsuper.service.manager.GroupMessageService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 短信群发
 *
 * @author zengdun
 * 2017年11月21日 17:41
 */
@Component
public class SendGroupMessageTask {

    private static final Logger logger = LoggerFactory.getLogger(SendGroupMessageTask.class);

    @Autowired
    private GroupMessageService groupMessageService;

    public void handle() {
        logger.info("SendGroupMessageTask start ...");
        try {
            logger.info("SendGroupMessageTask running ...");
            int count = SendMessageUtil.queryBalance();
            if (count <= Constants.MIN_MESSAGE_COUNT) {
                logger.info("短信余额不足" + Constants.MIN_MESSAGE_COUNT);
            } else {
                List<GroupMessage> list = groupMessageService.getTimingMessage();
                Map<String, Object> params = Maps.newHashMap();
                for (GroupMessage groupMessage : list) {
                    count = SendMessageUtil.queryBalance();
                    if (count <= Constants.MIN_MESSAGE_COUNT) {
                        logger.info("短信余额不足" + Constants.MIN_MESSAGE_COUNT);
                        break;
                    }
                    int result = SendMessageUtil.sendMessage(groupMessage.getMobiles(), groupMessage.getContent());
                    if (result == 200) {
                        params.put("pushStatus", 2);
                    } else {
                        params.put("pushStatus", 3);
                    }
                    params.put("id", groupMessage.getId());
                    params.put("pushTime", new Date());
                    groupMessageService.update(params);
                }
            }
        } catch (Exception e) {
            logger.error("SendGroupMessageTask exception", e);
        }
        logger.info("SendGroupMessageTask end ...");
    }

}
