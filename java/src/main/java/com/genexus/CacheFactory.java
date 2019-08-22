package com.genexus;

import com.genexus.db.CacheValue;
import com.genexus.db.InProcessCache;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;

public class CacheFactory {

	public static final ILogger logger = LogManager.getLogger(CacheFactory.class);
    private static volatile ICacheService instance;
    private static Object syncRoot = new Object();
	private static boolean forceHighestTimetoLive = false;
	public static String FORCE_HIGHEST_TIME_TO_LIVE = "FORCE_HIGHEST_TIME_TO_LIVE";
	private static final boolean DEBUG = com.genexus.DebugFlag.DEBUG;

	public static String CACHE_SD = "SD";
	public static String CACHE_DB = "DB";
	public static String CACHE_FL = "FL";

	public static ICacheService getInstance() {
		if (instance == null) {
			synchronized (syncRoot) {
				if (instance == null) {
					GXService providerService = Application.getGXServices().get(GXServices.CACHE_SERVICE);
					if (providerService != null) {
						String warnMsg = "Couldn't create CACHE_PROVIDER as ICacheService: "
								+ providerService.getClassName();
						try {
							logger.info("Loading providerService:" + providerService.getClassName());
							Class<?> type = Class.forName(providerService.getClassName());
							if (type != null) {
								ICacheService cacheInstance = (ICacheService) type.getDeclaredConstructor()
										.newInstance();
								if (cacheInstance != null) {
									instance = cacheInstance;
									if (providerService.getProperties().containsKey(FORCE_HIGHEST_TIME_TO_LIVE)) {
										if (Integer.parseInt(
												providerService.getProperties().get(FORCE_HIGHEST_TIME_TO_LIVE)) == 1) {
											forceHighestTimetoLive = true;
										}
									}
								} else {
									logger.error(warnMsg);
									System.err.println(warnMsg);
								}
							}
						} catch (Exception ex) {
							logger.error(warnMsg, ex);
							System.err.println(warnMsg);
							ex.printStackTrace();
						}
					}
					if (instance == null) {

						instance = new InProcessCache();

					}

					LoadTTLFromPreferences();
				}
			}
		}
		return instance;
	}

	private static void LoadTTLFromPreferences() {
		Preferences prefs = Preferences.getDefaultPreferences();
		for (int i = 0; i < Preferences.CANT_CATS; i++) {
			Preferences.TTL[i] = prefs.getCACHE_TTL(i, Preferences.TTL_NO_CACHE);
			if (Preferences.TTL[i] != Preferences.TTL_NO_CACHE) {
				Preferences.TTL[i] *= Preferences.SECONDS_IN_ONE_MINUTE;
			}
			Preferences.HTL[i] = prefs.getCACHE_HTL(i, Preferences.TTL_NO_EXPIRY);
		}
	}

	public static CacheValue createCacheValue(String key, Object[] parms, int cacheableLevel) {
		if (Preferences.TTL[cacheableLevel] <= Preferences.TTL_NO_CACHE) { // Si no debo cachear este level
			return null;
		}
		CacheValue cacheValue = new CacheValue(key, parms);
		cacheValue.setExpiryTime(Preferences.TTL[cacheableLevel]);
		cacheValue.setExpiryHits(Preferences.HTL[cacheableLevel]);
		return cacheValue;
	}

	public static void restartCache() {
		if (instance != null) {
			if (DEBUG) {
				System.err.println("Restarting cache...");
			}
			instance.clearAllCaches();
		}
	}

	public static boolean getForceHighestTimetoLive() {
		if (!ApplicationContext.getInstance().getReorganization() && getInstance() != null)
			return forceHighestTimetoLive;
		else
			return false;
	}
}
