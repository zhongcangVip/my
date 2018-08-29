package com.bbsuper.task.vip;


import com.bbsuper.model.vip.enums.SourceCancelReasonEnum;
import com.bbsuper.model.vip.enums.SourceStatusEnum;
import com.bbsuper.service.vip.goodsSource.GoodsSourceService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 超时未支付订单处理
 *
 * @author zengdun
 * 2017年09月25日 17:13
 */
@Component
public class TimeOutSourceHandleTask {

    private static final Logger logger = LoggerFactory.getLogger(TimeOutSourceHandleTask.class);

    @Autowired
    private GoodsSourceService goodsSourceService;

    public void handle() {
        logger.info("TimeOutSourceHandleTask start ...");
        try {
            logger.info("TimeOutSourceHandleTask running ...");
            List<Map<String, Object>> noReleaseSources = goodsSourceService.getNoReleaseSource(10);
            if (noReleaseSources.size() > 0) {
                Map<String, Object> params = Maps.newHashMap();
                params.put("sourceStatus", SourceStatusEnum.CANCEL);
                params.put("cancelType", "1");
                params.put("cancelReason", SourceCancelReasonEnum.TIMEOUT_UNPAID);
                params.put("remark", "付款时间已过期");
                params.put("remarkCancel", "付款时间已过期，货源自动取消");
                for (Map<String, Object> source : noReleaseSources) {
                    int minute = (int) ((System.currentTimeMillis() - ((Date) source.get("createTime")).getTime()) / 1000 / 60);
                    if (minute >= 30) {
                        params.put("id", source.get("id"));
                        params.put("cancelTime", new Date());
                        goodsSourceService.update(params);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("TimeOutSourceHandleTask exception", e);
        }
        logger.info("TimeOutSourceHandleTask end ...");
    }

}
