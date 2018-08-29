/**
 * 服務接口
 * 
 */
Package("com.bbsuper.common.service");
com.bbsuper.common.service = {
	setting: {
		type: "POST",
		dataType: "json",
		timeout: 90000,
		beforeSend: this.beforeSend,
		complete: this.complete,
		success: this.success,
		error: this.error,
		async: true,//默認:true 所有請求均為異步請求。
		cache: true//默認:true，設置為false將不會從瀏覽器緩存中加載請求信息。
	},
	send: function(config, options){
		var finalOptions = {};
		if (typeof(config) === 'string') {
			finalOptions.url = config;
		} else {
			finalOptions = config;
		}
		jQuery.extend(finalOptions, com.bbsuper.common.service.setting, options);
		jQuery.ajax(finalOptions);
	},
	beforeSend: function(){
		
	},
	complete: function(){
		
	},
	success: function(json){
		
	},
	error: function(){
		
	}
};
