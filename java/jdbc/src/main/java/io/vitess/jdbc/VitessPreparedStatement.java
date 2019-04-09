/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vitess.jdbc;

import io.vitess.client.Context;
import io.vitess.client.VTGateConnection;
import io.vitess.client.cursor.Cursor;
import io.vitess.client.cursor.CursorWithError;
import io.vitess.mysql.DateTime;
import io.vitess.util.Constants;
import io.vitess.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by harshit.gangal on 25/01/16.
 * <p>
 * This class expected for an sql query and a given set of parameters the DB Call can be made once
 * with any of the following method execute, executeQuery, executeUpdate and executeBatch. After the
 * call, the parameters will be reset and a new set of parameters needs to be provided before
 * calling any of the above method.
 */
public class VitessPreparedStatement extends VitessStatement implements PreparedStatement {
  private static final Logger LOG = LoggerFactory.getLogger(VitessPreparedStatement.class);
  /* Get actual class name to be printed on */
  private final String sql;
  private final Map<String, Object> bindVariables;
  /**
   * Holds batched commands
   */
  private final List<Map<String, ?>> batchedArgs;
  private VitessParameterMetaData parameterMetadata;

  public VitessPreparedStatement(VitessConnection vitessConnection, String sql)
      throws SQLException {
    this(vitessConnection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
  }

  public VitessPreparedStatement(VitessConnection vitessConnection, String sql, int resultSetType,
      int resultSetConcurrency) throws SQLException {
    this(vitessConnection, sql, resultSetType, resultSetConcurrency, Statement.NO_GENERATED_KEYS);
  }

  public VitessPreparedStatement(VitessConnection vitessConnection, String sql,
      int autoGeneratedKeys) throws SQLException {
    this(vitessConnection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
        autoGeneratedKeys);
  }

  public VitessPreparedStatement(VitessConnection vitessConnection, String sql, int resultSetType,
      int resultSetConcurrency, int autoGeneratedKeys) throws SQLException {
    super(vitessConnection, resultSetType, resultSetConcurrency);
    checkSQLNullOrEmpty(sql);
    this.bindVariables = new HashMap<>();
    this.sql = sql;
    this.generatedId = -1;
    this.retrieveGeneratedKeys = (autoGeneratedKeys == Statement.RETURN_GENERATED_KEYS);
    this.batchedArgs = new ArrayList<>();
  }

  public ResultSet executeQuery() throws SQLException {
    checkOpen();
    closeOpenResultSetAndResetCount();

    //Setting to default value
    this.generatedId = -1;

    VTGateConnection vtGateConn = this.vitessConnection.getVtGateConn();

    Cursor cursor;
    try {
      if (vitessConnection.isSimpleExecute() && this.fetchSize == 0) {
        checkAndBeginTransaction();
        Context context = this.vitessConnection.createContext(this.queryTimeoutInMillis);
        long start = System.currentTimeMillis();
        cursor = vtGateConn
            .execute(context, this.sql, this.bindVariables, vitessConnection.getVtSession())
            .checkedGet();
        long duration = System.currentTimeMillis() - start;
        if (duration > 15) {
          LOG.info("Query {} took {} ms with bindings {}", this.sql, duration, this.bindVariables);
        }
      } else {
        Context context = this.vitessConnection.createContext(this.queryTimeoutInMillis);
        cursor = vtGateConn
            .streamExecute(context, this.sql, this.bindVariables, vitessConnection.getVtSession());
      }

      if (null == cursor) {
        throw new SQLException(Constants.SQLExceptionMessages.METHOD_CALL_FAILED);
      }

      this.vitessResultSet = new VitessResultSet(cursor, this);
    } finally {
      this.bindVariables.clear();
    }
    return this.vitessResultSet;
  }

  public int executeUpdate() throws SQLException {
    checkOpen();
    checkNotReadOnly();
    closeOpenResultSetAndResetCount();

    VTGateConnection vtGateConn = this.vitessConnection.getVtGateConn();

    int truncatedUpdateCount;
    Cursor cursor;
    try {
      checkAndBeginTransaction();
      Context context = this.vitessConnection.createContext(this.queryTimeoutInMillis);
      cursor = vtGateConn
          .execute(context, this.sql, this.bindVariables, vitessConnection.getVtSession())
          .checkedGet();

      if (null == cursor) {
        throw new SQLException(Constants.SQLExceptionMessages.METHOD_CALL_FAILED);
      }

      if (!(null == cursor.getFields() || cursor.getFields().isEmpty())) {
        throw new SQLException(Constants.SQLExceptionMessages.SQL_RETURNED_RESULT_SET);
      }

      if (this.retrieveGeneratedKeys) {
        this.generatedId = cursor.getInsertId();
      }

      this.resultCount = cursor.getRowsAffected();

      if (this.resultCount > Integer.MAX_VALUE) {
        truncatedUpdateCount = Integer.MAX_VALUE;
      } else {
        truncatedUpdateCount = (int) this.resultCount;
      }
    } finally {
      this.bindVariables.clear();
    }
    return truncatedUpdateCount;
  }

  public boolean execute() throws SQLException {
    checkOpen();
    closeOpenResultSetAndResetCount();

    if (!maybeSelect(this.sql)) {
      this.executeUpdate();
      return false;
    } else {
      this.executeQuery();
      return true;
    }
  }

  public void clearParameters() throws SQLException {
    checkOpen();
    this.bindVariables.clear();
  }

  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, null);
  }

  public void setBoolean(int parameterIndex, boolean ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setByte(int parameterIndex, byte ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setShort(int parameterIndex, short ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setInt(int parameterIndex, int ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setLong(int parameterIndex, long ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setFloat(int parameterIndex, float ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setDouble(int parameterIndex, double ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setBigDecimal(int parameterIndex, BigDecimal ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setBigInteger(int parameterIndex, BigInteger ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setString(int parameterIndex, String ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setBytes(int parameterIndex, byte[] ignored) throws SQLException {
    checkOpen();
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, ignored);
  }

  public void setDate(int parameterIndex, Date date) throws SQLException {
    checkOpen();
    String dateString = DateTime.formatDate(date);
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, dateString);
  }

  public void setTime(int parameterIndex, Time time) throws SQLException {
    checkOpen();
    String timeString = DateTime.formatTime(time);
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, timeString);
  }

  public void setTimestamp(int parameterIndex, Timestamp timestamp) throws SQLException {
    checkOpen();
    String timestampString = DateTime.formatTimestamp(timestamp);
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, timestampString);
  }

  public void setDate(int parameterIndex, Date date, Calendar cal) throws SQLException {
    checkOpen();
    String formattedDate = DateTime.formatDate(date, cal);
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, formattedDate);
  }

  public void setTime(int parameterIndex, Time time, Calendar cal) throws SQLException {
    checkOpen();
    String formattedTime = DateTime.formatTime(time, cal);
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, formattedTime);
  }

  public void setTimestamp(int parameterIndex, Timestamp timestamp, Calendar cal)
      throws SQLException {
    checkOpen();
    String formattedTimestamp = DateTime.formatTimestamp(timestamp, cal);
    this.bindVariables.put(Constants.LITERAL_V + parameterIndex, formattedTimestamp);
  }

  public void setObject(int parameterIndex, Object object) throws SQLException {
    if (object == null) {
      setNull(parameterIndex, Types.NULL);
    } else if (object instanceof String) {
      setString(parameterIndex, (String) object);
    } else if (object instanceof Short) {
      setShort(parameterIndex, (Short) object);
    } else if (object instanceof Integer) {
      setInt(parameterIndex, (Integer) object);
    } else if (object instanceof Long) {
      setLong(parameterIndex, (Long) object);
    } else if (object instanceof Float) {
      setFloat(parameterIndex, (Float) object);
    } else if (object instanceof Double) {
      setDouble(parameterIndex, (Double) object);
    } else if (object instanceof Boolean) {
      setBoolean(parameterIndex, (Boolean) object);
    } else if (object instanceof Byte) {
      setByte(parameterIndex, (Byte) object);
    } else if (object instanceof Character) {
      setString(parameterIndex, String.valueOf(object));
    } else if (object instanceof Date) {
      setDate(parameterIndex, (Date) object);
    } else if (object instanceof Time) {
      setTime(parameterIndex, (Time) object);
    } else if (object instanceof Timestamp) {
      setTimestamp(parameterIndex, (Timestamp) object);
    } else if (object instanceof BigDecimal) {
      setBigDecimal(parameterIndex, (BigDecimal) object);
    } else if (object instanceof BigInteger) {
      setBigInteger(parameterIndex, (BigInteger) object);
    } else if (object instanceof byte[]) {
      setBytes(parameterIndex, (byte[]) object);
    } else if (getConnection().getTreatUtilDateAsTimestamp() && object instanceof java.util.Date) {
      setTimestamp(parameterIndex, new Timestamp(((java.util.Date) object).getTime()));
    } else {
      throw new SQLException(
          Constants.SQLExceptionMessages.SQL_TYPE_INFER + object.getClass().getCanonicalName());
    }
  }

  /**
   * Add bindVariables to the batch and clear it to have new set of bindVariables.
   */
  public void addBatch() throws SQLException {
    checkOpen();
    this.batchedArgs.add(new HashMap<>(this.bindVariables));
    this.bindVariables.clear();
  }

  /**
   * Clear all the batched bindVariables.
   */
  @Override
  public void clearBatch() throws SQLException {
    checkOpen();
    this.batchedArgs.clear();
  }

  /**
   * Submits a batch of commands to the database for execution and if all commands execute
   * successfully, returns an array of update counts. The array returned is according to the order
   * in which they were added to the batch.
   * <p>
   * If one of the commands in a batch update fails to execute properly, this method throws a
   * <code>BatchUpdateException</code>, and a JDBC driver may or may not continue to process the
   * remaining commands in the batch. If the driver continues processing after a failure, the array
   * returned by the method <code>BatchUpdateException.getUpdateCounts</code> will contain as many
   * elements as there are commands in the batch.
   *
   * @return int[] of results corresponding to each command
   */
  @Override
  public int[] executeBatch() throws SQLException {
    checkOpen();
    // An executeBatch can't contain SELECT statements as defined by the documentation:
    // https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html
    // "This list may contain statements for updating, inserting, or deleting a row; and it may
    // also contain DDL statements such as CREATE TABLE and DROP TABLE. It cannot, however,
    // contain a statement that would produce a ResultSet object, such as a SELECT statement.
    // In other words, the list can contain only statements that produce an update count."
    checkNotReadOnly();

    VTGateConnection vtGateConn;
    List<CursorWithError> cursorWithErrorList;
    List<String> batchedQueries = new ArrayList<>();

    if (0 == batchedArgs.size()) {
      return new int[0];
    }

    try {
      vtGateConn = this.vitessConnection.getVtGateConn();

      this.retrieveGeneratedKeys = true; // mimicking mysql-connector-j
      /*
       * Current api does not support single query and multiple bindVariables list.
       * So, List of the query is created to match the bindVariables list.
       */
      for (int i = 0; i < batchedArgs.size(); ++i) {
        batchedQueries.add(this.sql);
      }

      checkAndBeginTransaction();
      Context context = this.vitessConnection.createContext(this.queryTimeoutInMillis);
      cursorWithErrorList = vtGateConn
          .executeBatch(context, batchedQueries, batchedArgs, vitessConnection.getVtSession())
          .checkedGet();

      if (null == cursorWithErrorList) {
        throw new SQLException(Constants.SQLExceptionMessages.METHOD_CALL_FAILED);
      }

      return this.generateBatchUpdateResult(cursorWithErrorList, batchedQueries);
    } finally {
      this.clearBatch();
    }


  }

  //Methods which are currently not supported

  public ParameterMetaData getParameterMetaData() throws SQLException {
    checkOpen();
    if (this.parameterMetadata == null) {
      this.parameterMetadata = new VitessParameterMetaData(calculateParameterCount());
    }

    return this.parameterMetadata;
  }

  /**
   * This function was ported from mysql-connector-java ParseInfo object and greatly simplified to
   * just the parts for counting parameters
   */
  private int calculateParameterCount() throws SQLException {
    if (sql == null) {
      throw new SQLException(Constants.SQLExceptionMessages.ILLEGAL_VALUE_FOR + ": sql null");
    }

    char quotedIdentifierChar = '`';
    char currentQuoteChar = 0;
    boolean inQuotes = false;
    boolean inQuotedId = false;
    int statementCount = 0;
    int statementLength = sql.length();
    int statementStartPos = StringUtils.findStartOfStatement(sql);

    for (int i = statementStartPos; i < statementLength; ++i) {
      char curChar = sql.charAt(i);

      if (curChar == '\\' && i < (statementLength - 1)) {
        i++;
        continue; // next character is escaped
      }

      // are we in a quoted identifier? (only valid when the id is not inside a 'string')
      if (!inQuotes && curChar == quotedIdentifierChar) {
        inQuotedId = !inQuotedId;
      } else if (!inQuotedId) {
        //only respect quotes when not in a quoted identifier
        if (inQuotes) {
          if (((curChar == '\'') || (curChar == '"')) && curChar == currentQuoteChar) {
            if (i < (statementLength - 1) && sql.charAt(i + 1) == currentQuoteChar) {
              i++;
              continue; // inline quote escape
            }

            inQuotes = !inQuotes;
            currentQuoteChar = 0;
          }
        } else {
          if (curChar == '#'
              || (curChar == '-'
              && (i + 1) < statementLength
              && sql.charAt(i + 1) == '-')) {
            // comment, run out to end of statement, or newline, whichever comes first
            int endOfStmt = statementLength - 1;

            for (; i < endOfStmt; i++) {
              curChar = sql.charAt(i);

              if (curChar == '\r' || curChar == '\n') {
                break;
              }
            }

            continue;
          } else if (curChar == '/' && (i + 1) < statementLength) {
            // Comment?
            char nextChar = sql.charAt(i + 1);
            if (nextChar == '*') {
              i += 2;

              for (int j = i; j < statementLength; j++) {
                i++;
                nextChar = sql.charAt(j);

                if (nextChar == '*' && (j + 1) < statementLength) {
                  if (sql.charAt(j + 1) == '/') {
                    i++;

                    if (i < statementLength) {
                      curChar = sql.charAt(i);
                    }

                    break; // comment done
                  }
                }
              }
            }
          } else if ((curChar == '\'') || (curChar == '"')) {
            inQuotes = true;
            currentQuoteChar = curChar;
          }
        }
      }

      if ((curChar == '?') && !inQuotes && !inQuotedId) {
        statementCount++;
      }
    }

    return statementCount;
  }

  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setAsciiStream(int parameterIndex, InputStream ignored, int length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setBinaryStream(int parameterIndex, InputStream ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setObject(int parameterIndex, Object parameterObject, int targetSqlType,
      int scaleOrLength) throws SQLException {
    if (null == parameterObject) {
      setNull(parameterIndex, Types.OTHER);
    } else {
      try {
        switch (targetSqlType) {
          case Types.BOOLEAN:
            if (parameterObject instanceof Boolean) {
              setBoolean(parameterIndex, (Boolean) parameterObject);
              break;
            } else if (parameterObject instanceof String) {
              setBoolean(parameterIndex, "true".equalsIgnoreCase((String) parameterObject) || !"0"
                  .equalsIgnoreCase((String) parameterObject));
              break;
            } else if (parameterObject instanceof Number) {
              int intValue = ((Number) parameterObject).intValue();
              setBoolean(parameterIndex, intValue != 0);
              break;
            } else {
              throw new SQLException("Conversion from" + parameterObject.getClass().getName()
                  + "to Types.Boolean is not Possible");
            }
          case Types.BIT:
          case Types.TINYINT:
          case Types.SMALLINT:
          case Types.INTEGER:
          case Types.BIGINT:
          case Types.REAL:
          case Types.FLOAT:
          case Types.DOUBLE:
          case Types.DECIMAL:
          case Types.NUMERIC:
            setNumericObject(parameterIndex, parameterObject, targetSqlType, scaleOrLength);
            break;
          case Types.CHAR:
          case Types.VARCHAR:
          case Types.LONGVARCHAR:
            if (parameterObject instanceof BigDecimal) {
              setString(parameterIndex,
                  (StringUtils.fixDecimalExponent((parameterObject).toString())));
            } else {
              setString(parameterIndex, parameterObject.toString());
            }
            break;
          case Types.CLOB:
            if (parameterObject instanceof Clob) {
              setClob(parameterIndex, (Clob) parameterObject);
            } else {
              setString(parameterIndex, parameterObject.toString());
            }
            break;
          case Types.BINARY:
          case Types.VARBINARY:
          case Types.LONGVARBINARY:
          case Types.BLOB:
            if (parameterObject instanceof Blob) {
              setBlob(parameterIndex, (Blob) parameterObject);
            } else {
              setBytes(parameterIndex, (byte[]) parameterObject);
            }
            break;
          case Types.DATE:
          case Types.TIMESTAMP:
            java.util.Date parameterAsDate;
            if (parameterObject instanceof String) {
              ParsePosition pp = new ParsePosition(0);
              DateFormat sdf = new SimpleDateFormat(
                  StringUtils.getDateTimePattern((String) parameterObject, false), Locale.US);
              parameterAsDate = sdf.parse((String) parameterObject, pp);
            } else {
              parameterAsDate = (java.util.Date) parameterObject;
            }
            switch (targetSqlType) {
              case Types.DATE:
                if (parameterAsDate instanceof Date) {
                  setDate(parameterIndex, (Date) parameterAsDate);
                } else {
                  setDate(parameterIndex, new Date(parameterAsDate.getTime()));
                }
                break;
              case Types.TIMESTAMP:
                if (parameterAsDate instanceof Timestamp) {
                  setTimestamp(parameterIndex, (Timestamp) parameterAsDate);
                } else {
                  setTimestamp(parameterIndex, new Timestamp(parameterAsDate.getTime()));
                }
                break;
            }
            break;
          case Types.TIME:
            if (parameterObject instanceof String) {
              DateFormat sdf = new SimpleDateFormat(
                  StringUtils.getDateTimePattern((String) parameterObject, true), Locale.US);
              setTime(parameterIndex, new Time(sdf.parse((String) parameterObject).getTime()));
            } else if (parameterObject instanceof Timestamp) {
              Timestamp timestamp = (Timestamp) parameterObject;
              setTime(parameterIndex, new Time(timestamp.getTime()));
            } else {
              setTime(parameterIndex, (Time) parameterObject);
            }
            break;
          default:
            throw new SQLFeatureNotSupportedException(
                Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
        }
      } catch (Exception ex) {
        throw new SQLException(ex);
      }
    }
  }

  public void setAsciiStream(int parameterIndex, InputStream ignored, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setBinaryStream(int parameterIndex, InputStream ignored, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setCharacterStream(int parameterIndex, Reader reader, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setUnicodeStream(int parameterIndex, InputStream ignored, int length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setRef(int parameterIndex, Ref ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setBlob(int parameterIndex, Blob ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setClob(int parameterIndex, Clob clob) throws SQLException {
    checkOpen();
    if (clob.length() > Integer.MAX_VALUE) {
      throw new SQLFeatureNotSupportedException(
          String.format("Clob size over %d not support", Integer.MAX_VALUE),
          Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
    }
    // Clob uses 1-based indexing!
    this.bindVariables
        .put(Constants.LITERAL_V + parameterIndex, clob.getSubString(1, (int) clob.length()));
  }

  public void setArray(int parameterIndex, Array ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setURL(int parameterIndex, URL ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setRowId(int parameterIndex, RowId ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setNString(int parameterIndex, String value) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setNCharacterStream(int parameterIndex, Reader value, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setBlob(int parameterIndex, InputStream inputStream, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setAsciiStream(int parameterIndex, InputStream ignored) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setBinaryStream(int parameterIndex, InputStream ignored, int length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException(
        Constants.SQLExceptionMessages.SQL_FEATURE_NOT_SUPPORTED);
  }

  public void setObject(int parameterIndex, Object parameterObject, int targetSqlType)
      throws SQLException {
    if (!(parameterObject instanceof BigDecimal)) {
      setObject(parameterIndex, parameterObject, targetSqlType, 0);
    } else {
      setObject(parameterIndex, parameterObject, targetSqlType,
          ((BigDecimal) parameterObject).scale());
    }
  }

  private void setNumericObject(int parameterIndex, Object parameterObj, int targetSqlType,
      int scale) throws SQLException {
    Number numberParam;
    if (parameterObj instanceof Boolean) {
      numberParam = (Boolean) parameterObj ? Integer.valueOf(1) : Integer.valueOf(0);
    } else if (parameterObj instanceof String) {
      switch (targetSqlType) {
        case Types.BIT:
          if ("1".equals(parameterObj) || "0".equals(parameterObj)) {
            numberParam = Integer.valueOf((String) parameterObj);
          } else {
            boolean parameterAsBoolean = "true".equalsIgnoreCase((String) parameterObj);
            numberParam = parameterAsBoolean ? Integer.valueOf(1) : Integer.valueOf(0);
          }
          break;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
          numberParam = Integer.valueOf((String) parameterObj);
          break;

        case Types.BIGINT:
          numberParam = Long.valueOf((String) parameterObj);
          break;

        case Types.REAL:
          numberParam = Float.valueOf((String) parameterObj);
          break;

        case Types.FLOAT:
        case Types.DOUBLE:
          numberParam = Double.valueOf((String) parameterObj);
          break;

        case Types.DECIMAL:
        case Types.NUMERIC:
        default:
          numberParam = new java.math.BigDecimal((String) parameterObj);
      }
    } else {
      numberParam = (Number) parameterObj;
    }
    switch (targetSqlType) {
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
        setInt(parameterIndex, numberParam.intValue());
        break;

      case Types.BIGINT:
        setLong(parameterIndex, numberParam.longValue());
        break;

      case Types.REAL:
        setFloat(parameterIndex, numberParam.floatValue());
        break;

      case Types.FLOAT:
      case Types.DOUBLE:
        setDouble(parameterIndex, numberParam.doubleValue());
        break;

      case Types.DECIMAL:
      case Types.NUMERIC:

        if (numberParam instanceof java.math.BigDecimal) {
          BigDecimal scaledBigDecimal;
          try {
            scaledBigDecimal = ((java.math.BigDecimal) numberParam).setScale(scale);
          } catch (ArithmeticException ex) {
            try {
              scaledBigDecimal = ((java.math.BigDecimal) numberParam)
                  .setScale(scale, BigDecimal.ROUND_HALF_UP);
            } catch (ArithmeticException arEx) {
              throw new SQLException(
                  "Can't set the scale of '" + scale + "' for Decimal Argument" + numberParam);
            }
          }
          setBigDecimal(parameterIndex, scaledBigDecimal);
        } else if (numberParam instanceof java.math.BigInteger) {
          setBigInteger(parameterIndex, (BigInteger) numberParam);
        } else {
          setBigDecimal(parameterIndex, new java.math.BigDecimal(numberParam.doubleValue()));
        }
        break;
    }
  }

}
