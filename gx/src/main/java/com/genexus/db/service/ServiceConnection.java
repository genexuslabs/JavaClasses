package com.genexus.db.service;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

public abstract class ServiceConnection implements Connection, DatabaseMetaData
{
    public String url;
    public Properties props;
    
    public ServiceConnection(String connUrl, Properties connProps)
    {
        this.props = new Properties();
        for(Map.Entry<Object, Object> entry:connProps.entrySet())
            props.setProperty((String)entry.getKey(), (String)entry.getValue());
        this.url = parseUrlAndProps(connUrl);        
    }
    
    private String parseUrlAndProps(String url)
    {
        int index = url.indexOf(';');
        if(index == -1)
            return url;
        else
        {
            String sProps = url.substring(index + 1);
            for(String prop : sProps.split(";"))
            {
                int idx = prop.indexOf('=');
                if(idx == -1)
                    continue;
                props.put(prop.substring(0, idx).trim(), prop.substring(idx + 1).trim());
            }
            return url.substring(0, index);
        }
    }    

    
//----------------------------------------------------------------------------------------------------

    @Override
    public Statement createStatement() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
//        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        return true;
    }

    @Override
    public void commit() throws SQLException
    {
    }

    @Override
    public void rollback() throws SQLException
    {
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getCatalog() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    private int dummyTransactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;

    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        dummyTransactionIsolation = level;
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        return dummyTransactionIsolation;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException
    {
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getHoldability() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Clob createClob() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Blob createBlob() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public NClob createNClob() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getClientInfo(String name) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /* ----------------------------------- DatabaseMetaData interface --------------------------------*/

    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getURL() throws SQLException
    {
        return url;
    }

    @Override
    public String getUserName() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getDriverMajorVersion()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getDriverMinorVersion()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getSQLKeywords() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getNumericFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getStringFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getSystemFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getSearchStringEscape() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsConvert() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getSchemaTerm() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getProcedureTerm() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getCatalogTerm() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getCatalogSeparator() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsUnion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxConnections() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxIndexLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxRowSize() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxStatementLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxStatements() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getSchemas() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getCatalogs() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getTableTypes() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsSavepoints() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getResultSetHoldability() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getSQLStateType() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
