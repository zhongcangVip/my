<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>任务调度</title>
</head>
<body>
<div>
	<div id="Web_right">
	   <input type="hidden" id="taskId" value="<#if (taskManager.id)??>${taskManager.id!''}<#else>0</#if>"/>
	   <div id="mess_list" style="margin:0 0 0 10px;">
	      <table border="0" class="table_noborder">
	       <tr style="height:10px;"></tr>
	       <tr>
	          <td>
	          	任务名称:
	          </td>
	          <td  colspan="2">
	          	 <input id="name" name="name" style="width:280px;"
	            	 value="${(taskManager.name)!''}"/>
	          </td>
	      </tr>
	      <tr style="height:10px;"></tr>
	      <tr>
	          <td>
	          	执行时间参数:
	          </td>
	          <td  colspan="2">
	            <input id="param" name="param" style="width:280px;"
	             	value="${(taskManager.param)!''}"/>(Cron表达式)
	          </td>
	      </tr>
	      <tr style="height:10px;"></tr>
		    <tr>
		          <td>
		          	执行类名称:
		          </td>
		          <td  colspan="2">
		            <input id="className" name="className" style="width:280px;"
		             	value="${(taskManager.className)!''}"/>(包名加类名)
		          </td>
		      </tr>
		      <tr style="height:10px;"></tr>
		    <tr>
		        <td>
		          	执行方法名称：
		        </td>
		        <td  colspan="2">
	          		 <input id="methodName" name="methodName" style="width:280px;"
	             	value="${(taskManager.methodName)!''}"/>
		        </td>
		   </tr>
	      </table>
	   </div>
	</div>  
</div>
<div class="clearFix"></div>
</div>
</body>
</html>
<script type="text/javascript" src="${base}/scripts/plugin/jquery.min.js"></script>
<script type="text/javascript" src="${base}/scripts/common/core.js"></script>
<script type="text/javascript" src="${base}/scripts/common/InitArtDialog.js"></script>
<script type="text/javascript" src="${base}/scripts/plugin/artDialog/plugins/iframeTools.source.js"></script>
	<script type="text/javascript" >
	      function saveOrUpdate(){
	      		var taskManager = {};
				var taskId = $("#taskId").val();
				if(taskId != "0") {
					taskManager.id = taskId;
				}
				var name = $("#name").val();
				if(name && name.length < 50 ){
					taskManager.name = name;
				}else{
					art.dialog.tips("任务名称不能为空且不能超过50个字符!");
					return false;
				}
				
				var param = $("#param").val();
				if(param && param.length < 50 ){
					taskManager.param = param;
				}else{
					art.dialog.tips("执行时间参数不能为空且不能超过50个字符!");
					return false;
				}
				
				var className = $("#className").val();
				if(className && className.length < 100 ){
					taskManager.className = className;
				}else{
					art.dialog.tips("执行类名称不能为空且不能超过100个字符!");
					return false;
				}
				
				var methodName = $("#methodName").val();
				if(methodName && methodName.length < 50 ){
					taskManager.methodName = methodName;
				}else{
					art.dialog.tips("执行方法名称不能为空且不能超过50个字符!");
					return false;
				}
				
				$.ajax({
					 type:"POST",
					 url: ctx+"/taskmanager/saveOrUpdate",
					 data: taskManager,
					 dataType: "json",
					 success: function(data){
						 if(data.flag){
						    art.dialog.tips('保存成功');
						    art.dialog.close();
						 }else{
							 art.dialog.tips(data.errorMsg);
						 }
					 }
				 });
		   }
	</script>
