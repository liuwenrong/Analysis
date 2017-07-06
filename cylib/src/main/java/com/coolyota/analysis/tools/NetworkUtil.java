/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.analysis.tools;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.coolyota.analysis.tools.CYConstants.LOG_TAG;

class NetworkUtil {   
	public static int REQUEST_TIMEOUT = 5000; // 5s
	public static int SO_TIMEOUT = 3000; // 3s	
	private static int serverPort = -1;
	private static boolean hasInitSSL = false;
	private static URL serverUrl = null;
	 
	private static void initSSL() {
		CYLog.d(LOG_TAG,NetworkUtil.class,"InitSSL start it:" + CYConstants.SDK_POS_NAME);
		System.setProperty("javax.net.ssl.keyStoreProvider",
				CYConstants.SDK_POS_NAME);
		System.setProperty("javax.net.ssl.certAlias",
				CYConstants.SDK_CSR_ALIAS);
		CYLog.d(LOG_TAG,NetworkUtil.class,"InitSSL end it:" + CYConstants.SDK_CSR_ALIAS);
	}

	public static MyMessage Post(String url, String data) {
		CYLog.d(LOG_TAG, NetworkUtil.class,"URL = " + url);
		CYLog.d(LOG_TAG, NetworkUtil.class, "LENGTH:" + data.length() + " *Data = " + data + "*");

		if (!hasInitSSL && CYConstants.SDK_SECURITY_LEVEL.equals("2")) {
			initSSL();
			hasInitSSL = true;
		}
		
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		DefaultHttpClient httpclient = null;
		
		/*SDK会运行在如下两种环境中:
		 * 1,CPOS:需要进行双向ssl校验；SDK_SSL=true;此时还要验证dn(如果不想验dn，设置SDK_HTTPS_DN为none)
		 * 2,一般移动设备，是用HTTPS正常发送即可，也能接受非标准证书的https服务端
		 * 3,测试：使用http
		*/
		if (CYConstants.SDK_SECURITY_LEVEL.equals("2")) {
			httpclient = new DefaultHttpClient(httpParams);
			// cpos with dn check
			if (!CYConstants.SDK_HTTPS_DN.equals("none")) {
				SSLSocketFactory mysf = null;
				try {
					mysf = new CposSSLSocketFactory();
					if (serverUrl == null) {
						serverUrl = new URL(url);
						serverPort = ((serverUrl.getPort() == -1) ? serverUrl
								.getDefaultPort() : serverUrl.getPort());
					}

					httpclient
							.getConnectionManager()
							.getSchemeRegistry()
							.register(
									new Scheme(serverUrl.getProtocol(), mysf,
											serverPort));

				} catch (Exception e) {
					CYLog.d(LOG_TAG,NetworkUtil.class, e.toString());
				}
			}
		} else if (CYConstants.SDK_SECURITY_LEVEL.equals("1")
		        && url.toLowerCase().startsWith("https")){
			// for https with company cert
			if (serverPort < 0) {
				serverPort = getPort();
			}
			CYLog.d(LOG_TAG,NetworkUtil.class,"InitSSL port is:" + serverPort);
			SchemeRegistry schReg = new SchemeRegistry();			 
			schReg.register(new Scheme("https", SSLCustomSocketFactory
					.getSocketFactory(), serverPort));

			ClientConnectionManager connMgr = new ThreadSafeClientConnManager(
					httpParams, schReg);
			httpclient = new DefaultHttpClient(connMgr, httpParams);
		} else {
		    httpclient = new DefaultHttpClient(httpParams); 
		}
		processCookieRejected(httpclient);
		
		MyMessage message = new MyMessage();
		try {
			HttpPost httppost = new HttpPost(url);

			StringEntity se = new StringEntity("content="
					+ URLEncoder.encode(data), HTTP.UTF_8);

			CYLog.d(LOG_TAG, NetworkUtil.class, "data = " + data);

			se.setContentType("application/x-www-form-urlencoded");
			httppost.setEntity(se);

			HttpResponse response = httpclient.execute(httppost);
			CYLog.d(LOG_TAG,NetworkUtil.class, "Status code="
					+ response.getStatusLine().getStatusCode());

			String returnXML = EntityUtils.toString(response.getEntity());

			int status = response.getStatusLine().getStatusCode();
			String returnContent = URLDecoder.decode(returnXML, "UTF-8");
			CYLog.d(LOG_TAG,NetworkUtil.class, "returnString = " + returnContent);
			//TODO:只要服務端有返回200ok并且返回的是json字符串即可认为发送成功；因为如果发送的数据不完整服务端会返回flag<0；
            //这部分数据按照flag来判断会导致错误数据始终保存在本地
			switch (status) {
            case 200:
                message.setSuccess(isJson(returnContent));
                message.setMsg(returnContent);                
                break;
            default:
                Log.e("error", status + returnContent);
                message.setSuccess(false);
                message.setMsg(returnContent);
                break;
            }
		} catch (Exception e) {
		    message.setSuccess(false);
            message.setMsg(e.toString());   
		}
		return message;
	}

	private static int getPort() {
		String url = CYConstants.BASE_URL.toLowerCase();
		CYLog.d(LOG_TAG,NetworkUtil.class, url);
		int pos = url.indexOf(":");
		pos = url.indexOf(":", pos + 1);
		if (pos > 0) {
			int pos2 = url.indexOf("/", pos + 1);

			if (pos2 > 0) {
				return Integer.parseInt(url.substring(pos + 1, pos2));
			} else {
				return Integer.parseInt(url.substring(pos + 1));
			}
		} else {
			return url.startsWith("https") ? 443 : 80;
		}
	}
	
	private static boolean isJson(String strForValidating) {
	    try {
	        JSONObject jsonObject = new JSONObject(strForValidating);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	private static void processCookieRejected(DefaultHttpClient client) {
	    client.getCookieSpecs().register("esay", new EasyCookieSpecFactory());
	    client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "esay");
	}
	
	private static class EasyCookieSpecFactory implements CookieSpecFactory {
        @Override
        public CookieSpec newInstance(HttpParams arg0) {
            return new BrowserCompatSpec() {

                @Override
                public void validate(Cookie cookie, CookieOrigin origin)
                        throws MalformedCookieException {
                    //do nothing here
                }//public             
            }; // new BrowserCompatSpec
        }//public
	    
	}
}
