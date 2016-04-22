package com.tianyl.weixin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tianyl.core.ioc.ApplicationContext;
import com.tianyl.core.ioc.annotation.Autowired;
import com.tianyl.core.ioc.annotation.Service;
import com.tianyl.core.util.StringUtil;
import com.tianyl.core.util.log.LogManager;
import com.tianyl.core.util.webClient.RequestResult;
import com.tianyl.core.util.webClient.WebUtil;
import com.tianyl.weixin.dao.ArticalDAO;
import com.tianyl.weixin.dao.OfficialAccountDAO;
import com.tianyl.weixin.model.Artical;
import com.tianyl.weixin.model.OfficialAccount;

@Service
public class ArticalService {

	@Autowired
	private ArticalDAO articalDAO;

	@Autowired
	private OfficialAccountDAO officialAccountDAO;

	public void crawl() {
		List<OfficialAccount> officialAccounts = officialAccountDAO.findAll();
		for (OfficialAccount oa : officialAccounts) {
			try {
				Thread.sleep(1000 * 70);// 暂停70秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String historyUrl = getHistoryUrl(oa.getWxId());
			if (StringUtil.isBlank(historyUrl)) {
				continue;
			}
			List<Artical> articals = null;
			try {
				articals = parseArtical(historyUrl, oa.getId());
			} catch (Exception e) {
				e.printStackTrace();
				LogManager.log("parse artical error : " + historyUrl);
				LogManager.log(e);
			}
			if (articals != null && articals.size() > 0) {
				Set<String> existUuids = articalDAO.findUuids(oa.getId());
				List<Artical> toSave = new ArrayList<>();
				for (Artical ar : articals) {
					if (!existUuids.contains(ar.getUuid())) {
						toSave.add(ar);
					}
				}
				if (toSave.size() > 0) {
					articalDAO.save(toSave);
				}
			}
		}
	}

	private List<Artical> parseArtical(String historyUrl, Integer oaId) {
		RequestResult requestResult = WebUtil.getUrlResponse(historyUrl, null, null, true);
		if (!requestResult.isOk()) {
			LogManager.log("get history content error : " + historyUrl);
			LogManager.log(requestResult.getResultStr());
			return null;
		}
		Document doc = Jsoup.parse(requestResult.getResultStr());
		Elements eles = doc.getElementsByClass("weui_msg_card");
		if (eles == null) {
			LogManager.log("find weui_msg_card error : " + historyUrl);
			LogManager.log(requestResult.getResultStr());
			return null;
		}
		Elements scripts = doc.getElementsByTag("script");
		Element sc = scripts.get(scripts.size() - 1);
		String scHtml = sc.html();
		String msgList = scHtml.replaceFirst("[\\s\\S]*var msgList = '", "").replaceFirst("seajs.use[\\s\\S]*", "").replaceFirst("';", "").trim();
		msgList = StringEscapeUtils.unescapeHtml(msgList);
		LogManager.log("-------------------------");
		LogManager.log("oaId:" + oaId);
		LogManager.log("historyUrl:" + historyUrl);
		LogManager.log("msgList:" + msgList);
		LogManager.log("-------------------------");
		JSONObject msgObj = JSONObject.parseObject(msgList);
		JSONArray msgArray = msgObj.getJSONArray("list");
		List<Artical> result = new ArrayList<>();
		for (int index = 0; index < msgArray.size(); index++) {
			JSONObject obj = msgArray.getJSONObject(index);
			Long time = obj.getJSONObject("comm_msg_info").getLong("datetime");
			String title = obj.getJSONObject("app_msg_ext_info").getString("title");
			String url = "http://mp.weixin.qq.com" + StringEscapeUtils.unescapeHtml(obj.getJSONObject("app_msg_ext_info").getString("content_url")).substring(1);
			String uuid = obj.getJSONObject("comm_msg_info").getString("id");
			Artical artical = new Artical();
			artical.setOfficialAccountId(oaId);
			artical.setPublishDate(new Date(time * 1000));
			artical.setTitle(title);
			artical.setUrl(url);
			artical.setUuid(uuid);
			result.add(artical);
		}
		return result;
	}

	private String getHistoryUrl(String wxId) {
		String searchAccountUrl = "http://weixin.sogou.com/weixin?query=" + wxId;
		RequestResult requestResult = WebUtil.getUrlResponse(searchAccountUrl, null, null, true);
		if (!requestResult.isOk()) {
			LogManager.log("search account error : " + wxId);
			LogManager.log(requestResult.getResultStr());
			return null;
		}
		Document doc = Jsoup.parse(requestResult.getResultStr());
		Element ele = doc.getElementById("sogou_vr_11002301_box_0");
		if (ele == null) {
			LogManager.log("find sogou_vr_11002301_box_0 error : " + wxId);
			LogManager.log(requestResult.getResultStr());
			return null;
		}
		String url = ele.attr("href");
		if (StringUtil.isBlank(url)) {
			LogManager.log("parse url error : " + wxId);
			LogManager.log(requestResult.getResultStr());
			return null;
		}
		return url;
	}

	public static void main(String[] args) {
		ApplicationContext.getBean(ArticalService.class).crawl();
		// ScriptEngineManager mgr = new ScriptEngineManager();
		// ScriptEngine engine = mgr.getEngineByExtension("js");
		// try {
		// engine.eval("var aaa = 'bbb';var bbb = 'ccc'");
		// } catch (ScriptException e) {
		// e.printStackTrace();
		// }
	}
}
