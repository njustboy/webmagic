package org.webmagic.renren;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
/**
 * 解析相册主页面中的相册链接
 * 将相册链接存入redis
 * @author Administrator
 *
 */
public class MainPageParser implements Runnable {
	Logger logger = LogManager.getLogger(MainPageParser.class.getName());
	ConfigData configData = ConfigData.getInstance();
	Jedis jedis = new Jedis(configData.getDbHost(),configData.getDbPort());
	private HttpGet httpget;
	private HttpResponse re;
	private HttpClient httpclient;
	private String id;
	
	public MainPageParser(String id,HttpClient httpClient) {
		this.id = id;
		this.httpclient = httpClient;
	}
	
	@Override
	public void run() {
		String entryUrl = createMainUrl();
		String mainHtml = getHtmlSource(entryUrl);
		String userName = parserUserName(mainHtml);
		if(userName.equals("")){
			logger.warn("用户"+id+"没有相册或无浏览权限");
			return;
		}
		logger.info("成功获取用户"+id+"的相册列表");
		//相册列表
		List<String> albumList = parserAlbumlistUrl(mainHtml);
		for(String url:albumList){
			String str = configData.getRootDir()+File.separator+userName+"###"+url;
			jedis.rpush("albumUrl", str);
		}
	}
	
	
	private String createMainUrl() {
		return "http://photo.renren.com/photo/" + id + "/album/relatives/profile";
	}
	
	private String getHtmlSource(String url) {
		String html = "";
		httpget = new HttpGet(url);
		re = null;

		try {
			re = httpclient.execute(httpget);

			html = EntityUtils.toString(re.getEntity());
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			httpget.abort();
		}
		return html;
	}
	
	/**
	 * 解析用户名
	 * @param html
	 * @return
	 */
	private String parserUserName(String html) {
		String userName = "";
		int beginIndex = html.indexOf("<title>") + 7;
		int endIndex = html.indexOf("</title>");
		if (beginIndex >= endIndex) {
			return userName;
		}
		userName = html.substring(beginIndex, endIndex);
		return userName;
	}
	
	/**
	 * 从相册列表页面解析出相册链接
	 * @param html
	 * @return
	 */
	private List<String> parserAlbumlistUrl(String html){
		List<String> albumlistUrl = new ArrayList<String>();
		Pattern p = Pattern.compile("http:((?!http).)*frommyphoto");
		Matcher m = p.matcher(html);
		while(m.find()){
			String url = m.group();
			if(!albumlistUrl.contains(url)){
			albumlistUrl.add(url);
			}
		}
		return albumlistUrl;
	}

}
