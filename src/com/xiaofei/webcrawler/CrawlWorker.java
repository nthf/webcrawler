package com.xiaofei.webcrawler;

import java.io.IOException;  
import java.net.MalformedURLException;  
import java.util.List;
import java.util.Random;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
/** 
 * 想象一下：如果能够模拟一个没有界面的浏览器,还有什么不能做到的呢? 
 * 我选择了HtmlUnit,可以说是一个java版本的无界面浏览器,
 * 几乎无所不能,而且很多东西都封装得特别完美
 * 
 * @see HtmlUnit http://htmlunit.sourceforge.net
 */ 
public class CrawlWorker implements Runnable {
	
	/**
	 * 目标网页的url地址
	 */
	private String url;
	/**
	 * 操作指令. 如click表示模拟点击
	 */
	private String cmd;
	/**
	 * 操作的附加参数. 不是必须的
	 */
	private String param;
	
	/**
	 * 伪造的访问来源. 不是必须的
	 */
	private String referer;
	
	public CrawlWorker(String url, String cmd, String param, String referer) {
		this.url = url;
		this.cmd = cmd;
		this.param = param;
		this.referer = referer;
	}
	
	/**
	 * 奔跑吧，小强!
	 */
	public void run() {
		switch (this.cmd) {
			case "click":
				this.doClick();
				break;
			case "seo":
				this.doSeo();
				break;
		}
	}
	
