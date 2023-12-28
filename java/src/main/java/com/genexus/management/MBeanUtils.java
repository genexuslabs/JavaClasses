package com.genexus.management;

import java.util.ArrayList;
import java.util.Vector;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.genexus.db.CacheValue;
import com.genexus.db.InProcessCache;
import com.genexus.db.LocalUserInformation;
import com.genexus.db.Namespace;
import com.genexus.db.ServerUserInformation;
import com.genexus.db.driver.ConnectionPool;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;

public class MBeanUtils {
	private static MBeanServer mbs = null;
	private static Vector < ObjectName > registeredObjects = new Vector<>();
	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(MBeanUtils.class);
	public MBeanUtils() {}
	private static MBeanServer getMBeanServer() {
		if (mbs == null) {
			try {
				ArrayList list = MBeanServerFactory.findMBeanServer(null);
				if (list.size() > 0)
					mbs = (MBeanServer) list.get(0);
			} catch (Throwable t) {
				logger.error(t);
				System.exit(1);
			}
		}
		return mbs;
	}

	public static void registerBean(Object mbean, String sName) {
		ObjectName name = null;
		try {
			name = new ObjectName(sName);
		} catch (MalformedObjectNameException e1) {
			logger.error(e1);
		}
		if (name == null)
			return;
		try {
			mbs.getObjectInstance(name);
		} catch (InstanceNotFoundException ex) {
			try {
				mbs.registerMBean(mbean, name);
				registeredObjects.addElement(name);
			} catch (javax.management.InstanceAlreadyExistsException e) {
				logger.error(e);
			} catch (javax.management.MBeanRegistrationException e) {
				logger.error(e);
			} catch (javax.management.NotCompliantMBeanException e) {
				logger.error(e);
			}
		}
	}

