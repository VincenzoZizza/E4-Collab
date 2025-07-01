package com.example.e4_collab_rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppLoginResultDto {
	private Map<String, Object> response;

	public AppLoginResultDto(String sessionId, String userId, String userEmail, String accessToken) {

		Map<String, Object> header = new HashMap<>();
		header.put("status", "ok");

		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("user_id", userId);
		userInfo.put("user_email", userEmail);
		userInfo.put("user_type", 1);
		userInfo.put("user_category", 1);
		userInfo.put("access_token", accessToken);

		ArrayList<Object> payload = new ArrayList<>();
		payload.add(userInfo);

		this.response = new HashMap<>();
		this.response.put("success", true);
		this.response.put("session_id", sessionId);
		this.response.put("header", header);
		this.response.put("payload", payload);
	}

	public Map<String, Object> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Object> response) {
		this.response = response;
	}
}
