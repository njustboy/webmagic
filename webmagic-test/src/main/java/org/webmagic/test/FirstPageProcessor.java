package org.webmagic.test;

import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.ImgFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class FirstPageProcessor implements PageProcessor {
	private Site site = Site.me().setRetryTimes(3).setSleepTime(0);
	@Override
	public void process(Page page) {
		// TODO Auto-generated method stub
		List<String> all = page.getHtml().links().regex(".*\\.jpg").all();
		page.addTargetRequests(all);
		
		
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		 Spider.create(new FirstPageProcessor())
         .addUrl("http://www.163.com/")
         .addPipeline(new ImgFilePipeline("C:\\webmagic\\"))
         //开启5个线程抓取
         .thread(5)
         //启动爬虫
         .run();
	}

}
