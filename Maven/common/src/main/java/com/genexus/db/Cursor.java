package com.genexus.db;

import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.common.interfaces.SpecificImplementation;
import java.sql.SQLException;

public abstract class Cursor
{
	public static final int DUPLICATE			= 1;
	public static final int DATA_TRUNCATION			= 2;
	public static final int EOF 				= 101;
	public static final int LOCKED 				= 103;
	public static final int OBJECT_NOT_FOUND 		= 105;
	public static final int DATABASE_ALREADY_EXISTS 	= 106;
	public static final int PARENT_PRIMARY_KEY_NOTFOUND 	= 500;
	public static final int REFERENTIAL			= 600;
	public static final int UNEXPECTED_DBMS_ERROR 		= 999;

	protected int status;
	protected String 	mCursorId;
	protected String 	mSQLSentence;
	boolean dynStatement = false;
	protected int errMask;

	IGXPreparedStatement mPreparedStatement;

	public Cursor(String cursorId, String sqlSentence, int errMask)
	{
		this.mCursorId	  		= cursorId;
		this.mSQLSentence  		= sqlSentence;
		this.errMask			= errMask;
	}

	abstract byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException;


	abstract void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException;

        public int getStmtHandle(){
            return mPreparedStatement.getHandle();
        }

        protected void close() throws SQLException{
            throw new SQLException("Close operation is not implemented in " + this.getClass().getName());
        }

        public String getCursorId()
        {
            return this.mCursorId;
        }

        public String getSQLSentence()
        {
            return this.mSQLSentence;
        }

        public IGXPreparedStatement getStatement()
        {
            return this.mPreparedStatement;
        }

        protected boolean isForFirst()
        {
            return false;
        }
        public boolean isBatchCursor(){
            return false;
        }

        public boolean isCurrentOf() {
            return false;
        }

		public int getCacheableLevel(int cacheableLvl){
			if (SpecificImplementation.Cursor != null)
				return SpecificImplementation.Cursor.getCacheableLevel(cacheableLvl);
			return 0;
		}

}
