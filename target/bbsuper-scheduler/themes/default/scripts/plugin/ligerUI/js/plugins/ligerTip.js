/**
* jQuery ligerUI 1.1.9
* 
* http://ligerui.com
*  
* Author daomi 2012 [ gd_star@163.com ] 
* 
*/

(function ($)
{
    //氣泡,可以在制定位置顯示
    $.ligerTip = function (p)
    {
        return $.ligerui.run.call(null, "ligerTip", arguments);
    };

    //在指定Dom Element右側顯示氣泡
    //target：將ligerui對像ID附加上
    $.fn.ligerTip = function (options)
    {
        this.each(function ()
        {
            var p = $.extend({}, $.ligerDefaults.ElementTip, options || {});
            p.target = p.target || this;
            //如果是自動模式：鼠標經過時顯示，移開時關閉
            if (p.auto || options == undefined)
            {
                if (!p.content)
                {
                    p.content = this.title;
                    if (p.removeTitle)
                        $(this).removeAttr("title");
                }
                p.content = p.content || this.title;
                $(this).bind('mouseover.tip', function ()
                {
                    p.x = $(this).offset().left + $(this).width() + (p.distanceX || 0);
                    p.y = $(this).offset().top + (p.distanceY || 0);
                    $.ligerTip(p);
                }).bind('mouseout.tip', function ()
                {

                    var tipmanager = $.ligerui.managers[this.ligeruitipid];
                    if (tipmanager)
                    {
                        tipmanager.remove();
                    }
                });
            }
            else
            {
                if (p.target.ligeruitipid) return;
                p.x = $(this).offset().left + $(this).width() + (p.distanceX || 0);
                p.y = $(this).offset().top + (p.distanceY || 0);
                p.x = p.x || 0;
                p.y = p.y || 0;
                $.ligerTip(p);
            }
        });
        return $.ligerui.get(this, 'ligeruitipid');
    };
    //關閉指定在Dom Element(附加了ligerui對像ID,屬性名"ligeruitipid")顯示的氣泡
    $.fn.ligerHideTip = function (options)
    {
        return this.each(function ()
        {
            var p = options || {};
            if (p.isLabel == undefined)
            {
                //如果是lable，將查找指定的input，並找到ligerui對像ID
                p.isLabel = this.tagName.toLowerCase() == "label" && $(this).attr("for") != null;
            }
            var target = this;
            if (p.isLabel)
            {
                var forele = $("#" + $(this).attr("for"));
                if (forele.length == 0) return;
                target = forele[0];
            }
            var tipmanager = $.ligerui.managers[target.ligeruitipid];
            if (tipmanager)
            {
                tipmanager.remove();
            }
        }).unbind('mouseover.tip').unbind('mouseout.tip');
    };


    $.fn.ligerGetTipManager = function ()
    {
        return $.ligerui.get(this);
    };


    $.ligerDefaults = $.ligerDefaults || {};


    //隱藏氣泡
    $.ligerDefaults.HideTip = {};

    //氣泡
    $.ligerDefaults.Tip = {
        content: null,
        callback: null,
        width: 150,
        height: null,
        x: 0,
        y: 0,
        appendIdTo: null,       //保存ID到那一個對像(jQuery)(待移除)
        target: null,
        auto: null,             //是否自動模式，如果是，那麼：鼠標經過時顯示，移開時關閉,並且當content為空時自動讀取attr[title]
        removeTitle: true        //自動模式時，默認是否移除掉title
    };

    //在指定Dom Element右側顯示氣泡,通過$.fn.ligerTip調用
    $.ligerDefaults.ElementTip = {
        distanceX: 1,
        distanceY: -3,
        auto: null,
        removeTitle: true
    };

    $.ligerMethos.Tip = {};

    $.ligerui.controls.Tip = function (options)
    {
        $.ligerui.controls.Tip.base.constructor.call(this, null, options);
    };
    $.ligerui.controls.Tip.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Tip';
        },
        __idPrev: function ()
        {
            return 'Tip';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Tip;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            var tip = $('<div class="l-verify-tip"><div class="l-verify-tip-corner"></div><div class="l-verify-tip-content"></div></div>');
            g.tip = tip;
            g.tip.attr("id", g.id);
            if (p.content)
            {
                $("> .l-verify-tip-content:first", tip).html(p.content);
                tip.appendTo('body');
            }
            else
            {
                return;
            }
            tip.css({ left: p.x, top: p.y }).show();
            p.width && $("> .l-verify-tip-content:first", tip).width(p.width - 8);
            p.height && $("> .l-verify-tip-content:first", tip).width(p.height);
            eee = p.appendIdTo;
            if (p.appendIdTo)
            {
                p.appendIdTo.attr("ligerTipId", g.id);
            }
            if (p.target)
            {
                $(p.target).attr("ligerTipId", g.id);
                p.target.ligeruitipid = g.id;
            }
            p.callback && p.callback(tip);
            g.set(p);
        },
        _setContent: function (content)
        {
            $("> .l-verify-tip-content:first", this.tip).html(content);
        },
        remove: function ()
        {
            if (this.options.appendIdTo)
            {
                this.options.appendIdTo.removeAttr("ligerTipId");
            }
            if (this.options.target)
            {
                $(this.options.target).removeAttr("ligerTipId");
                this.options.target.ligeruitipid = null;
            }
            this.tip.remove();
        }
    });
})(jQuery);