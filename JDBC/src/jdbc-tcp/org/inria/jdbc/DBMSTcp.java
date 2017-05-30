package org.inria.jdbc;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author jvandewa
 */
public class DBMSTcp extends DBMS
{
	private Socket sock = null;

	private int[] counter = Util.makeTransientIntArray(1);
	private static final int PORT = 33000; // default server port

	public void setSocket( Socket socket ) throws SQLException
	{
		sock = socket;
	}

	public static int getPort()
	{
		int port;
		try
		{
			port = Integer.parseInt( System.getProperty("jdbc.port") );
		}
		catch ( Exception ex ) // i.e. NumberFormatException
		{
			port = PORT;
		}
		return port;
	}

	@Override
	public void open(String host, String user, String role) throws SQLException
	{
		try
		{
			Socket socket = Socket.create( host, getPort() );
			sock = socket;
			super.open( host, user, role );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void exit() throws SQLException // make it public for DEBUG reason
	{
		try
		{
			sock.StreamOut().close();
			sock.StreamIn().close();
			sock = null;
			super.exit();
		}
		catch (IOException ioe)
		{
			throw new SQLException("exit dbms failed: " + ioe.getMessage());
		}
	}

	///
	/// Called in DBMS								.call()
	///
	@Override
	public int call0(byte cmd,
					 String static_plan,
					 byte[] plan_params, int plan_params_size,
					 byte[] res) throws SQLException
	{
		long time_entry = System.currentTimeMillis();
		int status = 0;
		try
		{
			if ( static_plan == null )
			{
				if ( plan_params == null )
				{
					status = sendData(new byte[] { Macro.STATE_DBMS, cmd, 0 });
				}
				else
				{
					int len = plan_params_size;
					byte[] buffer = new byte[ len + 6 ];
					buffer[ 0 ] = Macro.STATE_DBMS;
					buffer[ 1 ] = cmd;
					DBMSFactory.int2bytea( plan_params_size, buffer, 2 );
					System.arraycopy( plan_params, 0, buffer, 6, plan_params_size );
					status = sendData( buffer );
				}
			}
			else
			{	// remove the header "/*EP " and put 0 in the end of the static plan
				int static_len = static_plan.length() + 1;
				int len = static_len + plan_params_size;
				byte[] buffer = new byte[ len + 6 ];
				buffer[ 0 ] = Macro.STATE_DBMS;
				buffer[ 1 ] = cmd;
				DBMSFactory.int2bytea( static_len, buffer, 2 );
				System.arraycopy( static_plan.getBytes(), 0, buffer, 6, static_len-1 );
				// put 0 in the end of the static plan
				buffer[ static_len + 5 ] = 0;
				if ( plan_params_size > 0 )
					System.arraycopy( plan_params, 0, buffer, static_len + 6, plan_params_size );
				status = sendData( buffer );
			}
			// receive resulting data if status code is OK and there is something to receive...
			if ( status == 0 && res != null )
			{
				receiveData( res );
			}
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
		return status;
	}

	// ================== BLOBs =====================

	/**
	 * Send CMD_BLOB_NEW
	 * Used to create new blob object or to abandon existing one
	 * @return New blob head part of id
	 */
	@Override
	public int newBlob() throws SQLException
	{
		long time_entry = System.currentTimeMillis();
		int status = 0;
		try
		{
			byte[] buffer = new byte[ 1 + 1 ];
			buffer[ 0 ] = Macro.STATE_DBMS;
			buffer[ 1 ] = Macro.CMD_BLOB_NEW;
			status = sendData( buffer );

			// receive resulting data if status code is OK...
			if ( status != 0 )
				throw new SQLException( "Expected 0, received " + status + "." );

			byte[] res = new byte[ 4 ];
			receiveData( res );

			return DBMSFactory.bytea2int( res );
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
	}

	/**
	 * Send CMD_BLOB_SET
	 * Used to append data to the specified blob's head and size from its id
	 * @param id Blob id
	 * @param bytes Data to append
	 * @return New blob hash
	 */
	@Override
	public byte[] setBlob( Blob.BlobId id, byte[] bytes ) throws SQLException
	{
		long time_entry = System.currentTimeMillis();
		int status = 0;
		try
		{
			int p = 0;
			byte[] buffer = new byte[ 1 + 1 + 4 + 4 + 16 + 2 + bytes.length ];
			buffer[ 0 ] = Macro.STATE_DBMS; ++p;
			buffer[ 1 ] = Macro.CMD_BLOB_SET; ++p;
			System.arraycopy( id.id, 0, buffer, p, 4+4+16 ); p += 4+4+16;// is better than:
			//DBMSFactory.int2bytea( id.getHead(), buffer, p ); p += 4;
			//DBMSFactory.int2bytea( id.getSize(), buffer, p ); p += 4;
			//System.arraycopy( id.getHash(), 0, buffer, p, 16 ); p += 16;
			DBMSFactory.short2bytea( (short)bytes.length, buffer, p ); p += 2;
			System.arraycopy( bytes, 0, buffer, p, bytes.length );
			status = sendData( buffer );

			// receive resulting data if status code is OK...
			if ( status != 0 )
				throw new SQLException( "Expected 0, received " + status + "." );

			// receive new id
			byte [] hash = new byte[ Blob.BlobId.BLOB_HASH_SIZE ];
			receiveData( hash );

			return hash;
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
	}

	/**
	 * Send CMD_BLOB_GET
	 * Used to retrieve length bytes of data of given blob's head from its id at given offset
	 * @param id Blob id
	 * @param offset Offset from blob's head to retrieve data from
	 * @param length Number of bytes to retrieve
	 * @return Received data
	 */
	@Override
	public byte[] getBlob( Blob.BlobId id, long offset, short length ) throws SQLException
	{
		long time_entry = System.currentTimeMillis();
		int status = 0;
		try
		{
			int p = 0;
			byte[] buffer = new byte[ 1 + 1 + 4 + 4 + 2 ];
			buffer[ 0 ] = Macro.STATE_DBMS; ++p;
			buffer[ 1 ] = Macro.CMD_BLOB_GET; ++p;
			DBMSFactory.int2bytea( id.getHead(), buffer, p ); p += 4;
			DBMSFactory.int2bytea( (int)offset, buffer, p ); p += 4;
			DBMSFactory.short2bytea( length, buffer, p ); p += 2;
			status = sendData( buffer );

			// receive resulting data if status code is OK...
			if ( status != 0 )
				throw new SQLException( "Expected 0, received " + status + "." );

			byte[] res = new byte[ 2 ];
			receiveData( res );

			short len = DBMSFactory.bytea2short( res );
			byte[] retval = new byte[ len ];
			receiveData( retval );

			return retval;
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
	}

	// ================== Tuple =====================

	///
	/// Called in
	///		org.inria.jdbc.ResultSet						.next()
	///		org.inria.dmsp.debug.TracesReplayer		.replayTraces()
	///		org.inria.jdbc.Statement						.executeUpdate()
	///
	@Override
	public int tupleProduced() throws SQLException
	{
		if (DEBUG)
		{	// print trace header and function name; end of line
			System.out.print("\nTRACE_API;tupleProduced; \n");
		}

		int ret = 0;
		long time_entry = System.currentTimeMillis();
		try
		{
			int status = sendData( new byte[] {
					Macro.STATE_TUPLE,
					Macro.CMD_TUPLE_PRODUCED,
					0				});
			if ( status != 0 )
			{
				throw new SQLException( "Expected 0, received " + status + "." );
			}

			byte[] res = new byte[ 4 ];
			receiveData( res );
			ret = DBMSFactory.bytea2int( res );
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
		return ret;
	}

	// ============================= Column =================

	///
	/// Called in
	///		org.inria.jdbc.ResultSet						.next()
	///		org.inria.dmsp.debug.TracesReplayer		.replayTraces()
	///
	@Override
	public int nextColumnSize() throws SQLException
	{
		if (DEBUG)
		{	// print trace header and function name; end of line
			System.out.print("\nTRACE_API;nextColumnSize; \n");
		}

		int ret = 0;
		long time_entry = System.currentTimeMillis();
		try
		{
			int status = sendData( new byte[] {
					Macro.STATE_TUPLE,
					Macro.CMD_NEXT_COLUMN_SIZE,
					0				});
			if ( status != 0 )
			{
				throw new SQLException( "Expected 0, received " + status + "." );
			}

			byte[] res = new byte[ 4 ];
			receiveData( res );
			ret = DBMSFactory.bytea2int( res );
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
		return ret;
	}

	///
	/// Called in
	///		org.inria.jdbc.ResultSet						.next()
	///		org.inria.dmsp.debug.TracesReplayer		.replayTraces()
	///
	@Override
	public void nextColumnCopy(byte[] res, int len) throws SQLException
	{
		if (DEBUG)
		{	// print trace header and function name
			System.out.print("\nTRACE_API;nextColumnCopy;");
			if ( res != null )			// print res
			{
				for ( int i=0; i<res.length; ++i )
				{
					System.out.print( (int)res[i] + " ");
				}
			}
			System.out.print( ";" + len + "; \n" );	// close res + print len + close len and end of line
		}

		long time_entry = System.currentTimeMillis();
		try
		{
			byte[] buffer = new byte[ 6 ];
			buffer[ 0 ] = Macro.STATE_TUPLE;
			buffer[ 1 ] = Macro.CMD_NEXT_COLUMN_COPY;
			DBMSFactory.int2bytea( len, buffer, 2 );
			int status = sendData( buffer );
			if ( status != 0 )
			{
				throw new SQLException( "Expected 0, received " + status + "." );
			}

			// TODO: move it to Recordset? handles the "optimized" case [len == 1 or 2] (?!)
			if ( res == null )
				res = new byte[ len ];
			receiveData( res );
		}
		finally
		{
			this.counter[ 0 ] += (int)( System.currentTimeMillis() - time_entry );
		}
	}

	// ==================== Patient ===================

	///
	/// Called in
	///		org.inria.dmsp.debug.TracesReplayer		.replayTraces()
	///		org.inria.jdbc.Connection					.getSptIdPatient()
	///
	@Override
	public int getSptIdPatient() throws SQLException
	{
		if (DEBUG)
		{	// print trace header and function name; end of line
			System.out.print("\nTRACE_API;getSptIdPatient; \n");
		}

		int status = sendData( new byte[] {
				Macro.STATE_TUPLE,
				Macro.CMD_IDPATIENT,
				0				});
		if ( status != 0 )
		{
			throw new SQLException( "Expected 0, received " + status + "." );
		}

		byte [] res = new byte[ 4 ];
		receiveData( res );
		return DBMSFactory.bytea2int( res );
	}

	// =================== Timestamp =================

	///
	/// Called in
	///		org.inria.jdbc.Connection					.nextGlobalTimestamp()
	///
	@Override
	int nextGlobalTimestamp() throws SQLException
	{
		if (DEBUG)
		{	// print trace header and function name; end of line
			System.out.print("\nTRACE_API;incGlobalTimestamp; \n");
		}

		int status = sendData( new byte[] {
				Macro.STATE_TUPLE,
				Macro.CMD_TSGLOBAL,
				0				});
		if ( status != 0 )
		{
			throw new SQLException( "Expected 0, received " + status + "." );
		}

		byte [] res = new byte[ 4 ];
		receiveData( res );
		return DBMSFactory.bytea2int( res );
	}

	///
	/// Called in
	///		org.inria.dmsp.debug.TracesReplayer		.replayTraces()
	///		org.inria.jdbc.Connection					.getGlobalTimestamp()
	///
	@Override
	public int getGlobalTimestamp() throws SQLException
	{
		if (DEBUG)
		{	// print trace header and function name; end of line
			System.out.print("\nTRACE_API;getGlobalTimestamp; \n");
		}

		int status = sendData( new byte[] {
				Macro.STATE_TUPLE,
				Macro.CMD_TSGLOBAL_NO_INC,
				0				});
		if ( status != 0 )
		{
			throw new SQLException( "Expected 0, received " + status + "." );
		}

		byte[] res = new byte[ 4 ];
		receiveData( res );
		return DBMSFactory.bytea2int( res );
	}

	///
	/// Called in
	///		org.inria.jdbc.Connection					.setGlobalTimestamp()
	///
	@Override
	void setGlobalTimestamp(int tsspt) throws SQLException
	{
		byte[] buffer = new byte[ 1+1+4+4 ];
		buffer[ 0 ] = Macro.STATE_TUPLE;
		buffer[ 1 ] = Macro.CMD_SETTSGLOBAL;
		DBMSFactory.int2bytea( 4, buffer, 2 );
		DBMSFactory.int2bytea( tsspt, buffer, 6 );
		int status = sendData( buffer );
		if ( status != 0 )
		{
			throw new SQLException( "Expected 0, received " + status + "." );
		}
	}

	// =================== DBMS call time ================

	///
	/// Called in
	///		org.inria.jdbc.Connection					.getDBMSCallTime()
	///		org.inria.jdbc.Connection					.getDBMSCallTime(String)
	///
	@Override
	long getDBMSCallTime(String tag)
	{
		return counter[ 0 ];
	}

	///
	/// Called in
	///		org.inria.jdbc.Connection					.resetDBMSCallTime()
	///
	@Override
	long resetDBMSCallTime()
	{
		long val = counter[ 0 ];
		counter[ 0 ] = 0;
		return val;
	}

	///
	/// Retrieving debug information from DBMS
	/// Called in
	///		org.inria.jdbc.Connection					.getDebugInfo()
	///
	@Override
	byte[] getDebugInfo( byte param, byte[] result ) throws SQLException
	{
		byte[] retval = result;
		int status = sendData( new byte[] {
				Macro.STATE_TUPLE,
				Macro.CMD_DEBUG,
				param				});
		if ( status != 0 )
		{
			throw new SQLException( "Expected 0, received " + status + "." );
		}

		byte [] len = new byte[ 1 ];
		receiveData( len );

		// in java byte value can be negative :(
		int llen = DBMSFactory.byte2int( len[0] );
		if ( retval == null || retval.length < llen )
			retval = new byte[ llen ];
		receiveData( retval );
		return retval;
	}

	protected int sendData(byte[] buffer) throws SQLException
	{	// send a buffer(byte array) with a packet by UDP
		int retval = 0;
		try
		{
			// send data to server...
			byte[] data = new byte[ Macro.JIBSON_HEADER_SIZE + buffer.length ];
			System.arraycopy( Macro.composeJibsonHeader( buffer.length ), 0, data, 0, Macro.JIBSON_HEADER_SIZE );
			System.arraycopy( buffer, 0, data, Macro.JIBSON_HEADER_SIZE, buffer.length );
			sock.StreamOut().write( data );
			sock.StreamOut().flush();

			// receive ending frame for our call...
			byte[] frame = new byte[ Macro.JIBSON_HEADER_SIZE + Macro.ERROR_CODE_SIZE ];
			sock.StreamIn().read( frame, 0, frame.length );
			if ( Macro.checkJibsonHeader( frame ) )
			{
				byte[] res = new byte[ Macro.ERROR_CODE_SIZE ];
				System.arraycopy( frame, Macro.JIBSON_HEADER_SIZE, res, 0, Macro.ERROR_CODE_SIZE );
				retval = DBMSFactory.bytea2int( res );
			}
			else
				retval = -1;
		}
		catch (IOException E)
		{
			System.out.println(
					"Cannot send data to the host because of underlying exception: '" +
					E.toString() + "'");
			retval = -1;
		}
		return retval;
	}

	protected void receiveData(byte[] buffer) throws SQLException
	{
		try
		{
			// receive data and put it into given buffer...
			sock.StreamIn().readFully( buffer );
		}
		catch (IOException E)
		{
			System.out.println(
					"Cannot receive data from the host because of underlying exception: '" +
					E.toString() + "'");
		}
	}
}
