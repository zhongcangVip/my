package com.bbsuper.core;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.LoggerFactory;

import com.bbsuper.model.scheduler.TaskManager;
import com.bbsuper.model.scheduler.enums.TaskRunningStatusEnum;

/**
 * 任务管理器
 * @author yinyuqiao
 * 2017年6月22日 下午2:06:56
 */
public class TaskOperator {
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(TaskOperator.class);
	
	/**
	 * 开始任务
	 * @param taskManager
	 * @throws Exception
	 */
	public static void startTask(TaskManager taskManager) throws Exception {
		JobKey jobKey = new JobKey(taskManager.getId() + "");
		TriggerKey triggerKey = new TriggerKey(taskManager.getId() + "");
		// 刪除任务
		deleteTask(taskManager);

		JobDetail jobDetail = JobBuilder.newJob(TaskProxy.class).withIdentity(jobKey).build();
		try {
			CronTrigger trigger = (CronTrigger) TaskScheduler.getScheduler().getTrigger(triggerKey);
			// 设置执行过期
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(taskManager.getParam());
			trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
			// 注册作业和触发器
			TaskScheduler.getScheduler().scheduleJob(jobDetail, trigger);
			// 启动
			TaskScheduler.getScheduler().start();
		} catch (SchedulerException e) {
			logger.error("启动定时任务失败", e);
			TaskScheduler.setTaskStatusMap(taskManager.getId(), TaskRunningStatusEnum.STOP);
			
			throw new Exception(e);
		}
	}
	
	/**
	 * 停止任务
	 */
	public static void stopTask(TaskManager taskManager) throws Exception {
		JobKey jobKey = new JobKey(taskManager.getId() + "");
		
		try {
			TaskScheduler.getScheduler().pauseJob(jobKey);
			TaskScheduler.setTaskStatusMap(taskManager.getId(), TaskRunningStatusEnum.STOP);
		} catch (SchedulerException e) {
			logger.error("停止定时任务失败", e);
			
			throw new Exception(e);
		}
	}
	
	/**
	 * 刪除任务
	 */
	public static void deleteTask(TaskManager taskManager) throws Exception {
		JobKey jobKey = new JobKey(taskManager.getId() + "");
		TriggerKey triggerKey = new TriggerKey(taskManager.getId() + "");
		
		try {
			// 停止计划任务
			TaskScheduler.getScheduler().unscheduleJob(triggerKey);
			TaskScheduler.getScheduler().deleteJob(jobKey);
			
			// 刪除任务
			TaskScheduler.removeTaskStatusMapById(taskManager.getId());
		} catch (SchedulerException e) {
			logger.error("刪除定时任务失败", e);
			
			throw new Exception(e);
		}
	}
	
	

}
