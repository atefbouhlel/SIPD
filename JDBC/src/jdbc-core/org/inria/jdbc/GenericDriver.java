/**
 * <P>The Java SQL framework allows for multiple database drivers.
 *
 * <P>Each driver should supply a class that implements
 * the Driver interface.
 *
 * <P>The DriverManager will try to load as many drivers as it can
 * find and then for any given connection request, it will ask each
 * driver in turn to try to connect to the target URL.
 *
 * <P>It is strongly recommended that each Driver class should be
 * small and standalone so that the Driver class can be loaded and
 * queried without bringing in vast quantities of supporting code.
 *
 * <P>When a Driver class is loaded, it should create an instance of
 * itself and register it with the DriverManager. This means that a
 * user can load and register a driver by doing
 * Class.forName("foo.bah.Driver").
 *
 * @see DriverManager
 * @see Connection
 */

/*=============================================================================

Name: Driver.java

Abs:  Implements the interface java.sql.Driver

Auth: 18-09-2007, Kevin JACQUEMIN (KJ):
Rev:  18-09-2007, Kevin JACQUEMIN (KJ):

=============================================================================*/


package org.inria.jdbc;

import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public abstract class GenericDriver implements java.sql.Driver {
  //
  // Version Info
  //
  static final int _MAJORVERSION = 0;
  static final int _MINORVERSION = 1;

  abstract String getDBMSFQClassName();

  /**
   * Construct a new driver and register it with DriverManager
   *
   * @exception java.sql.SQLException
   */
  GenericDriver() throws java.sql.SQLException {
    java.sql.DriverManager.registerDriver(this);
    org.inria.jdbc.DBMSFactory.registerDBMS(getDBMSFQClassName());
  }


  public synchronized java.sql.Connection connect(String url, Properties info)
  throws java.sql.SQLException {
    try {
      Connection newConn = new Connection();
      Util.transferToClientContext(newConn);
      newConn.init(url);
      return newConn;
    } catch (Exception e) {
      throw new SQLException(
        "Cannot load connection class because of underlying exception: '"
        + e.toString() + "'");
    }
  }

  public synchronized boolean acceptsURL(String Url)
  throws java.sql.SQLException {
    return true;
  }

  /**
   * Gets the drivers major version number
   *
   * @return the drivers major version number
   */
  public int getMajorVersion() {
    return _MAJORVERSION;
  }

  /**
   * Get the drivers minor version number
   *
   * @return the drivers minor version number
   */
  public int getMinorVersion() {
    return _MINORVERSION;
  }

  /**
   * Report whether the driver is a genuine JDBC compliant driver.  A
   * driver may only report "true" here if it passes the JDBC compliance
   * tests, otherwise it is required to return false.  JDBC compliance
   * requires full support for the JDBC API and full support for SQL 92
   * Entry Level.
   *
   */
  public boolean jdbcCompliant() {
    return false;
  }

	// FYI: we implement abstract method here to make compiler happy
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
	{
		return null;
	}
}
