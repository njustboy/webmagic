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
 * 解析相册页面中图片链接
 * 从redis中获取相册页面链接
 * 将解析到的图片链接存入redis
 * @author Administrator
 *
 */
public class AlbumPageParser implements Runnable {
	Logger logger = LogManager.getLogger(AlbumPageParser.class.getName());
	ConfigData configData = ConfigData.getInstance();
	Jedis jedis = new Jedis(configData.getDbHost(),configData.getDbPort());
	private HttpGet httpget;
	private HttpResponse re;
	private HttpClient httpclient;
	
	public AlbumPageParser(HttpClient httpclient) {
		this.httpclient = httpclient;
	}
	
	@Override
	public void run() {
		int timeWait = 0;
		while (true) {
			if (jedis.llen("albumUrl") > 0) {
				parseAlbumPage();
				timeWait = 0;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.warn(e.getMessage());
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.warn(e.getMessage());
				}
				timeWait += 1;
			}

			if (timeWait >= 10) {
				return;
			}
		}
	}
	
	public void parseAlbumPage(){
		String str = jedis.lpop("albumUrl");
		if(str==null){
			return;
		}
		String rootDir = str.substring(0, str.indexOf("###"));
		String albumUrl = str.substring(str.indexOf("###")+3, str.length());
		//相册对应的HTML页面
		String albumHtml = getHtmlSource(albumUrl);
		//从相册页面中提取出图片链接
		List<String> imgList = parserImglistUrl(albumHtml);
		//相册名称
		String albumName = parserAlbumName(albumHtml);
		//图片文件夹：根目录/用户名/相册名
		String dir = rootDir + File.separator+ albumName;
		File dirFile = new File(dir);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		logger.info("成功获取相册"+albumName+"中链接");
		for (String imgUrl : imgList) {
			String fileName = dir + File.separator
					+ imgUrl.substring(imgUrl.lastIndexOf("/") + 1,imgUrl.length());
			String url = fileName+"###"+imgUrl;
			jedis.rpush("imgUrl", url);
		}
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
	
	private List<String> parserImglistUrl(String html) {
		html = html.replaceAll("\\\\", "");
		
		List<String> imglistUrl = new ArrayList<String>();
		Pattern p = Pattern.compile("http:((?!http)(?!jpg).)*large.*jpg");
		Matcher m = p.matcher(html);
		while (m.find()) {
			String url = m.group();
			imglistUrl.add(url);
		}
		return imglistUrl;
	}
	
	/**
	 * 解析相册的名称
	 * @param html
	 * @return
	 */
	private String parserAlbumName(String html) {
		String albumName = "";
		int beginIndex = html.indexOf("albumName");
		if (beginIndex == -1) {
			return albumName;
		}
		beginIndex = beginIndex + 12;
		html = html.substring(beginIndex);
		int endIndex = html.indexOf("\'");
		albumName = html.substring(0, endIndex);
		return albumName;
	}

}
