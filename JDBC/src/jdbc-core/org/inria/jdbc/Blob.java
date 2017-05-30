package org.inria.jdbc;

/**
 * Implementation of the Blob interface to support BLOB SQL data type in JDBC
 *
 * @author Alexei TROUSSOV
 * @date 2011/06/17
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class Blob implements java.sql.Blob
{
	public static class BlobId
	{
		public static final int BLOB_HASH_SIZE = 16;
		public byte[] id;
		public BlobId() { id = new byte[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }; }
		public int getHead() { return DBMSFactory.bytea2int( id ); }
		public void setHead( int h ) { DBMSFactory.int2bytea( h, id, 0 ); }
		public int getSize() { return DBMSFactory.bytea2int( id, 4 ); }
		public void setSize( int s ) { DBMSFactory.int2bytea( s, id, 4 ); }
		public byte[] getHash()
		{
			byte[] temp = new byte[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };
			for ( int k = 8; k < 8 + BLOB_HASH_SIZE - 1; ++k )
				temp[ k - 8 ] = id[ k ];
			return temp;
		}
		public void setHash( byte[] b ) { for (int i=0; i<BLOB_HASH_SIZE; ++i ) id[i+4+4]=b[i]; }
	}

	private DBMS client;
	private BlobId id;

	/**
	 * Default constructor used to create a new empty blob object.
	 * Generates new blob identifier as well
	 */
	public Blob( DBMS client ) throws SQLException
	{
		if ( client == null )
			throw new SQLException( "Wrong connection object" );
		this.client = client;
		id = new BlobId();
		id.setHead( client.newBlob() );
	}

	/**
	 * Constructor used to create instance of already existing blob object
	 * Specified identifier must not be empty string to be able to attach
	 * to the existing blob object in the database
	 */
	public Blob( DBMS client, BlobId id /** id of existing blob */ ) throws SQLException
	{
		if ( client == null )
			throw new SQLException( "Wrong connection object" );
		this.client = client;
		if ( id == null )
			throw new SQLException( "Wrong BLOB id" );
		this.id = id;
	}

	/**
	 * Accessor to retrieve blob identifier
	 */
	public BlobId getId()
	{
		return id;
	}

	/**
	 * This method frees the Blob object and releases the resources that it holds
	 */
	@Override
	public void free() throws SQLException
	{
		id.setHead( client.newBlob() );	// re-generate new Id
	}

	/**
	 * Returns the number of bytes in the BLOB value designated by this Blob object
	 */
	@Override
	public long length() throws SQLException
	{
		return id.getSize();
	}

	/**
	 * Retrieves all or part of the BLOB value that this Blob object represents, as an array of bytes
	 */
	@Override
	public byte[] getBytes( long pos, int length ) throws SQLException
	{
		return client.getBlob( id, pos-1, (short)length );
	}
	/**
	 * Writes the given array of bytes to the BLOB value that this Blob object represents,
	 * starting at position pos (>1), and returns the number of bytes written
	 */
	@Override
	public int setBytes( long pos, byte[] bytes ) throws SQLException
	{
		int sz = id.getSize();
		if ( pos-1 != sz )
			throw new SQLException( "Cannot overwrite BLOB bytes at offset " + (pos-1) + ", BLOB size is " + sz );
		id.setHash( client.setBlob( id, bytes ) );
		id.setSize( sz + bytes.length );
		return bytes.length;
	}
	/**
	 * Writes all or part of the given byte array to the BLOB value that this Blob object
	 * represents and returns the number of bytes written
	 */
	@Override
	public int setBytes( long pos, byte[] bytes, int offset, int len ) throws SQLException
	{
		int sz = id.getSize();
		if ( pos-1 != sz )
			throw new SQLException( "Cannot overwrite BLOB bytes at offset " + (pos-1) + ", BLOB size is " + sz );
		if ( offset+len > bytes.length )
			throw new SQLException( "Wrong offset/length parameters in BLOB.setBytes()" );
		byte[] dummy = new byte[ len ];
		for ( int k = offset; k < offset + len; ++k )
			dummy[ k - offset ] = bytes[ k ];
		id.setHash( client.setBlob( id, dummy ) );
		id.setSize( sz + len );
		return len;
	}

	/* ======= THE FOLLOWING METHODS ARE LEFT NOT IMPLEMENTED ======== */

	/**
	 * Truncates the BLOB value that this Blob object represents to be length bytes
	 */
	@Override
	public void truncate( long len ) throws SQLException
	{
		throw new SQLException();// java.sql.SQLFeatureNotSupportedException();
	}

	/**
	 * Retrieves the BLOB value designated by this Blob instance as a stream
	 */
	@Override
	public InputStream getBinaryStream() throws SQLException
	{
		throw new SQLException();// java.sql.SQLFeatureNotSupportedException();
	}
	/**
	 * Returns an InputStream object that contains a partial Blob value, starting with
	 * the byte specified by pos, which is length bytes in length
	 */
	@Override
	public InputStream getBinaryStream( long pos, long length ) throws SQLException
	{
		throw new SQLException();// java.sql.SQLFeatureNotSupportedException();
	}
	/**
	 * Retrieves a stream that can be used to write to the BLOB value that this Blob object represents
	 */
	@Override
	public OutputStream setBinaryStream( long pos ) throws SQLException
	{
		throw new SQLException();// java.sql.SQLFeatureNotSupportedException();
	}
	/**
	 * Retrieves the byte position at which the specified byte array pattern begins
	 * within the BLOB value that this Blob object represents
	 */
	@Override
	public long position( byte[] pattern, long start ) throws SQLException
	{
		throw new SQLException();// java.sql.SQLFeatureNotSupportedException();
	}
	/**
	 * Retrieves the byte position in the BLOB value designated by this Blob object at which pattern begins.
	 */
	@Override
	public long position( java.sql.Blob pattern, long start ) throws SQLException
	{
		throw new SQLException();// java.sql.SQLFeatureNotSupportedException();
	}
}
