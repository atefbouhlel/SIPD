/**
 * <P>A Connection represents a session with a specific
 * database. Within the context of a Connection, SQL statements are
 * executed and results are returned.
 *
 * <P>A Connection's database is able to provide information
 * describing its tables, its supported SQL grammar, its stored
 * procedures, the capabilities of this connection, etc. This
 * information is obtained with the getMetaData method.
 *
 * <P><B>Note:</B> By default the Connection automatically commits
 * changes after executing each statement. If auto commit has been
 * disabled, an explicit commit must be done or database changes will
 * not be saved.
 *
 * @see DriverManager#getConnection
 * @see Statement
 * @see ResultSet
 */

/*=============================================================================

 Name: Connection.java

 Abs:  Implements the interface java.sql.Connection

 Auth: 18-09-2007, Kevin JACQUEMIN (KJ):
 Rev:  20-09-2007, Kevin JACQUEMIN (KJ):

 =============================================================================*/

package org.inria.jdbc;

import java.security.AccessControlException;
import java.sql.Blob;
import java.sql.SQLException;

public class Connection implements java.sql.Connection
{
	// should be private but is made public for use on PC
	// to enable opening consecutive connections
	public /*private*/ DBMS client = null;

	/* return the bits vector of one byte long which describes an authorization
	 * (read/update/delete/insert)
	 *
	 * right can be '0' = deny '1' = grant '?' = no deny, no grant, depends on the
	 * others rights from others categories
	 */
	public static int ComputeAuthorizationBitsVector(
		char readright,
		char updateright,
		char deleteright,
		char insertright
		) throws AccessControlException
	{
		return (((parseRight(readright  ) & 0x00000003) << 6) |
				((parseRight(updateright) & 0x00000003) << 4) |
				((parseRight(deleteright) & 0x00000003) << 2) |
				 (parseRight(insertright) & 0x00000003) );
	}

	private static int parseRight( char c ) throws AccessControlException
	{
		switch ( c )
		{
		case '1':	return 1;
		case '0':	return 0;
		case '?':	return 2;
		}
		throw new AccessControlException(
				"Right " + c + " not in {0,1,?}, cannot be parsed !!" );
	}

	/**
	 *
	 * @param url
	 * @throws SQLException
	 */
	void init(String url) throws SQLException
	{
		try
		{	// records the DBMS client
			this.client = DBMSFactory.getDBMS();
			// realizes the connection to the DBMS without user/role
			int pos_ac = url.indexOf( "?", 0 );
			if ( pos_ac == -1 )
			{
				this.client.open( url, null, null );
				return;
			}
			// computes user/role
			String host = url.substring( 0, pos_ac );
			int pos_sep_user_role = url.indexOf( '&', 0 );
			if ( pos_sep_user_role == -1 )
			{
				throw new SQLException( "Invalid connection URL: " + url );
			}
			int pos_user_val = url.indexOf( '=', 0 );
			int pos_role_val = url.indexOf( '=', pos_sep_user_role );
			if ( pos_role_val == -1 ||
				 pos_user_val == -1 )
			{
				throw new SQLException( "Invalid connection URL: " + url );
			}
			// realizes the connection to the DBMS
			this.client.open( host,
                       url.substring( pos_user_val + 1, pos_sep_user_role ),
                       url.substring( pos_role_val + 1 ) );
		}
		catch ( SQLException sqle )
		{	// exit in case of error
			client.exit();
			throw sqle;
		}
		catch ( Throwable t )
		{
			client.exit();
			throw new SQLException( t.toString() );
		}
	}

