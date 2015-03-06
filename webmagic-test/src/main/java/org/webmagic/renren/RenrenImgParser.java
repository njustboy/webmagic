package org.webmagic.renren;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RenrenImgParser {
	private Logger logger = LogManager.getLogger(RenrenImgParser.class.getName());
	ConfigData configData = ConfigData.getInstance();
	private CloseableHttpClient httpclient;
	private List<String> userList;
	public static void main(String[] args) {
		RenrenImgParser parser = new RenrenImgParser();
		parser.init();
		boolean loginSuccess = parser.loginSuccess();
		if(!loginSuccess){
			return;
		}
		parser.parse();
	}
	
	private void parse(){
		ExecutorService pool1 = Executors.newFixedThreadPool(5);
		ExecutorService pool2 = Executors.newFixedThreadPool(5);
		ExecutorService pool3 = Executors.newFixedThreadPool(5);
		
		for(String id:userList){
			pool1.submit(new MainPageParser(id,httpclient));
		}
		
		for(int i=0;i<5;i++){
			pool2.submit(new AlbumPageParser(httpclient));
			pool3.submit(new ImageDownloader(httpclient));
		}
	}
	
	private boolean loginSuccess(){
		httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("http://www.renren.com/PLogin.do");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", configData.getUserName()));
		params.add(new BasicNameValuePair("password", configData.getPassWord()));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			// 提交登录数据
			HttpResponse re = httpclient.execute(httppost);
			// 获得跳转的网址
			Header locationHeader = re.getFirstHeader("Location");
			// 登陆不成功
			if (locationHeader == null) {
				logger.error("登陆不成功");
				return false;
			} else
			{
				logger.info("登录成功");
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private void init(){
		userList = new LinkedList<String>();
		BufferedReader br = null;
		FileReader fr = null;
		try{
			fr = new FileReader(new File("config/userList"));
			br = new BufferedReader(fr);
			String line = br.readLine();
			while(line!=null){
				userList.add(line);
				line = br.readLine();
			}
			logger.info("共需爬取"+userList.size()+"个用户的相册");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br!=null){
				try{
					br.close();
				}catch(Exception e){
					
				}
			}
			if(fr!=null){
				try{
					fr.close();
				}catch(Exception e){
					
				}
			}
		}
	}
}
