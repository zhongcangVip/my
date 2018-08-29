Package("com.bbsuper.taskmanager.taskManagerList");
com.bbsuper.taskmanager.taskManagerList = {	
	init : function() {

	},
	
	viewErrorInfo : function(taskId){
		art.dialog.open(ctx + '/taskmanager/viewErrorInfo?id=' + taskId, {
			id : 'taskErrorInfoList',
			title : '查看异常信息',
			lock : true,
			cancel : true,
			resize : true,
			padding : 0,
			width : 1000,
			height : 800
		});
	},
	
	saveOrUpdateTask : function(taskId) {
		if (taskId && taskId != 0) {
			art.dialog.open(ctx + '/taskmanager/toSaveOrUpdate?id=' + taskId, {
				id : 'taskManagerListUpd',
				title : '修改任务信息',
				ok : function(subWindow) {
					subWindow.saveOrUpdate();
					return false;
				},
				okVal : "保存",
				lock : true,
				cancel : true,
				resize : true,
				padding : 0,
				width : 580,
				height : 200
			});
		} else {
			art.dialog.open(ctx + '/taskmanager/toSaveOrUpdate', {
				id : 'taskManagerListAdd',
				title : '添加任务信息',
				ok : function(subWindow) {
					subWindow.saveOrUpdate();
					return false;
				},
				okVal : "保存",
				lock : true,
				cancel : true,
				resize : true,
				padding : 0,
				width : 580,
				height : 200
			});
		}
	},
	
	deleteTask : function(taskId){
		art.dialog.confirm("确定删除?", function() {
			$.ajax({
				 type:"POST",
				 url:ctx+"/taskmanager/deleteTask",
				 data:{"id":taskId},
				 success:function(data){
					 data = $.parseJSON(data);
					 if(data.flag) {
					    art.dialog.tips('刪除成功');
					 }else {
						 art.dialog.tips('刪除失败');
					 }
				 }
			 });
		});
	},
	
	startTask : function(taskId){
		$.ajax({
			 type:"POST",
			 url:ctx+"/taskmanager/startTask",
			 data:{"id":taskId},
			 success:function(data){
				 data = $.parseJSON(data);
				 if(data.flag) {
				    art.dialog.tips('启动任务成功');
				 }else{
					 art.dialog.tips('启动任务失敗,' + data.errorMsg);
				 }
			 }
		});
	},
	
	startForOneTask:function(taskId){
		art.dialog.confirm("首先确保该定时器今日未执行成功，确定运行该定时器？",function(){
			$.ajax({
				 type:"POST",
				 url: ctx+"/taskmanager/startForOneTask",
				 data:{"id":taskId},
				 success:function(data){
					 data = $.parseJSON(data);
					 if(data.flag) {
					    art.dialog.tips('运行任务成功');
					 }else{
						 art.dialog.tips('运行任务失敗');
					 }
				}
			});
		});
		
	},	
	stopJob : function(taskId){
		$.ajax({
			 type:"POST",
			 url:ctx+"/taskmanager/stopTask",
			 data:{"id":taskId},
			 success:function(data){
				 data = $.parseJSON(data);
				 if(data.flag) {
				    art.dialog.tips('停止任务成功');
				 }else{
					 art.dialog.tips('停止任务失败,' + data.errorMsg);
				 }
			}
		});
	},
	
	disableTask : function(taskId){
		$.ajax({
			 type:"POST",
			 url:ctx+"/taskmanager/changeStatus",
			 data:{"id":taskId,"status":"DISABLE"},
			 success:function(data){
				 data = $.parseJSON(data);
				 if(data.flag) {
				    art.dialog.tips('禁用任务成功');
				 }else{
					 art.dialog.tips('禁用任务失败,' + data.errorMsg);
				 }
			}
		});
	},
	
	enableTask : function(taskId){
		$.ajax({
			 type:"POST",
			 url:ctx+"/taskmanager/changeStatus",
			 data:{"id":taskId,"status":"ENABLE"},
			 success:function(data){
				 data = $.parseJSON(data);
				 if(data.flag) {
				    art.dialog.tips('启用任务成功');
				 }else{
					 art.dialog.tips('启用任务失败,' + data.errorMsg);
				 }
			}
		});
	}
	
}
