package com.genexus.specific.java;

import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXDBMS;

public class BatchUpdateCursor implements com.genexus.common.interfaces.IExtensionBatchUpdateCursor {

	@Override
	public boolean supportsSavePoint(Object obj) {
		DataSource ds = (DataSource) obj;
		return ( ds.dbms.getId() == GXDBMS.DBMS_SQLSERVER || 
                 ds.dbms.getId() == GXDBMS.DBMS_AS400 || 
                 ds.dbms.getId() == GXDBMS.DBMS_DB2  || 
                 ds.dbms.getId() == GXDBMS.DBMS_POSTGRESQL || 
                 ds.dbms.getId() == GXDBMS.DBMS_ORACLE );
	}

}
