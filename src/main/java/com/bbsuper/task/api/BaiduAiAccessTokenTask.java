package com.bbsuper.task.api;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.bbsuper.service.api.base.AiTokenService;

/**
 * 定时更新百度ai token
 * @author yinyuqiao
 * 2017年8月28日 上午9:46:43
 */
@Component
public class BaiduAiAccessTokenTask {
    private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(BaiduAiAccessTokenTask.class);
    @Autowired
    private AiTokenService aiTokenService;

    /**
     * 定时更新百度ai token
     */
    public void exectue() {
        logger.info("BaiduAiAccessTokenTask start ...");
        try {
            logger.info("BaiduAiAccessTokenTask running ...");
            aiTokenService.update(); //更新token
        } catch (Exception e) {
            logger.error("BaiduAiAccessTokenTask exception", e);
        }
        logger.info("BaiduAiAccessTokenTask end ...");
    }

}
