package it.grational.http.request;

import groovy.json.JsonOutput;
import it.grational.http.shared.Constants;

import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JsonPatch extends Patch {

	public JsonPatch(Map<String, Object> params) {
		super(createPatchParams(params));
	}

	private static Map<String, Object> createPatchParams(Map<String, Object> params) {
		Map<String, Object> patchParams = new HashMap<>();
		patchParams.put("url", params.get("url"));

		String body = (String) params.get("json");
		if (body == null && params.get("map") != null) {
			body = JsonOutput.toJson(params.get("map"));
		}
		patchParams.put("body", body);

		patchParams.put("parameters", params.get("parameters"));
		patchParams.put("connectTimeout", params.get("connectTimeout"));
		patchParams.put("readTimeout", params.get("readTimeout"));

		Map<String, String> headers = (Map<String, String>) params.get("headers");
		if (headers == null) headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		patchParams.put("headers", headers);

		patchParams.put("cookies", params.get("cookies"));
		patchParams.put("proxy", params.get("proxy"));
		patchParams.put("charset", params.get("charset"));

		return patchParams;
	}

	public JsonPatch (
		URL url,
		String json,
		Map<String, Object> params,
		Proxy proxy
	) {
		super(url, json, params, proxy);
		ensureContentType();
	}

	public JsonPatch(URL url, String json) {
		this(url, json, new HashMap<>(), null);
	}

	public JsonPatch(URL url, Object map) {
		this(url, JsonOutput.toJson(map), new HashMap<>(), null);
	}

	private void ensureContentType() {
		if (!this.parameters.containsKey("headers")) {
			this.parameters.put (
				"headers",
				new HashMap<String, String>()
			);
		}
		((Map<String, String>) this.parameters.get("headers")).put (
			"Content-Type",
			"application/json"
		);
	}
}