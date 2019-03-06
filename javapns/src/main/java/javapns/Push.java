package javapns;

import java.util.*;

import javapns.devices.*;
import javapns.devices.implementations.basic.*;
import javapns.feedback.*;
import javapns.notification.*;

/**
 * <p>Main class for easily interacting with the Apple Push Notification System</p>
 * 
 * <p>This is the best starting point for pushing simple or custom notifications,
 * or for contacting the Feedback Service to cleanup your list of devices.</p>
 * 
 * <p>The <b>javapns</b> library also includes more advanced options such as
 * multithreaded transmission, special payloads, JPA-support, and more.
 * See the library's documentation at <a href="http://code.google.com/p/javapns/">http://code.google.com/p/javapns/</a>
 * for more information.</p>
 * 
 * @author Sylvain Pedneault
 */
public class Push {

	/**
	 * Push a simple alert to one or more devices.
	 * 
	 * @param message the alert message to push.
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> alert(String message, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.alert(message), keystore, password, production, tokens);
	}


	/**
	 * Push a simple badge number to one or more devices.
	 * 
	 * @param badge the badge number to push.
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> badge(int badge, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.badge(badge), keystore, password, production, tokens);
	}


	/**
	 * Push a simple sound name to one or more devices.
	 * 
	 * @param sound the sound name (stored in the client app) to push.
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> sound(String sound, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.sound(sound), keystore, password, production, tokens);
	}


	/**
	 * Push a notification combining an alert, a badge and a sound. 
	 * 
	 * @param message the alert message to push (set to null to skip).
	 * @param badge the badge number to push (set to -1 to skip).
	 * @param sound the sound name to push (set to null to skip).
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> combined(String message, int badge, String sound, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.combined(message, badge, sound), keystore, password, production, tokens);
	}


	/**
	 * Push a content-available notification for Newsstand.
	 * 
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> contentAvailable(Object keystore, String password, boolean production, String... tokens) {
		return payload(NewsstandNotificationPayload.contentAvailable(), keystore, password, production, tokens);
	}


	/**
	 * Push a preformatted payload.
	 * This is a convenience method for passing a List of tokens instead of an array.
	 * 
	 * @param payload a simple or complex payload to push.
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> payload(Payload payload, Object keystore, String password, boolean production, List<String> tokens) {
		return payload(payload, keystore, password, production, tokens.toArray(new String[0]));
	}


	/**
	 * Push a preformatted payload.
	 * 
	 * @param payload a simple or complex payload to push.
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens one or more device tokens to push to.
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> payload(Payload payload, Object keystore, String password, boolean production, String... tokens) {
		List<PushedNotification> devices = new Vector<PushedNotification>();
		if (payload == null) return devices;
		PushNotificationManager pushManager = new PushNotificationManager();
		try {
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
			pushManager.initializeConnection(server);
			for (String token : tokens) {
				Device device = new BasicDevice(token);
				PushedNotification notification = pushManager.sendNotification(device, payload, false);
				devices.add(notification);
			}
		} catch (Exception e) {
			System.out.println("Error pushing notification(s):");
			e.printStackTrace();
		} finally {
			try {
				pushManager.stopConnection();
			} catch (Exception e) {
			}
		}
		return devices;
	}


	/**
	 * Push a different preformatted payload for each device.
	 * This is a convenience method for passing a List of PayloadPerDevice instead of an array.
	 * 
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param payloadDevicePairs a list of joint payloads and devices to push
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> payloads(Object keystore, String password, boolean production, List<PayloadPerDevice> payloadDevicePairs) {
		return payloads(keystore, password, production, payloadDevicePairs.toArray(new PayloadPerDevice[0]));
	}


	/**
	 * Push a different preformatted payload for each device.
	 * 
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @param payloadDevicePairs a list of joint payloads and devices to push
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 */
	public static List<PushedNotification> payloads(Object keystore, String password, boolean production, PayloadPerDevice... payloadDevicePairs) {
		List<PushedNotification> devices = new Vector<PushedNotification>();
		if (payloadDevicePairs == null) return devices;
		PushNotificationManager pushManager = new PushNotificationManager();
		try {
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
			pushManager.initializeConnection(server);
			for (PayloadPerDevice ppd : payloadDevicePairs) {
				Device device = ppd.getDevice();
				Payload payload = ppd.getPayload();
				PushedNotification notification = pushManager.sendNotification(device, payload, false);
				devices.add(notification);
			}
		} catch (Exception e) {
			System.out.println("Error pushing notification(s):");
			e.printStackTrace();
		} finally {
			try {
				pushManager.stopConnection();
			} catch (Exception e) {
			}
		}
		return devices;
	}


	/**
	 * <p>Retrieve a list of devices that should be removed from future notification lists.</p>
	 * 
	 * <p>Devices in this list are ones that you previously tried to push a notification to,
	 * but to which Apple could not actually deliver because the device user has either
	 * opted out of notifications, has uninstalled your application, or some other conditions.</p>
	 * 
	 * <p>Important: Apple's Feedback Service always resets its list of inactive devices
	 * after each time you contact it.  Calling this method twice will not return the same
	 * list of devices!</p>
	 * 
	 * <p>Please be aware that Apple does not specify precisely when a device will be listed
	 * by the Feedback Service.  More specifically, it is unlikely that the device will
	 * be  listed immediately if you uninstall the application during testing.  It might
	 * get listed after some number of notifications couldn't reach it, or some amount of
	 * time has elapsed, or a combination of both.</p>
	 * 
	 * <p>Further more, if you are using Apple's sandbox servers, the Feedback Service will
	 * probably not list your device if you uninstalled your app and it was the last one
	 * on your device that was configured to receive notifications from the sandbox.
	 * See the library's wiki for more information.</p>
	 * 
	 * @param keystore a PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password the keystore's password.
	 * @param production true to use Apple's production servers, false to use the sandbox servers.
	 * @return a list of devices that are inactive.
	 */
	public static List<Device> feedback(Object keystore, String password, boolean production) {
		List<Device> devices = new Vector<Device>();
		try {
			FeedbackServiceManager feedbackManager = new FeedbackServiceManager();
			AppleFeedbackServer server = new AppleFeedbackServerBasicImpl(keystore, password, production);
			devices.addAll(feedbackManager.getDevices(server));
		} catch (Exception e) {
			System.out.println("Error pushing notification(s):");
			e.printStackTrace();
		}
		return devices;
	}

}
