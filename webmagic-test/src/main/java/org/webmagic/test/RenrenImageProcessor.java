package org.webmagic.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ImgFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class RenrenImageProcessor implements PageProcessor {
	private Site site = Site.me().setNameLabel("email").setName("15601589581")
			.setPasswdLabel("password").setPasswd("520520").setLoginUrl("http://www.renren.com/PLogin.do").setRetryTimes(3)
			.setSleepTime(0);

	@Override
	public void process(Page page) {
		//对于图片链接不需要处理
		if(page.getRequest().getUrl().endsWith("jpg")){
			return;
		}
		String html = page.getRawText();
		List<String> url1 = parserAlbumlistUrl(html);
		List<String> url2 = parserImglistUrl(html);
		page.addTargetRequests(url1);
		page.addTargetRequests(url2);

	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		String[] urls = new String[2];
		urls[1] = "aaa";
		urls[2] = "bbb";
		Spider.create(new RenrenImageProcessor())
				.addUrl("http://photo.renren.com/photo/222652243/album-920687585?frommyphoto")
				.addPipeline(new ImgFilePipeline("C:/webmagic"))
				// 开启5个线程抓取
				.thread(5)
				// 启动爬虫
				.run();
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
		Pattern p = Pattern.compile("http:((?!http)(?!jpg).)*large.*jpg");
		Matcher m = p.matcher(html);
		while (m.find()) {
			String url = m.group();
			imglistUrl.add(url);
		}
		return imglistUrl;
	}

}
