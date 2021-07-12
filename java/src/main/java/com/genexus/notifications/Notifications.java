package com.genexus.notifications;

import com.genexus.notifications.android.MessageUtil;
import com.genexus.notifications.properties.Certificates;
import com.genexus.notifications.properties.Properties;

import javapns.Push;
import javapns.notification.Payload;


public class Notifications {
	private static final String PAYLOAD_FORMAT = "{\"aps\":{\"alert\":\"%s\"},\"m\":\"%s\",\"a\":\"%s\",\"t\":\"%s\",\"p\":%s}";
	private static final String PAYLOAD_BADGE_FORMAT = "{\"aps\":{\"badge\":%d,\"sound\":\"%s\"}}";
    private static final String PAYLOAD_RESET = "{\"aps\":}";
	
	private static final short IOS = 0;
	private static final short ANDROID = 1;
	private static final short BB = 2;
	private static final short WPHONE = 3;
	
	private int _error = 0;
	private String _customError = "";
	
	public int Call(String applicationId, short deviceType, String deviceToken, String alertMessage) {
		return sendInternal(applicationId, deviceType, deviceToken, alertMessage, "", null);
	}
	
	public int CallAction(String applicationId, short deviceType, String deviceToken, String alertMessage, String action, NotificationParameters props) {
		return sendInternal(applicationId, deviceType, deviceToken, alertMessage, action,props);
	}
	
	public int IOSSetBadge(String applicationId, String deviceToken, int badgeNumber, String sound) {
		Properties cert = Certificates.getInstance().getPropertiesFor(applicationId);
		if (cert == null)
			return 2;
			
		if(sound == null || sound.length() == 0)
			sound = "default";
			
		String payloadStr = String.format(PAYLOAD_BADGE_FORMAT, badgeNumber, sound);
		
		try {
			Payload payload = new GxPayload(payloadStr);
			Push.payload(payload, cert.getiOScertificate(), cert.getiOScertificatePassword(), !cert.getiOSUseSandboxServer(), deviceToken);
			
		}catch(Exception e) {
			_customError = e.getMessage();
			return 3;
		}
		return 0;
	}
	
	public int IOSResetBadge(String applicationId, String deviceToken) {
		Properties cert = Certificates.getInstance().getPropertiesFor(applicationId);
		if (cert == null)
			return 2;
			
		try {
			Payload payload = new GxPayload(PAYLOAD_RESET);
			Push.payload(payload, cert.getiOScertificate(), cert.getiOScertificatePassword(), !cert.getiOSUseSandboxServer(), deviceToken);
			
		}catch(Exception e) {
			_customError = e.getMessage();
			return 3;
		}
		return 0;
	}
	
	private int sendInternal(String applicationId, short deviceType, String deviceToken, String alertMessage, String action, NotificationParameters props) {
		switch (deviceType) {
		case IOS:
			_error = sendIOS(applicationId, deviceToken.trim(), alertMessage, action, props);
			break;
		case ANDROID:
			_error = sendAndroid(applicationId, deviceToken, alertMessage, action, props);
			break;
		default:
			_error = -1;
			break;
		}
		return getLastErrorCode();
	}
	
	private int sendIOS(String applicationId, String deviceToken, String alert, String action, NotificationParameters props) {
		if (props == null)
			props = new NotificationParameters();
		Properties cert = Certificates.getInstance().getPropertiesFor(applicationId);
		if (cert == null)
			return 2;
		
		String payloadStr = String.format(PAYLOAD_FORMAT, alert, applicationId, action, cert.getType(), props.toJson());
		
		try {
			Payload payload = new GxPayload(payloadStr);
			Push.payload(payload, cert.getiOScertificate(), cert.getiOScertificatePassword(), !cert.getiOSUseSandboxServer(), deviceToken);
			
		}catch(Exception e) {
			_customError = e.getMessage();
			return 3;
		}
		return 0;
	}
	
	
	private int sendAndroid(String applicationId, String deviceToken, String alert, String action, NotificationParameters props) {
		if (props == null)
			props = new NotificationParameters();
		Properties cert = Certificates.getInstance().getPropertiesFor(applicationId);
		if (cert == null)
			return 2;
	
		// new AndroidUserToken for gcm getting from config file now
		String AndroidUserToken = cert.getAndroidAPIKey();
		try {
			MessageUtil.sendMessage(AndroidUserToken, deviceToken, alert, action);
			
		}catch(Exception e) {
			_customError = e.getMessage();
			return 3;
		}
		return 0;
	}
	
	public int getLastErrorCode() {
		return _error;
	}
	
	public String getLastErrorDescription() {
		switch (_error) {
		case 0:
			return "OK";
		case -1:
			return "Unknown device type";
		case 2:
			return "Invalid Application Id";
		case 3:
			return _customError;
		}
		return "";
	}
	
	class GxPayload extends Payload {
		private String _data;
		GxPayload(String data) {
			_data = data;
		}
		
		@Override
		public String toString() {
			return _data;
		}
	}
}
