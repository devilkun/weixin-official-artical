package com.tianyl.weixin.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tianyl.core.ioc.annotation.Autowired;
import com.tianyl.core.mvc.annotation.Controller;
import com.tianyl.weixin.service.ArticalService;
import com.tianyl.weixin.service.OfficialAccountService;

@Controller("/wx")
public class WeiXinController {

	@Autowired
	private ArticalService articalService;

	@Autowired
	private OfficialAccountService officialAccountService;

	public Object getAccountInfo() {
		JSONArray result = officialAccountService.getAccountInfo();
		return result;
	}

	public Object getUnreadAccount() {
		JSONArray result = officialAccountService.getUnreadAccount();
		return result;
	}

	public Object getArticals(Integer officialAccountId) {
		JSONArray result = articalService.find(officialAccountId);
		return result;
	}

	public Object getUnreadArticals(Integer officialAccountId) {
		JSONArray result = articalService.findUnreadArticals(officialAccountId);
		return result;
	}

	public Object searchByName(String name) {
		return officialAccountService.searchByName(name);
	}

	public void save(String wxId, String name) {
		officialAccountService.save(wxId, name);
	}

	public void setHasRead(Integer articalId) {
		articalService.setHasRead(articalId);
	}

	public void setHasReadByOfficialAccountId(Integer officialAccountId) {
		articalService.setHasReadByOfficialAccountId(officialAccountId);
	}

	public void setUnRead(Integer articalId) {
		articalService.setUnRead(articalId);
	}

	public Object getAccountCount() {
		JSONObject obj = officialAccountService.getAccountCount();
		return obj;
	}

}
