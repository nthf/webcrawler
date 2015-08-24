package com.xiaofei.webcrawler;

/** 
 * 网页爬虫脚本的入口类. 执行此脚本按顺序提供两个参数:
 * 
 * 1. 目标网页的URL绝对地址, 如: http://www.baidu.com
 * 2. 对目标网页的操作指令, 如: click表示点击
 * 3. 操作的附加参数, 不是必须的，如click时为关键词 
 * 4. 伪造的访问来源地址, 如: http://xxx.com
 */ 
public class CrawlScheduler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "https://www.baidu.com";
		String cmd = "click";
		String param = "yohoboys";
		String referer = "http://www.baidu.com";
		
		if (args.length > 0) {
			url = String.valueOf(args[0]);
		}
		if (args.length > 1) {
			cmd = String.valueOf(args[1]);
		}
		if (args.length > 2) {
			param = String.valueOf(args[2]);
		}
		if (args.length > 3) {
			referer = String.valueOf(args[3]);
		}
		
		CrawlWorker worker = new CrawlWorker(url, cmd, param, referer);
		Thread crawler = new Thread(worker);
		crawler.start();
	}
	
}