	/**
	 * 模拟百度搜索的点击
	 * 目的: 提高百度PR值
	 */
	public void doClick() {
		java.util.logging.Logger.getLogger("net.sourceforge.htmlunit").setLevel(java.util.logging.Level.OFF); 
		
		// 伪造随机生成一个浏览器来源信息
		String[] userAgents = CrawlWorker.genUserAgents();
		Random rand = new Random();
		final int node = rand.nextInt(userAgents.length);
		
		// 模拟一个浏览器  
        final WebClient webClient = new WebClient(); 
        // 设置相关的参数  
        webClient.getOptions().setTimeout(60 * 1000); // 限制1分钟
        webClient.getOptions().setJavaScriptEnabled(false); 
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);  
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());  

        // 伪造访问来源
        if (this.referer != "") {
        	webClient.addRequestHeader("Referer", this.referer);
        }
        webClient.addRequestHeader("User-Agent", userAgents[node]);
        
		try {
			System.out.println("start");
			
			// 模拟浏览器打开一个目标网址 
			HtmlPage rootPage = webClient.getPage(this.url);
			
	        // 获取OnSubmit事件
	        //HtmlForm searchForm = (HtmlForm) rootPage.getElementById("form");
	        //String submitScript = searchForm.getOnSubmitAttribute();
	        //String submitScript = "javascript:F.call('ps/sug','pssubmit');";
			String submitScript = "javascript:document.form.onsubmit();";
	        
	        // 获取搜索输入框并设置搜索值
	        HtmlInput searchInput = (HtmlInput) rootPage.getElementById("kw");
	        searchInput.setValueAttribute(this.param);
	        // 获取搜索按钮并点击
	        HtmlSubmitInput searchButton = (HtmlSubmitInput) rootPage.getElementById("su");  
	        HtmlPage searchResultPage = searchButton.click();
	        
	        System.out.println("doing");
	        
	        if (!searchResultPage.isHtmlPage()) {
		        ScriptResult submitResult = rootPage.executeJavaScript(submitScript);
		        searchResultPage = (HtmlPage) submitResult.getNewPage();
	        } else {
	        	//System.out.println(searchResultPage.asXml());
	        }
	        
	        // 该方法在getPage()方法之后调用才能生效  
	        webClient.waitForBackgroundJavaScript(5000);
	        webClient.setJavaScriptTimeout(0);
	        
	        // 点击搜索结果集中的A链接
			List<?> searchALinkList = searchResultPage.getByXPath("//a[@class=\"c-img6\"]");
        	if (!searchALinkList.isEmpty()) {
	        	final int totalA = searchALinkList.size();
	        	for (int i = 0; i < totalA; i ++) {
	        		HtmlAnchor searchALink = (HtmlAnchor) searchALinkList.get(i);
	        		searchALink.click();
	        		System.out.println(searchALink.asXml());
	        		Thread.sleep(3000);
	        	}
        	}
	        
	        System.out.println("success");  
	        
	        rootPage = null;
	        searchButton = null;
	        searchResultPage = null;
	        searchALinkList = null;

		} catch (FailingHttpStatusCodeException e) {
			//e.printStackTrace();
		} catch (MalformedURLException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (InterruptedException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			//e.printStackTrace();
		} finally {
			webClient.close();
		}
	}
	
	/**
	 * 获取网页的SEO信息
	 */
	public void doSeo() {
		// 模拟一个浏览器  
        WebClient webClient = new WebClient(); 
        // 设置相关的参数  
        webClient.getOptions().setTimeout(60 * 1000); // 限制1分钟
        webClient.getOptions().setJavaScriptEnabled(false); 
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);  
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        
        // 伪造访问来源
        if (this.referer != "") {
        	webClient.addRequestHeader("Referer", this.referer);
        }
        
		try {
			// 模拟浏览器打开一个目标网址 
			HtmlPage rootPage = webClient.getPage(this.url);
			
	        // 获取网页meta属性 title,keywords,description
			String metaTitle = "";
			String metaKeywords = "";
			String metaDescription = "";
			if (!rootPage.getElementsByTagName("title").isEmpty()) {
				metaTitle = rootPage.getElementsByTagName("title").get(0).asText();
			}
			if (!rootPage.getElementsByTagName("keywords").isEmpty()) {
				metaKeywords = rootPage.getElementsByTagName("keywords").get(0).asText();
			}
			if (!rootPage.getElementsByTagName("description").isEmpty()) {
				metaDescription = rootPage.getElementsByTagName("description").get(0).asText();
			}
			
			System.out.println("title: " + metaTitle);
			System.out.println("keywords: " + metaKeywords);
			System.out.println("description: " + metaDescription);
	        
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			webClient.close();
		}
	}
	
	/**
	 * 获取并生成主流浏览器的UserAgent
	 * 
	 * 备注：从网上找到, 只找了主流浏览器的, 以后可根据需要再改造
	 */
	private static String[] genUserAgents() {
		
		String[] userAgents = new String[26];
		
		// Safari 5.1 Windows
		userAgents[0] = "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50";
		// Safari 5.1 MAC
		userAgents[1] = "User-Agent:Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50";
		// IE 9.0
		userAgents[2] = "User-Agent:Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0";
		// IE 8.0
		userAgents[3] = "User-Agent:Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)";
		// IE 7.0
		userAgents[4] = "User-Agent:Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)";
		// IE 6.0
		userAgents[5] = "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)";
		// Firefox 4.0.1 MAC
		userAgents[6] = "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1";
		// Firefox 4.0.1 Windows
		userAgents[7] = "User-Agent:Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1";
		// Opera 11.11 MAC
		userAgents[8] = "User-Agent:Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; en) Presto/2.8.131 Version/11.11";
		// Opera 11.11 Windows
		userAgents[9] = "User-Agent:Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11";
		// Chrome 17 MAC
		userAgents[10]= "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11";
		// 傲游 Maxthon
		userAgents[11]= "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon 2.0)";
		// 腾讯 TT
		userAgents[12]= "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; TencentTraveler 4.0)";
		// 搜狗浏览器 1.x
		userAgents[13]= "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)";
		// 360浏览器
		userAgents[14]= "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; 360SE)";
		// 世界之窗浏览器
		userAgents[15]= "User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; The World)";
		// 移动Safari iOS 4.33 iPhone
		userAgents[16]= "User-Agent:Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5";
		// 移动Safari iOS 4.33 iPod
		userAgents[17]= "User-Agent:Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5";
		// 移动Safari iOS 4.33 iPad
		userAgents[18]= "User-Agent:Mozilla/5.0 (iPad; U; CPU OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5";
		// 移动Android 4.4.4
		userAgents[19]= "Mozilla/5.0 (Linux; Android 4.4.4; HTC D820u Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko)";
		userAgents[20]= "Mozilla/5.0 (Linux; U; Android 4.4.4; zh-cn; HTC_D820u Build/KTU84P) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
		userAgents[21]= "Mozilla/5.0 (Linux; Android 4.4.4; HTC D820u Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 bdbrowser_i18n/4.6.0.7";
		userAgents[22]= "Mozilla/5.0 (Linux; U; Android 4.4.4; zh-CN; HTC D820u Build/KTU84P) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/10.1.0.527 U3/0.8.0 Mobile Safari/534.30";
		userAgents[23]= "Mozilla/5.0 (Linux; U; Android 4.4.4; zh-cn; HTC D820u Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko)Version/4.0 MQQBrowser/5.6 Mobile Safari/537.36";
		// 移动IOS 8.0
		userAgents[24]= "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12A365 Safari/600.1.4";
		userAgents[25]= "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0)";
		
		return userAgents;
	}
	
}
