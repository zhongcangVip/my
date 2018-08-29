/*!
 * artDialog iframeTools
 * Date: 2011-11-25 13:54
 * http://code.google.com/p/artdialog/
 * (c) 2009-2011 TangBin, http://www.planeArt.cn
 *
 * This is licensed under the GNU LGPL, version 2.1 or later.
 * For details, see: http://creativecommons.org/licenses/LGPL/2.1/
 */
;(function ($, window, artDialog, undefined) {

var _topDialog, _proxyDialog, _zIndex,
	_data = '@ARTDIALOG.DATA',
	_open = '@ARTDIALOG.OPEN',
	_opener = '@ARTDIALOG.OPENER',
	_winName = window.name = window.name
	|| '@ARTDIALOG.WINNAME' + (new Date).getTime(),
	_isIE6 = window.VBArray && !window.XMLHttpRequest;

$(function () {
	!window.jQuery && document.compatMode === 'BackCompat'
	// 不支持怪異模式，請用主流的XHTML1.0或者HTML5的DOCTYPE申明
	&& alert('artDialog Error: document.compatMode === "BackCompat"');
});
	
	
/** 獲取 artDialog 可跨級調用的最高層的 window 對像 */
var _top = artDialog.top = function () {
	var top = window,
	test = function (name) {
		try {
			var doc = window[name].document;	// 跨域|無權限
			doc.getElementsByTagName; 			// chrome 本地安全限制
		} catch (e) {
			return false;
		};
		
		return window[name].artDialog
		// 框架集無法顯示第三方元素
		&& doc.getElementsByTagName('frameset').length === 0;
	};
	
	if (test('top')) {
		top = window.top;
	} else if (test('parent')) {
		top = window.parent;
	};
	
	return top;
}();
artDialog.parent = _top; // 兼容v4.1之前版本，未來版本將刪除此


_topDialog = _top.artDialog;


// 獲取頂層頁面對話框疊加值
_zIndex = function () {
	return _topDialog.defaults.zIndex;
};



/**
 * 跨框架數據共享接口
 * @see		http://www.planeart.cn/?p=1554
 * @param	{String}	存儲的數據名
 * @param	{Any}		將要存儲的任意數據(無此項則返回被查詢的數據)
 */
artDialog.data = function (name, value) {
	var top = artDialog.top,
		cache = top[_data] || {};
	top[_data] = cache;
	
	if (value !== undefined) {
		cache[name] = value;
	} else {
		return cache[name];
	};
	return cache;
};


/**
 * 數據共享刪除接口
 * @param	{String}	刪除的數據名
 */
artDialog.removeData = function (name) {
	var cache = artDialog.top[_data];
	if (cache && cache[name]) delete cache[name];
};


/** 跨框架普通對話框 */
artDialog.through = _proxyDialog = function () {
	var api = _topDialog.apply(this, arguments);
	var options=arguments[0]||{};
	// 因為iframe註銷後也會從內存中刪除其創建的對象，這樣可以防止回調函數報錯
	if (_top !== window) artDialog.list[api.config.id] = api;
	 
	return api;
};

// 框架頁面卸載前關閉所有穿越的對話框
_top !== window && $(window).bind('unload', function () {
	var list = artDialog.list, config;
	for (var i in list) {
		if (list[i]) {
			config = list[i].config;
			if (config) config.duration = 0; // 取消動畫
			list[i].close();
			delete list[i];
		};
	};
});


/**
 * 彈窗 (iframe)
 * @param	{String}	地址
 * @param	{Object}	配置參數. 這裡傳入的回調函數接收的第1個參數為iframe內部window對像
 * @param	{Boolean}	是否允許緩存. 默認true
 */
artDialog.open = function (url, options, cache) {
	options = options || {};
	var api, DOM,
		$content, $main, iframe, $iframe, $idoc, iwin, ibody,
		top = artDialog.top,
		initCss = 'position:absolute;left:-9999em;top:-9999em;border:none 0;background:transparent',
		loadCss = 'width:100%;height:100%;border:none 0';
		
	if (cache === false) {
		var ts = (new Date()).getTime(),
			ret = url.replace(/([?&])_=[^&]*/, "$1_=" + ts );
		url = ret + ((ret === url) ? (/\?/.test(url) ? "&" : "?") + "_=" + ts : "");
	};
		
	var load = function () {
		var iWidth, iHeight,
			loading = DOM.content.find('.aui_loading'),
			aConfig = api.config;
			
		$content.addClass('aui_state_full');
		
		loading && loading.hide();
		
		try {
			iwin = iframe.contentWindow;
			$idoc = $(iwin.document);
			ibody = iwin.document.body;
		} catch (e) {// 跨域
			iframe.style.cssText = loadCss;
			
			aConfig.follow
			? api.follow(aConfig.follow)
			: api.position(aConfig.left, aConfig.top);
			
			options.init && options.init.call(api, iwin, top);
			options.init = null;
			return;
		};
		
		// 獲取iframe內部尺寸
		iWidth = aConfig.width === 'auto'
		? $idoc.width() + (_isIE6 ? 0 : parseInt($(ibody).css('marginLeft')))
		: aConfig.width;
		
		iHeight = aConfig.height === 'auto'
		? $idoc.height()
		: aConfig.height;
		//不能大於900
		iHeight=iHeight>850?850:iHeight;
		// 適應iframe尺寸
		setTimeout(function () {
			iframe.style.cssText = loadCss;
		}, 0);// setTimeout: 防止IE6~7對話框樣式渲染異常
		api.size(iWidth, iHeight);
		
		// 調整對話框位置
		aConfig.follow
		? api.follow(aConfig.follow)
		: api.position(aConfig.left, aConfig.top);
		
		
		options.init && options.init.call(api, iwin, top);
		options.init = null;
	};
		
	var config = {
		zIndex: _zIndex(),
		init: function () {
			api = this;
			DOM = api.DOM;
			$main = DOM.main;
			$content = DOM.content;
			
			iframe = api.iframe = top.document.createElement('iframe');
			iframe.src = url;
			iframe.name = 'Open' + api.config.id;
			iframe.style.cssText = initCss;
			iframe.setAttribute('frameborder', 0, 0);
			iframe.setAttribute('allowTransparency', true);
			
			$iframe = $(iframe);
			api.content().appendChild(iframe);
			iwin = iframe.contentWindow;
			
			try {
				iwin.name = iframe.name;
				artDialog.data(iframe.name + _open, api);
				artDialog.data(iframe.name + _opener, window);
			} catch (e) {};
			
			$iframe.bind('load', load);
		},
		close: function () {
			$iframe.css('display', 'none').unbind('load', load);
			
			if (options.close && options.close.call(this, iframe.contentWindow, top) === false) {
				return false;
			};
			$content.removeClass('aui_state_full');
			
			// 重要！需要重置iframe地址，否則下次出現的對話框在IE6、7無法聚焦input
			// IE刪除iframe後，iframe仍然會留在內存中出現上述問題，置換src是最容易解決的方法
			$iframe[0].src = 'about:blank';
			$iframe.remove();
			
			try {
				artDialog.removeData(iframe.name + _open);
				artDialog.removeData(iframe.name + _opener);
			} catch (e) {};
		}
	};
	
	// 回調函數第一個參數指向iframe內部window對像
	if (typeof options.ok === 'function') config.ok = function () {
		return options.ok.call(api, iframe.contentWindow, top);
	};
	if (typeof options.cancel === 'function') config.cancel = function () {
		return options.cancel.call(api, iframe.contentWindow, top);
	};
	
	delete options.content;

	for (var i in options) {
		if (config[i] === undefined) config[i] = options[i];
	};
	
	return _proxyDialog(config);
};


/** 引用open方法擴展方法(在open打開的iframe內部私有方法) */
artDialog.open.api = artDialog.data(_winName + _open);


/** 引用open方法觸發來源頁面window(在open打開的iframe內部私有方法) */
artDialog.opener = artDialog.data(_winName + _opener) || window;
artDialog.open.origin = artDialog.opener; // 兼容v4.1之前版本，未來版本將刪除此

/** artDialog.open 打開的iframe頁面裡關閉對話框快捷方法 */
artDialog.close = function () {
	var api = artDialog.data(_winName + _open);
	api && api.close();
	return false;
};

// 點擊iframe內容切換疊加高度
_top != window && $(document).bind('mousedown', function () {
	var api = artDialog.open.api;
	api && api.zIndex();
});


/**
 * Ajax填充內容
 * @param	{String}			地址
 * @param	{Object}			配置參數
 * @param	{Boolean}			是否允許緩存. 默認true
 */
artDialog.load = function(url, options, cache){
	cache = cache || false;
	var opt = options || {};
		
	var config = {
		zIndex: _zIndex(),
		init: function(here){
			var api = this,
				aConfig = api.config;
			
			$.ajax({
				url: url,
				success: function (content) {
					api.content(content);
					opt.init && opt.init.call(api, here);		
				},
				cache: cache
			});
			
		}
	};
	
	delete options.content;
	
	for (var i in opt) {
		if (config[i] === undefined) config[i] = opt[i];
	};
	
	return _proxyDialog(config);
};


/**
 * 警告
 * @param	{String}	消息內容
 */
artDialog.alert = function (content, callback) {
	return _proxyDialog({
		id: 'Alert',
		zIndex: _zIndex(),
		icon: 'warning',
		fixed: true,
		lock: true,
		content: content,
		ok: true,
		close: callback
	});
};


/**
 * 確認
 * @param	{String}	消息內容
 * @param	{Function}	確定按鈕回調函數
 * @param	{Function}	取消按鈕回調函數
 */
artDialog.confirm = function (content, yes, no) {
	return _proxyDialog({
		id: 'Confirm',
		zIndex: _zIndex(),
		icon: 'question',
		fixed: true,
		lock: true,
		opacity: .1,
		content: content,
		ok: function (here) {
			return yes.call(this, here);
		},
		cancel: function (here) {
			return no && no.call(this, here);
		}
	});
};


/**
 * 提問
 * @param	{String}	提問內容
 * @param	{Function}	回調函數. 接收參數：輸入值
 * @param	{String}	默認值
 */
artDialog.prompt = function (content, yes, value) {
	value = value || '';
	var input;
	
	return _proxyDialog({
		id: 'Prompt',
		zIndex: _zIndex(),
		icon: 'question',
		fixed: true,
		lock: true,
		opacity: .1,
		content: [
			'<div style="margin-bottom:5px;font-size:12px">',
				content,
			'</div>',
			'<div>',
				'<input value="',
					value,
				'" style="width:18em;padding:6px 4px" />',
			'</div>'
			].join(''),
		init: function () {
			input = this.DOM.content.find('input')[0];
			input.select();
			input.focus();
		},
		ok: function (here) {
			return yes && yes.call(this, input.value, here);
		},
		cancel: true
	});
};


/**
 * 短暫提示
 * @param	{String}	提示內容
 * @param	{Number}	顯示時間 (默認1.5秒)
 */
artDialog.tips = function (content, time) {
	return _proxyDialog({
		id: 'Tips',
		zIndex: _zIndex(),
		title: false,
		cancel: false,
		fixed: true,
		lock: false
	})
	.content('<div style="padding: 0 1em;">' + content + '</div>')
	.time(time || 1.5);
};


// 增強artDialog拖拽體驗
// - 防止鼠標落入iframe導致不流暢
// - 對超大對話框拖動優化
$(function () {
	var event = artDialog.dragEvent;
	if (!event) return;

	var $window = $(window),
		$document = $(document),
		positionType = _isIE6 ? 'absolute' : 'fixed',
		dragEvent = event.prototype,
		mask = document.createElement('div'),
		style = mask.style;
		
	style.cssText = 'display:none;position:' + positionType + ';left:0;top:0;width:100%;height:100%;'
	+ 'cursor:move;filter:alpha(opacity=0);opacity:0;background:#FFF';
		
	document.body.appendChild(mask);
	dragEvent._start = dragEvent.start;
	dragEvent._end = dragEvent.end;
	
	dragEvent.start = function () {
		var DOM = artDialog.focus.DOM,
			main = DOM.main[0],
			iframe = DOM.content[0].getElementsByTagName('iframe')[0];
		
		dragEvent._start.apply(this, arguments);
		style.display = 'block';
		style.zIndex = artDialog.defaults.zIndex + 3;
		
		if (positionType === 'absolute') {
			style.width = $window.width() + 'px';
			style.height = $window.height() + 'px';
			style.left = $document.scrollLeft() + 'px';
			style.top = $document.scrollTop() + 'px';
		};
		
		if (iframe && main.offsetWidth * main.offsetHeight > 307200) {
			main.style.visibility = 'hidden';
		};
	};
	
	dragEvent.end = function () {
		var dialog = artDialog.focus;
		dragEvent._end.apply(this, arguments);
		style.display = 'none';
		if (dialog) dialog.DOM.main[0].style.visibility = 'visible';
	};
});

})(this.art || this.jQuery, this, this.artDialog);

