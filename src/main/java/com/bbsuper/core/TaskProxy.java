package com.bbsuper.core;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.bbsuper.model.scheduler.TaskErrorInfo;
import com.bbsuper.model.scheduler.TaskManager;
import com.bbsuper.model.scheduler.enums.TaskRunningStatusEnum;
import com.bbsuper.service.scheduler.TaskErrorInfoService;
import com.bbsuper.service.scheduler.TaskManagerService;

/**
 * 任务启动代理类
 * @author yinyuqiao
 * 2017年6月22日 下午2:06:14
 */
@Component
public class TaskProxy implements Job{
	@Autowired
	private TaskManagerService taskManagerService;
	@Autowired
	private TaskErrorInfoService taskErrorInfoService;

	//执行
	public void execute(JobExecutionContext context) throws JobExecutionException {
		 // 使得job对象可以通过注解实现依赖注入
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		// 执行
		executeTask(context);
	}

	/**
	 * 执行任务
	 * @param context
	 * @throws Exception
	 */
	private void executeTask(JobExecutionContext context) {
		JobKey jobKey = context.getJobDetail().getKey();
		String id = jobKey.getName();
		// 通过ID查询
		TaskManager taskManager = taskManagerService.getTaskManagerById(Integer.parseInt(id));
		
		try {
			excuteByReflection(taskManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 利用反射执行定时方法
	 * @param taskManager
	 */
	public void excuteByReflection(TaskManager taskManager) throws Exception {
		try {
			// 利用反射获取定时方法
			Class<?> clazz = Class.forName(taskManager.getClassName());
			Method method = clazz.getDeclaredMethod(taskManager.getMethodName());
			
			Object obj = SpringContext.getBean(clazz);
			if (obj == null) {
				return;
			}
			// 设置开始时间
			taskManager.setStartTime(new Date());
			// 更改任务状态
			TaskScheduler.setTaskStatusMap(taskManager.getId(), TaskRunningStatusEnum.RUNNING);
			// 执行
			method.invoke(obj);
		}catch (Exception e) {
			e.printStackTrace();
			// 记录错误日志
			TaskErrorInfo taskErrorInfo = new TaskErrorInfo();
			taskErrorInfo.setCreateTime(new Date());
			taskErrorInfo.setInfo(ExceptionUtils.getFullStackTrace(e));
			taskErrorInfo.setTaskManager(taskManager);
			taskErrorInfoService.insert(taskErrorInfo);
			// 更改任务状态
			TaskScheduler.setTaskStatusMap(taskManager.getId(), TaskRunningStatusEnum.STOP);
			throw new Exception(e);
		} finally {
			// 设置结束时间
			taskManager.setEndTime(new Date());
			taskManagerService.update(taskManager);
			// 更改任务状态
			TaskScheduler.setTaskStatusMap(taskManager.getId(), TaskRunningStatusEnum.WAIT);
		}
	}

}
