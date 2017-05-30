package org.inria.dmsp.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.inria.jdbc.DBMSTcp;
import org.inria.jdbc.Socket;
import org.inria.jdbc.Util;

public class TracesReplayer
{
	private BufferedReader	traces;
	private String			traces_line;

	///
	/// FYI: this method is a copy of DeltaLoader.strSplit()
	/// Returns an array of substrings of given string s separated with delim.
	/// If no delimiters found, then the entire input string is placed
	/// in the output array's first element
	/// Note that the input string s has to be not null
	///
	protected String[] strSplit(String s, String delim) throws Exception
	{
		// count number of delim occured...
		java.util.Stack<String> st = new java.util.Stack<String>();
		int ntok = 1,
			pp = 0,
			p = s.indexOf(delim);
		while ( p >= 0 )
		{
			++ntok;
			st.push( s.substring(pp, p) );	// put token to the stack and
			pp = p + delim.length();		// move further...
			p = s.indexOf(delim, pp);
		}
		st.push( s.substring(pp) );
		// prepare resulting array...
		String[] retval = new String[ ntok ];
		for (p=ntok-1; p>=0; --p)
			retval[ p ] = st.pop();
		st = null;
		return retval;
	}

	public void replayTraces( Reader fr, String dbmsHost )throws Exception
	{
		// ** call() function
		// public void call(byte cmd, String static_plan, byte[] plan_params, int plan_params_size, byte[] res);
		
		// ** format of the traces file -- TRACE_CALL:
		// TRACE_CALL;cmd;nb_params;static_plan_modif;plan_params_size;plan_params[0]-plan_params[1]-...;res;
	    // e.g. TRACE_CALL;61;;;0;;; 
		//  cmd:= cast into int -> String
	    //  nb_params:= other unicode (ie, nb of params) in static_plan cast into int -> String
	    //	static_plan_modif:= static_plan modified by 
	    //		replacing \u0000 by _u0000 
	    //		replacing the other unicode (only one) by _uNBPARAM
	    //  plan_params_size:= int -> String
	    //	plan_params[i]:= cast into int -> String
		//	res[i]:= cast into int -> String
		// ** format of the traces file -- TRACE_API:
		// TRACE_API;API_name;args[0];args[1];...;args[n]; 
		// e.g. TRACE_API;nextColumnCopy;0 0 0 0 ;4; 
		
		String[] args; // values in each line
		String[] plan_param_values; // values(casted into int) of the plan_param array
		String[] res_values; // values(casted into int) of the res array
		byte cmd; // the 1st argument of call(): command
		int nb_params; // number of parameters in the plan_params array
		String static_plan; // the 2nd argument of call(): the static part of the EP
		byte[] plan_params; // the 3rd argument of call(): the dynamic part of the EP
		int plan_params_size; // the 4th argument of call(): the real size of the plan_params array
		byte[] res; // the 5th argument of call(): result set?

		// create a database connection and initialize it
		DBMSTcp conn = new DBMSTcp();
		DBMSTcp.initialized = true;

		// load the traces into a buffer
		traces = new BufferedReader(fr);
		// read the first line
		traces_line = traces.readLine();

		// parse the traces line by line
		while(traces_line != null)
		{
			// get the values in this line
			args = strSplit( traces_line, ";" );
			// if it is a trace for call (), replay it
			if (args[0].equals("TRACE_CALL"))
			{
				// get command
				cmd = Byte.parseByte(args[1]);
				// get number of params
				if (args[2].equals(""))
				{
					nb_params = 0;
				}
				else
				{
					nb_params = Integer.parseInt(args[2]);
				}
				// get the static plan
				if (args[3].equals(""))
				{
					static_plan = null;
				}
				else
				{
					static_plan = args[3];
					// rewrite unicode 0 (if any) in static_plan:
					static_plan = static_plan.replace("_u0000", "\u0000");
					// if the query plan if more than 5, rewrite the unicode 
					//		at position 5 (nb_params):
					if ( static_plan.length() > 5 )
					{
						// rewrite nb_param in static_plan
						static_plan=static_plan.replace("_uNBPARAM", ""+(char)nb_params);
					}
				}
				// get the real size of the plan_params
				plan_params_size = Integer.parseInt(args[4]);
				// get plan parameters
				if ( args[5].equals("") )
				{
					plan_params = null;
				}
				else
				{
//					plan_param_values = args[5].split(" ");
					plan_param_values = strSplit( args[5].trim(), " " );
					plan_params = Util.makeTransientByteArray(plan_param_values.length);
					for (int i = 0; i < plan_params.length; i++)
					{
						plan_params[i] = (byte)Integer.parseInt(plan_param_values[i]);
					}
				}
				// get result set if it exists
				if ( !(args.length>6) || (args.length>6 && args[6].equals("")))
				{
					res = null;
				}
				else
				{
//					res_values = args[6].split(" ");
					res_values = strSplit( args[6].trim(), " " );
					res = Util.makeTransientByteArray(res_values.length);
					for (int i = 0; i < res.length; i++)
					{
						res[i] = (byte)Integer.parseInt(res_values[i]);
					}
				}
				// call function call()
				conn.call(cmd, static_plan, plan_params, plan_params_size, res);
			}
			else if (args[0].equals("TRACE_API"))
			{	// traces for DBMS API calls
				// if an API is called, recall it
				if ( args[1].equals("nextColumnCopy") )
				{
					// get parameter res
					if ( args[2].equals("") )
					{
						res = null;
					}
					else
					{
//						res_values = args[2].split(" ");
						res_values = strSplit( args[2].trim(), " " );
						res = Util.makeTransientByteArray(res_values.length);
						for (int i = 0; i < res.length; i++)
						{
							res[i] = (byte)Integer.parseInt(res_values[i]);
						}
					}
					// get parameter len
					int len = Integer.parseInt(args[3]);
					// recall API
					conn.nextColumnCopy(res, len);
				}
				else if (args[1].equals("nextColumnSize"))
				{
					// recall API
					conn.nextColumnSize();
				}
				else if (args[1].equals("tupleProduced"))
				{
					// recall API
					conn.tupleProduced();
				}
				else if (args[1].equals("getGlobalTimestamp"))
				{
					// recall API
					conn.getGlobalTimestamp();
				}
				else if (args[1].equals("getSptIdPatient"))
				{
					// recall API
					conn.getSptIdPatient();
				}
				else if (args[1].equals("open"))
				{
					// part of the code in method open() of class DBMS
					// create a socket
					try
					{
						conn.setSocket( Socket.create(dbmsHost, DBMSTcp.getPort()) );
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				else if (args[1].equals("exit"))
				{
					conn.exit();
				}
			}
			// load a new line
			traces_line = traces.readLine();
		}
	}
}
