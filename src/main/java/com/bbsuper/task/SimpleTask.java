package com.bbsuper.task;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 定时器示例
 * @author yinyuqiao
 * 2017年6月22日 下午2:05:04
 */
@Component
public class SimpleTask{
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(SimpleTask.class);
	
	public void execute(){
		logger.info("task start ...");
		try {
			logger.info("task running...");
		} catch (Exception e) {
			logger.error("task exception", e);
		}
		logger.info("task end ...");
		
	}

	
}
