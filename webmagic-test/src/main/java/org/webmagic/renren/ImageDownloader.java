package org.webmagic.renren;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.pipeline.ImgFilePipeline;

/**
 * 下载图片链接对应的图片 从redis中获取图片链接
 * 
 * @author Administrator
 *
 */
public class ImageDownloader implements Runnable {
	Logger logger = LogManager.getLogger(ImageDownloader.class.getName());
	ConfigData configData = ConfigData.getInstance();
	Jedis jedis = new Jedis(configData.getDbHost(), configData.getDbPort());
	private HttpGet httpget;
	private HttpResponse re;
	private HttpClient httpclient;
	
	public ImageDownloader(HttpClient httpclient) {
		this.httpclient = httpclient;
	}

	@Override
	public void run() {
		int timeWait = 0;
		while (true) {
			if (jedis.llen("imgUrl") > 0) {
				downloadImg();
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

	private void downloadImg() {
		String str = jedis.lpop("imgUrl");
		if(str==null){
			return;
		}
		String filePath = str.substring(0, str.indexOf("###"));
		String imgUrl = str.substring(str.indexOf("###") + 3, str.length());
		saveImg(imgUrl, filePath);
	}

	private void saveImg(String imgUrl, String filePath) {
		logger.info("开始下载图片："+imgUrl);
		httpget = new HttpGet(imgUrl);
		FileOutputStream fos = null;
		InputStream input = null;
		try {
			fos = new FileOutputStream(new File(filePath));
			re = httpclient.execute(httpget);
			input = re.getEntity().getContent();
			byte[] bytes = new byte[1024];
			int rc = 0;
			while ((rc = input.read(bytes, 0, 1024)) > 0) {
				fos.write(bytes, 0, rc);
			}
			fos.flush();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			httpget.abort();
			try {
				if (fos != null) {
					fos.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

}
