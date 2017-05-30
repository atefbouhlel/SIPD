package org.inria.jdbc;

/**
 * Implementation of the Backup features: interface between the backup server and the token
 *
 * @author Matthieu Finiasz
 * @date 2015/05
 */

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Backup {

    private String serverUrl = null;
    private HttpClient httpClient = null;
    private CookieStore cookies = null;

    public String id = null;
    
    public Backup(String baseUrl) {
	this.serverUrl = baseUrl;
	if (baseUrl.charAt(baseUrl.length() -1) != '/') {
	    this.serverUrl = this.serverUrl + '/';
	}
	this.cookies = new BasicCookieStore();
	httpClient = HttpClientBuilder.create().setDefaultCookieStore(this.cookies).build();
    }

    private JSONObject sendPost(List<NameValuePair> urlParameters, String urlComplement) {
	HttpPost post = new HttpPost(this.serverUrl + urlComplement);

	try {
	    post.setEntity(new UrlEncodedFormEntity(urlParameters));
	    HttpResponse response = httpClient.execute(post);
	    if (response.getStatusLine().getStatusCode() != 200) {
		return null;
	    }
	    JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(response.getEntity().getContent()));
	    return json;
	} catch (Exception e) {
	    return null;
	}
    }

    private JSONObject sendGet(String urlComplement) {
	HttpGet get = new HttpGet(this.serverUrl + urlComplement);

	try {
	    HttpResponse response = httpClient.execute(get);
	    if (response.getStatusLine().getStatusCode() != 200) {
		return null;
	    }   
	    JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(response.getEntity().getContent()));
	    return json;
	} catch (Exception e) {
	    return null;
	}
    }

    public int emptyDB() {
	JSONObject json = this.sendGet("user/eraseall");
	if (json == null) {
	    return 1;
	}
	if (((Number) json.get("status")).intValue() != 0) {
	    return 2;
	}	
	return 0;
    }

    public int authenticate() {
	// before authenticating, the id has to be set
	if (this.id == null) {
	    return 6;
	}
	
	// first: a GET query to get a challenge String
	JSONObject json = sendGet("user/authenticate");
	if (json == null) {
	    return 1;
	}
	if (((Number) json.get("status")).intValue() != 2) {
	    return 2;
	}
	String challengeString = (String) json.get("challenge");
	
	// byte[] challenge = ... ;

	try {
	    // for tests only
	    String responseString = "ad9857";
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("response", responseString));
	    params.add(new BasicNameValuePair("user_id", this.id));
	    json = sendPost(params, "user/authenticate");
	    if (json == null) {
		return 3;
	    }
	    if (((Number) json.get("status")).intValue() != 0) {
		return 4;
	    }
	    
	    
	    // HttpGet get = new HttpGet(this.serverUrl);
	    // HttpResponse response = httpClient.execute(get);
	    // IOUtils.copy(response.getEntity().getContent(), out);
	} catch (Exception e) {
	    return 5;
	}
	return 0;
    }


    public int register() {
	// first get the public key from the card
	// here, we use a hardcoded string instead
	String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
	    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5a1BSgzKYMOi65P88h0U\n" +
	    "widgiD2OW564nWP3QFvKdTAJZ0QR55+ynzVyh0cy1PFB+SU8DGzjmnyxmIjU7KzT\n" +
	    "ng2daj9Pyg128rd5SiE3RcQlbk2j+VpbgIuqAA8nzvEOgdpUA6CL676Jc2f/IAPF\n" +
	    "A0UaVRA8trlyqPjB///TDXi+9zLy9LciyyfA2JELGGKrtETaWxghrOiT57WKNs5B\n" +
	    "1Kd6iShAAGhsVHpXjcVc9LcbSIPGHjmc8Y8gQA2FYLlDA0dUuOjQYxEvK2ybUygT\n" +
	    "kS5Sjv+vzXZX5Zg9ELptfNqClnM9qW6ALhoBSDMeBcLBHYafxb8eatuKBLVLgJmK\n" +
	    "/wIDAQAB\n" +
	    "-----END PUBLIC KEY-----\n";
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("pubkey", publicKey));
	try {
	    JSONObject json = sendPost(params, "user/register");
	    if (json == null) {
		return 1;
	    }
	    if (((Number) json.get("status")).intValue() != 0) {
		return 2;
	    }
	    String id = (String) json.get("id");
	    if (id == null) {
		return 3;
	    }
	    this.id = id;
	} catch (Exception e) {
	    return 1;
	}
	return 0;
    }
}