	public static void createMBean(Namespace namespace) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		NamespaceJMX mbean = new NamespaceJMX(namespace);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.ApplicationName,name= " + namespace.getName());
	}

	public static void createMBean(ServerUserInformation serverUserInfo, com.genexus.ModelContext context) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		ServerUserInformationJMX mbean = new ServerUserInformationJMX(serverUserInfo, context);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.ApplicationName.User,ApplicationName=" + serverUserInfo.getNamespace().getName() + ",name=User " + serverUserInfo.getHandle());
	}

	public static void createMBean(LocalUserInformation localUserInfo) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		LocalUserInformationJMX mbean = new LocalUserInformationJMX(localUserInfo);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.ApplicationName.User,ApplicationName=" + localUserInfo.getNamespace().getName() + ",name=User " + localUserInfo.getHandle());

	}

	public static void createMBean(DataSource dataSource) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		DataSourceJMX mbean = new DataSourceJMX(dataSource);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.ApplicationName.DataStore,ApplicationName=" + dataSource.getNamespace() + ",name=DataStore " + dataSource.name);
	}

	public static void createMBean(ConnectionPool connectionPool) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		ConnectionPoolJMX mbean = new ConnectionPoolJMX(connectionPool);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.ApplicationName.DataStore.ConnectionPool,ApplicationName=" + connectionPool.getDataSource().getNamespace() + ",DataStore=" + connectionPool.getDataSource().name + ",name=R/W pool");
	}

	public static void createMBean(PoolingHttpClientConnectionManager connectionPool) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		HTTPPoolJMX mbean = new HTTPPoolJMX(connectionPool);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.HTTPPool,ApplicationName=Http connection pool, Http connection pool id =" + connectionPool.hashCode());
	}

	public static void createMBean(HttpRoute httpRoute) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		HTTPConnectionJMX mbean = new HTTPConnectionJMX(httpRoute);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.HTTPPool.HTTPConnection,ApplicationName=" + httpRoute.getTargetHost().getHostName() + ",Port=" + httpRoute.getTargetHost().getPort() + ",Http connection id=" + System.identityHashCode(httpRoute));
	}

	public static void createMBean(GXConnection connection) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		ConnectionJMX mbean = new ConnectionJMX(connection);
		registerBean(mbean, "com.genexus.management:type=GeneXusApplicationServer.ApplicationName.DataStore.ConnectionPool.Connection,ApplicationName=" + connection.getDataSource().getNamespace() + ",DataStore=" + connection.getDataSource().name + ",ConnectionPool=R/W pool" + ",name=" + connection.getId());
	}

	public static void createMBean(InProcessCache cache) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		CacheJMX mbean = new CacheJMX(cache);
		registerBean(mbean, "com.genexus.management:type=InProcessCache");
	}

	public static void createMBean(CacheValue cacheValue) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		CacheItemJMX mbean = new CacheItemJMX(cacheValue);
		registerBean(mbean, "com.genexus.management:type=InProcessCache.Item,name=" + cacheValue.hashCode());
	}

	public static void destroyMBean(ServerUserInformation serverUserInfo) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		try {
			ObjectName name = new ObjectName("com.genexus.management:type=GeneXusApplicationServer.ApplicationName.User,ApplicationName=" + serverUserInfo.getNamespace().getName() + ",name=User " + serverUserInfo.getHandle());
			registeredObjects.removeElement(name);
			mbs.unregisterMBean(name);
		} catch (javax.management.MalformedObjectNameException e) {
			logger.error(e);
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
	}

	public static void destroyMBean(LocalUserInformation localUserInfo) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		try {
			ObjectName name = new ObjectName("com.genexus.management:type=GeneXusApplicationServer.ApplicationName.User,ApplicationName=" + localUserInfo.getNamespace().getName() + ",name=User " + localUserInfo.getHandle());
			registeredObjects.removeElement(name);
			mbs.unregisterMBean(name);
		} catch (javax.management.MalformedObjectNameException e) {
			logger.error(e);
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
	}

	public static void destroyMBean(GXConnection connection) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		try {
			ObjectName name = new ObjectName("com.genexus.management:type=GeneXusApplicationServer.ApplicationName.DataStore.ConnectionPool.Connection,ApplicationName=" + connection.getDataSource().getNamespace() + ",DataStore=" + connection.getDataSource().name + ",ConnectionPool=R/W pool" + ",name=" + connection.getId());
			registeredObjects.removeElement(name);
			mbs.unregisterMBean(name);
		} catch (javax.management.MalformedObjectNameException e) {
			logger.error(e);
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
	}

	public static void destroyMBean(HttpRoute httpRoute) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		try {
			ObjectName name = new ObjectName("com.genexus.management:type=GeneXusApplicationServer.HTTPPool.HTTPConnection,ApplicationName=" + httpRoute.getTargetHost().getHostName() + ",Port=" + httpRoute.getTargetHost().getPort() + ",Http connection id=" + System.identityHashCode(httpRoute));
			registeredObjects.removeElement(name);
			mbs.unregisterMBean(name);
		} catch (javax.management.MalformedObjectNameException e) {
			logger.error(e);
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
	}

	public static void destroyMBeanCache() {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		try {
			ObjectName name = new ObjectName("com.genexus.management:type=InProcessCache");
			registeredObjects.removeElement(name);
			mbs.unregisterMBean(name);
		} catch (javax.management.MalformedObjectNameException e) {
			logger.error(e);
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
	}

	public static void destroyMBean(CacheValue cacheValue) {
		MBeanServer mbs = getMBeanServer();
		if (mbs == null)
			return;
		try {
			ObjectName name = new ObjectName("com.genexus.management:type=InProcessCache.Item,name=" + cacheValue.hashCode());
			registeredObjects.removeElement(name);
			mbs.unregisterMBean(name);
		} catch (javax.management.MalformedObjectNameException e) {
			logger.error(e);
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
	}

	public static void unregisterObjects() {
		try {
			for (int i = 0; i < registeredObjects.size(); i++) {
				mbs.unregisterMBean(registeredObjects.elementAt(i));
			}
		} catch (javax.management.InstanceNotFoundException e) {
			logger.error(e);
		} catch (javax.management.MBeanRegistrationException e) {
			logger.error(e);
		}
		mbs = null;
	}
}