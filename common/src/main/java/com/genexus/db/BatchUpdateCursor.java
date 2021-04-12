package com.genexus.db;

import java.sql.SQLException;

import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.common.interfaces.SpecificImplementation;

import java.lang.reflect.Method;

import java.lang.reflect.InvocationTargetException;

public class BatchUpdateCursor extends UpdateCursor {
	Object[] errorRecords;
	int errorRecordIndex;

	public BatchUpdateCursor(String cursorId, String sqlSentence, int errMask, String tableName) {
		super(cursorId, sqlSentence, errMask);
	}

	public BatchUpdateCursor(String cursorId, String sqlSentence, int errMask) {
		super(cursorId, sqlSentence, errMask);
	}

	public BatchUpdateCursor(String cursorId, String sqlSentence) {
		this(cursorId, sqlSentence, 0);
	}

	byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds,
			Object[] params) throws SQLException {
		status = 0;
		mPreparedStatement = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId, mSQLSentence, false,
				true);
		return null;
	}

	boolean supportsSavePoint(AbstractDataSource ds) {
		if (SpecificImplementation.BatchUpdateCursor != null)
			return SpecificImplementation.BatchUpdateCursor.supportsSavePoint(ds);
		return false;
	}

	void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException {
		errorRecordIndex = -1;
		int[] res = null;
		Object savepoint = null;
		try {
			if (!mPreparedStatement.getConnection().getAutoCommit() && this.supportsSavePoint(ds)) {
				savepoint = setSavePoint(mPreparedStatement, mCursorId);
				res = mPreparedStatement.executeBatch();
				releaseSavepoint(mPreparedStatement, savepoint);
			} else {
				res = mPreparedStatement.executeBatch();
			}
		} catch (SQLException ex) {
			if (savepoint != null) {
				try {
					if (!mPreparedStatement.getConnection().getAutoCommit() && this.supportsSavePoint(ds)) {
						rollbackSavepoint(mPreparedStatement, savepoint);
					}
				} catch (SQLException ex1) {

					throw ex1;
				}
			}
			throw ex;
		}
		if (res != null && res.length == 0) {
			status = 1;
		}
	}

	private Object setSavePoint(IGXPreparedStatement stmt, String savepointName) throws SQLException {

		Class<?> parmTypes[] = new Class[1];
		parmTypes[0] = String.class;
		Method setSavepointMethod = null;
		try {
			setSavepointMethod = stmt.getConnection().getClass().getMethod("setSavepoint", parmTypes);
		} catch (SQLException ex) {
			throw ex;
		} catch (SecurityException ex) {
			throw new SQLException(ex.getMessage());
		} catch (NoSuchMethodException ex) {
			throw new SQLException(ex.getMessage());
		}
		Object argList[] = new Object[1];
		argList[0] = mCursorId;
		try {
			return setSavepointMethod.invoke(stmt.getConnection(), argList);
		} catch (SQLException ex1) {
			throw ex1;
		} catch (InvocationTargetException ex1) {
			throw new SQLException(ex1.getTargetException().getMessage());
		} catch (IllegalArgumentException ex1) {
			throw new SQLException(ex1.getMessage());
		} catch (IllegalAccessException ex1) {
			throw new SQLException(ex1.getMessage());
		}
	}

	private void releaseSavepoint(IGXPreparedStatement stmt, Object savepoint) throws SQLException {
		/*
		 * Class parmTypes[] = new Class[1]; Class savepointClass = null; try {
		 * savepointClass = Class.forName("java.sql.Savepoint"); } catch
		 * (ClassNotFoundException ex2) { throw new SQLException(ex2.getMessage()); }
		 * parmTypes = new Class[1]; parmTypes[0] = savepointClass; Method
		 * setSavepointMethod = null; try { setSavepointMethod =
		 * stmt.getConnection().getClass().getMethod( "releaseSavepoint", parmTypes); }
		 * catch (SQLException ex) { throw ex; } catch (SecurityException ex) { throw
		 * new SQLException(ex.getMessage()); } catch (NoSuchMethodException ex) { throw
		 * new SQLException(ex.getMessage()); }
		 * 
		 * Object argList[] = new Object[1]; argList[0] = savepoint; try {
		 * setSavepointMethod.invoke(stmt.getConnection(), argList); } catch
		 * (SQLException ex1) { throw ex1; } catch (InvocationTargetException ex1) {
		 * throw new SQLException(ex1.getTargetException().getMessage()); catch
		 * (IllegalArgumentException ex1) { throw new SQLException(ex1.getMessage()); }
		 * catch (IllegalAccessException ex1) { throw new
		 * SQLException(ex1.getMessage()); }
		 */
	}

	private void rollbackSavepoint(IGXPreparedStatement stmt, Object savepoint) throws SQLException {

		Class<?> parmTypes[] = new Class[1];
		Class<?> savepointClass = null;
		Method setSavepointMethod = null;
		try {
			savepointClass = Class.forName("java.sql.Savepoint");
		} catch (ClassNotFoundException ex2) {
			throw new SQLException(ex2.getMessage());
		}
		parmTypes = new Class[1];
		parmTypes[0] = savepointClass;

		try {
			setSavepointMethod = stmt.getConnection().getClass().getMethod("rollback", parmTypes);
		} catch (SQLException ex) {
			throw ex;
		} catch (SecurityException ex) {
			throw new SQLException(ex.getMessage());
		} catch (NoSuchMethodException ex) {
			throw new SQLException(ex.getMessage());
		}
		Object argList[] = new Object[1];
		argList[0] = savepoint;
		try {
			setSavepointMethod.invoke(stmt.getConnection(), argList);
		} catch (SQLException ex1) {
			throw ex1;
		} catch (InvocationTargetException ex1) {
			throw new SQLException(ex1.getTargetException().getMessage());
		} catch (IllegalArgumentException ex1) {
			throw new SQLException(ex1.getMessage());
		} catch (IllegalAccessException ex1) {
			throw new SQLException(ex1.getMessage());
		}
	}

	public void notInUse() {
		mPreparedStatement.notInUse();
	}

	void addBatch(Object[] parms) throws SQLException {
		mPreparedStatement.addBatch(parms);
	}

	int getBatchSize() {
		if (mPreparedStatement != null)
			return mPreparedStatement.getBatchSize();
		else
			return 0;
	}

	void setBatchSize(int size) {
		mPreparedStatement.setBatchSize(size);
	}

	void onCommitEvent(Object instance, String method) {
		mPreparedStatement.setOnCommitInstance(instance);
		mPreparedStatement.setOnCommitMethod(method);
	}

	int getRecordCount() {
		return mPreparedStatement.getRecordCount();
	}

	int readNextErrorRecord() {
		if (errorRecordIndex < mPreparedStatement.getRecordCount() - 1) {
			errorRecordIndex++;
			return 1;
		} else {
			return 0;
		}
	}

	protected void close() {
		mPreparedStatement.resetRecordCount();
	}

	public boolean pendingRecords() {

		return mPreparedStatement.getRecordCount() > 0;
	}

	public void beforeCommitEvent() throws SQLException {
		try {
			DynamicExecute.dynamicInstaceExecute(mPreparedStatement.getOnCommitInstance(),
					mPreparedStatement.getOnCommitMethod(), null);
		} catch (Exception ex) {
			throw new SQLException(ex.getMessage());
		}

	}

	public Object[] getBlockRecords() {
		return mPreparedStatement.getBatchRecords();
	}

	public boolean isBatchCursor() {
		return true;
	}

}
