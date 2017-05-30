package org.inria.jdbc;

/**
 * A ResultSetMetaData object can be used to find out about the types
 * and properties of the columns in a ResultSet.
 */

/*=============================================================================

Name: ResultSetMetaData.java

Abs:  Implements the interface java.sql.ResultSetMetaData

Auth: 19-09-2007, Kevin JACQUEMIN (KJ):
Rev:  05-10-2007, Kevin JACQUEMIN (KJ):

=============================================================================*/


import java.sql.SQLException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.inria.database.QEPng;

public class ResultSetMetaData implements java.sql.ResultSetMetaData {

  private static final String TYPENAME_CHAR = "Character";
  private static final String TYPENAME_DATE = "Date";
  public static final String TYPENAME_NUMBER = "Number"; // public for AutoGeneratedKeyResultSet
  private static final String TYPENAME_BINARY = "Binary";
  private static final String TYPENAME_BLOB = "Blob";
  private static final String TYPENAME_VARCHAR = "Varchar";

  int nb_cols;
  String[] col_names;
  byte[] col_types;
  byte[] meta_to_send;

  private static Hashtable<Integer, ResultSetMetaData> stored_metadata =
    new Hashtable <Integer, ResultSetMetaData>();

  static {
    Util.setEPO(stored_metadata);
  }

  static ResultSetMetaData build(int ep_full) throws SQLException {
    ResultSetMetaData metadata = stored_metadata.get(ep_full);
    if (metadata == null) {
      metadata = new ResultSetMetaData(ep_full);
      stored_metadata.put(ep_full, metadata);
    }
    return metadata;
  }

  private ResultSetMetaData(int ep_full) throws SQLException {
	String ep_metadata = QEPng.getMetaData( ep_full );
    StringTokenizer tokens = new StringTokenizer(ep_metadata, " ", false);
    int nb_col = Integer.parseInt(tokens.nextToken());
    col_names = new String[nb_col];
    col_types = new byte[nb_col];
    meta_to_send = new byte[nb_col + 2];
    meta_to_send[0] = (byte)nb_col;
    for (int i = 0; i <  nb_col; i++) {
      col_types[i] = Byte.parseByte(tokens.nextToken());
      meta_to_send[i+1] = Byte.parseByte(tokens.nextToken());
      col_names[i] = tokens.nextToken();
    }
    this.nb_cols = nb_col;
  }

  public int getColumnCount() throws SQLException {
    return nb_cols;
  }

  private void checkColumnIndex(int column) throws SQLException {
    if (column <= 0 || column > nb_cols) {
      throw new SQLException("Invalid column index");
    }
  }

  public String getColumnLabel(int column) throws SQLException {
    checkColumnIndex(column);
    return getColumnName(column) + ", " + getColumnTypeName(column) + " = ";
  }

  public String getColumnName(int column) throws SQLException {
    checkColumnIndex(column);
    return col_names[column-1];
  }

  public int getColumnType(int column) throws SQLException {
    checkColumnIndex(column);
    switch (col_types[column-1]) {
    case org.inria.jdbc.Macro.T_CHAR :
      return java.sql.Types.CHAR;
    case org.inria.jdbc.Macro.T_DATE	:
      return java.sql.Types.DATE;
    case org.inria.jdbc.Macro.T_NUMBER:
      return java.sql.Types.INTEGER;
    case org.inria.jdbc.Macro.T_BINARY:
      return java.sql.Types.BINARY;
    case org.inria.jdbc.Macro.T_BLOB:
        return java.sql.Types.BLOB;
    case org.inria.jdbc.Macro.T_VARCHAR:
        return java.sql.Types.VARCHAR;
    default	:
      throw new SQLException("Type not supported");
    }
  }

  public String getColumnTypeName(int column) throws SQLException {
    checkColumnIndex(column);
    switch (col_types[column-1]) {
    case org.inria.jdbc.Macro.T_CHAR :
      return TYPENAME_CHAR;
    case org.inria.jdbc.Macro.T_DATE	:
      return TYPENAME_DATE;
    case org.inria.jdbc.Macro.T_NUMBER:
      return TYPENAME_NUMBER;
    case org.inria.jdbc.Macro.T_BINARY:
      return TYPENAME_BINARY;
    case org.inria.jdbc.Macro.T_BLOB:
        return TYPENAME_BLOB;
    case org.inria.jdbc.Macro.T_VARCHAR:
        return TYPENAME_VARCHAR;
    default :
      throw new SQLException("Type not supported");
    }
  }
}