	public void create( byte[] schema, int userId ) throws SQLException
	{
		// To Reset Schema and empty Database
		try
		{
			client.installSchema( schema, userId );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void distroy()
	{
		// To Reset Schema and empty Database
		try
		{
			client.uninstallSchema();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Tells the JDBC that a schema is already loaded into the token.
	 */
	public void bypassInitialization()
	{
		client.bypassInitialization();
	}

	public void empty()
	{
		// To Empty the Database
		try
		{
			client.eraseData();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void insert_failure( byte function_id, byte struct_id, byte struct_type )
	{
		// To enable the failure point
		try
		{
			client.insertFailurePoint( function_id, struct_id, struct_type );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void remove_failure()
	{
		// To disable the failure point
		try
		{
			client.removeFailurePoint();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void insert_tearingpoint( byte function_id )
	{
		// To enable the tearing point
		try
		{
			client.insertTearingPoint(function_id);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void remove_tearingpoint()
	{
		// To disable the tearing point
		try
		{
			client.removeTearingPoint();
		} catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void PBFilter_init( byte with_partition, byte n_hashes )
	{
		// To disable the tearing point
		try
		{
			client.PBFilterInit( with_partition, n_hashes );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void PBFilter_insert_key( String key )
	{
		// To enable the failure point
		try
		{
			client.PBFilterInsertKey( key );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void PBFilter_delete_key( String key )
	{
		// To enable the failure point
		try
		{
			client.PBFilterDeleteKey( key );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void PBFilter_lookup_key( String key )
	{
		// To enable the failure point
		try
		{
			client.PBFilterLookupKey( key );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void PBFilter_close( byte reset_db )
	{
		// To enable the failure point
		try
		{
			client.PBFilterClose( reset_db );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Reset the counter measuring the time spent in the DBMS engine.
	 * @return the counter value, before reset;
	 */
	public long resetDBMSCallTime()
	{
		return client.resetDBMSCallTime();
	}

	/**
	 * Retrieves the amount of time spent in the DBMS engine
	 * @return a number of milliseconds
	 */
	public long getDBMSCallTime()
	{
		return client.getDBMSCallTime( null );
	}

	/**
	 * Retrieves the amount of time spent in the DBMS engine
	 * @return a number of milliseconds
	 */
	public long getDBMSCallTime( String tag )
	{
		return client.getDBMSCallTime( tag );
	}

	public boolean isClosed() throws SQLException
	{
		return client == null;
	}

	@Override
	public void close() throws SQLException
	{
		try
		{
			client.exit();
			client = null;
		}
		catch ( Exception e )
		{
			throw new SQLException( "close connection failed: " + e.getMessage() );
		}
	}

	/*
	 * Retrieving debug information from DBMS
	 */
	public byte[] getDebugInfo( byte param, byte[] result ) throws SQLException
	{
		return client.getDebugInfo( param, result );
	}

	public int getSptIdPatient() throws SQLException
	{
		return client.getSptIdPatient();
	}

	/**
	 * Returns the global timestamp
	 */
	public int getGlobalTimestamp() throws SQLException
	{
		return client.getGlobalTimestamp();
	}

	/**
	 * Increments the global timestamp and returns the value before incrementation
	 */
	public int nextGlobalTimestamp() throws SQLException
	{
		return client.nextGlobalTimestamp();
	}

	public void setGlobalTimestamp( int ts ) throws SQLException
	{
		client.setGlobalTimestamp( ts );
	}

	@Override
	public java.sql.Statement createStatement()
	{
		return new Statement( client );
	}

	@Override
	public java.sql.PreparedStatement prepareStatement( String sql ) throws SQLException
	{
		throw new SQLException( "SQL Feature Not Supported" );
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(
		String sql,
		int autoGeneratedKeys
		) throws SQLException
	{
		throw new SQLException( "SQL Feature Not Supported" );
	}
	
	public java.sql.PreparedStatement prepareStatement( int sql ) throws SQLException
	{
		return new PreparedStatement( client, sql, Statement.NO_GENERATED_KEYS );
	}

	public java.sql.PreparedStatement prepareStatement(
		int sql,
		int autoGeneratedKeys
		) throws SQLException
	{
		return new PreparedStatement( client, sql, autoGeneratedKeys );
	}

	@Override
	public void commit() throws SQLException
	{
		if ( client != null )
		{
			try
			{
				client.call( Macro.CMD_COMMIT, null, null, 0, null );
			}
			catch ( Exception e )
			{
				throw new SQLException( "Commit failed: " + e );
			}
		}
	}

	@Override
	public void rollback() throws SQLException
	{
		// To rollback the pending transaction
		try
		{
			client.rollbackTransaction();
		}
		catch ( Exception e )
		{
			throw new SQLException( "Rollback failed: " + e );
		}
	}
	
	/* Ajouté par Majdi*/
	@Override
	public Blob createBlob() throws SQLException
	{
		return new org.inria.jdbc.Blob( client );
	}

	
	
//	/**
//	 * Send CMD_BLOB_NEW
//	 * Used to create new blob object or to abandon existing one
//	 * @return New blob head part of id
//	 */
//	public int newBlob() throws SQLException
//	{
//		return client.newBlob();
//	}
//	/**
//	 * Send CMD_BLOB_SET
//	 * Used to append data to the specified blob's head and size from its id
//	 * @param id Blob id
//	 * @param bytes Data to append
//	 * @return New blob hash
//	 */
//	public byte[] setBlob( Blob.BlobId id, byte[] bytes ) throws SQLException
//	{
//		return client.setBlob( id, bytes );
//	}
//	/**
//	 * Send CMD_BLOB_GET
//	 * Used to retrieve length bytes of data of given blob's head from its id at given offset
//	 * @param id Blob id
//	 * @param offset Offset from blob's head to retrieve data from
//	 * @param length Number of bytes to retrieve
//	 * @return Received data
//	 */
//	public byte[] getBlob( Blob.BlobId id, long offset, short length ) throws SQLException
//	{
//		return client.getBlob( id, offset, length );
//	}
}
