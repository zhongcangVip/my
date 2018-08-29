package com.bbsuper.task.web;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.dao.scheduler.SitemapDao;
import com.bbsuper.model.web.enums.ChannelEnum;
import com.bbsuper.service.scheduler.sitemap.SiteMapWebHandler;

/**
 * 生成网站地图
 * @author yinyuqiao
 * 2017年7月26日 上午9:45:21
 */
@Component
public class CreateSitemapTask{
	private static final String SITEMAP_XMLFILE_PATH = "/data/xml/"; //web sitemap.xml路径
	private static final String SITEMAP_TXTFILE_PATH = "/data/xml/urls.txt"; //web urls.txt路径
	private static final String SITEMAP_XMLFILE_M_PATH = "/data/xml/m/"; //m站 sitemap.xml路径
	private static final String SITEMAP_TXTFILE_M_PATH = "/data/xml/m/urls.txt"; //m站 urls.txt路径

	@Autowired
	protected SitemapDao sitemapDao;
	
	/**
	 * PC端生成sitemap.xml
	 */
	public void createSiteMapWebXml() {
		SiteMapWebHandler.createSiteMapXml(SITEMAP_XMLFILE_PATH, "https://www.babasuper.com");
	}
	
	/**
	 * m站生成sitemap.xml
	 */
	public void createMSiteMapWebXml() {
		SiteMapWebHandler.createSiteMapXml(SITEMAP_XMLFILE_M_PATH, "https://m.babasuper.com");
	}
	
	/**
	 * PC端生成urls.txt
	 */
	public void createSiteMapUrls() {
		String fileName = SITEMAP_TXTFILE_PATH;
		//生成urls.txt
		try {
			File file = new File(fileName);
			if(file.exists()){
				file.delete();
				file.createNewFile();
			}else{
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			//资讯
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("timing", new Date());
			param.put("channel", ChannelEnum.NEWS.toString());
			List<Map<String,Object>> newsList = sitemapDao.getNewsList(param);
			for(Map<String,Object> news: newsList){
				writer.write("https://www.babasuper.com/news/" + news.get("id").toString() + ".html\n");
			}
			param.put("channel", ChannelEnum.INVITE.toString());
			newsList = sitemapDao.getNewsList(param);
			for(Map<String,Object> news: newsList){
				writer.write("https://www.babasuper.com/inviteinfo/" + news.get("id").toString() + ".html\n");
			}
			//招标
			newsList = sitemapDao.getInvite(param);
			for(Map<String,Object> news: newsList){
				writer.write("https://www.babasuper.com/invite/" + news.get("id").toString() + ".html\n");
			}
			//车源
			Map<String,Object> param2=new HashMap<String,Object>();
			param2.put("createTime", new Date());
			List<Map<String,Object>> carInfoList = sitemapDao.getCarList(param2);
			for(Map<String,Object> car: carInfoList){
				writer.write("https://www.babasuper.com/cheyuan/" + car.get("id").toString() + ".html\n");
			}
			//货源
			Map<String,Object> param3=new HashMap<String,Object>();
			param3.put("createTime", new Date());
			List<Map<String,Object>> goodsList = sitemapDao.getOwnerList(param3);
			for(Map<String,Object> goods: goodsList){
				writer.write("https://www.babasuper.com/huoyuan/" + goods.get("id").toString() + ".html\n");
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * m站生成urls.txt
	 */
	public void createMSiteMapUrls() {
		String fileName = SITEMAP_TXTFILE_M_PATH;
		//生成urls.txt
		try {
			File file = new File(fileName);
			if(file.exists()){
				file.delete();
				file.createNewFile();
			}else{
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			//资讯
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("timing", new Date());
			param.put("channel", ChannelEnum.NEWS.toString());
			List<Map<String,Object>> newsList = sitemapDao.getNewsList(param);
			for(Map<String,Object> news: newsList){
				writer.write("https://m.babasuper.com/news/" + news.get("id").toString() + ".html\n");
			}
			//车源
			Map<String,Object> param2=new HashMap<String,Object>();
			param2.put("createTime", new Date());
			List<Map<String,Object>> carInfoList = sitemapDao.getCarList(param2);
			for(Map<String,Object> car: carInfoList){
				writer.write("https://m.babasuper.com/cheyuan/" + car.get("id").toString() + ".html\n");
			}
			//货源
			Map<String,Object> param3=new HashMap<String,Object>();
			param3.put("createTime", new Date());
			List<Map<String,Object>> goodsList = sitemapDao.getOwnerList(param3);
			for(Map<String,Object> goods: goodsList){
				writer.write("https://m.babasuper.com/huoyuan/" + goods.get("id").toString() + ".html\n");
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
