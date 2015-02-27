package org.webmagic.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class RenrenImgCrawler {
	private CloseableHttpClient httpclient;

	private HttpGet httpget;

	private HttpResponse re2;

	public static void main(String[] args) {
		RenrenImgCrawler crawler = new RenrenImgCrawler();
		
		boolean loginSuccess = crawler.login("15601589581", "520520");
		if(!loginSuccess){
			return;
		}
		
		crawler.crawlerImg("303878665");

	}

	public void crawlerImg(String id) {
		String entryUrl = createMainUrl(id);
		String mainHtml = getHtmlSource(entryUrl);
		String userName = parserUserName(mainHtml);
		if(userName.equals("")){
			System.out.println("用户"+id+"没有相册或无浏览权限");
			return;
		}
		System.out.println("开始抓取："+userName);
		List<String> albumList = parserAlbumlistUrl(mainHtml);
		for (String albumUrl : albumList) {
			String albumHtml = getHtmlSource(albumUrl);
			List<String> imgList = parserImglistUrl(albumHtml);
			String albumName = parserAlbumName(albumHtml);
			String dir = "c:/人人相册"+File.separator+ userName + File.separator+ albumName;
			File dirFile = new File(dir);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			System.out.println("开始抓取相册："+albumName);
			for (String imgUrl : imgList) {
				String fileName = dir + File.separator
						+ imgUrl.substring(imgUrl.lastIndexOf("/") + 1,imgUrl.length());
				try{
					Thread.sleep(500);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				System.out.println("开始抓取图片："+imgUrl);
				saveImg(imgUrl, fileName);
			}
		}
	}

	private boolean login(String userName, String passwd) {
		httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("http://www.renren.com/PLogin.do");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", userName));
		params.add(new BasicNameValuePair("password", passwd));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			// 提交登录数据
			HttpResponse re = httpclient.execute(httppost);
			// 获得跳转的网址
			Header locationHeader = re.getFirstHeader("Location");
			// 登陆不成功
			if (locationHeader == null) {
				System.out.println("登陆不成功，请稍后再试!");
				return false;
			} else
			{
				System.out.println("登录成功！");
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private String createMainUrl(String id) {
		return "http://photo.renren.com/photo/" + id + "/album/relatives/profile";
	}

	private String getHtmlSource(String url) {
		String html = "";
		httpget = new HttpGet(url);
		re2 = null;

		try {
			re2 = httpclient.execute(httpget);

			html = EntityUtils.toString(re2.getEntity());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

	/**
	 * 从相册列表中解析出每个相册的链接
	 * @param html
	 * @param id
	 * @return
	 */
	private List<String> parserAlbumlistUrl(String html, String id) {
		List<String> albumlistUrl = new ArrayList<String>();
		Pattern p1 = Pattern.compile("\"albumId\":\"\\d{9}\"");
		Pattern p2 = Pattern.compile("\\d{9}");
		Matcher m1 = p1.matcher(html);
		Matcher m2 = null;
		while (m1.find()) {
			String str = m1.group();
			m2 = p2.matcher(str);
			String albumId = "";
			if(m2.find()){
				albumId = m2.group();
			}
			String url = "http://photo.renren.com/photo/" + id + "/album-"
					+ albumId + "/v7";
			albumlistUrl.add(url);
		}
		return albumlistUrl;
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

	private List<String> parserImglistUrl(String html) {
		html = html.replaceAll("\\\\", "");
		
		List<String> imglistUrl = new ArrayList<String>();
		Pattern p = Pattern.compile("http:((?!http).)*jpg");
		Matcher m = p.matcher(html);
		while (m.find()) {
			String url = m.group();
			imglistUrl.add(url);
		}
		return imglistUrl;
	}

	private void saveImg(String imgUrl, String filePath) {
		httpget = new HttpGet(imgUrl);
		re2 = null;
		FileOutputStream fos = null;
		InputStream input = null;
		try {
			fos = new FileOutputStream(new File(filePath));
			re2 = httpclient.execute(httpget);
			input = re2.getEntity().getContent();
			byte[] bytes = new byte[1024];
			int rc = 0;
			while ((rc = input.read(bytes, 0, 1024)) > 0) {
				fos.write(bytes, 0, rc);
			}
			fos.flush();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				e.printStackTrace();
			}
		}
	}
}
