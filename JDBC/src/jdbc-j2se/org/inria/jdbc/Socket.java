package org.inria.jdbc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

public class Socket
{
	private java.net.Socket socket;
	private DataInputStream recv_data;
	private OutputStream send_data;

	private Socket( String host, int port ) throws IOException
	{
		socket = new java.net.Socket( InetAddress.getByName( host ), port );

		socket.setTcpNoDelay( true );

		// when closing with unsent bytes, then wait 10 seconds before force close...
		socket.setSoLinger( true, 10 );
	
		recv_data = new DataInputStream( socket.getInputStream() );
		send_data = socket.getOutputStream();
	}

	public static Socket create( String host, int port ) throws IOException
	{
		return new Socket( host, port );
	}

	public DataInputStream StreamIn()
	{
		return recv_data;
	}

	public OutputStream StreamOut()
	{
		return send_data;
	}

	protected void finalize() throws Throwable
	{
		send_data.close();
		recv_data.close();

		socket.shutdownOutput();
		socket.shutdownInput();
		socket.close();
	}
}
