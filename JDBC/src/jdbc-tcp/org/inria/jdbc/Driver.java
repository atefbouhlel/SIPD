package org.inria.jdbc;



public class Driver extends org.inria.jdbc.GenericDriver {
	
  static boolean registered = false ;

  static {
    try {
      if (!registered) {
        Driver d = new Driver();
  		Util.setEPO(d);
        registered = true;
      }
    } catch (java.sql.SQLException sqle) {
    }
  }

	public Driver() throws java.sql.SQLException {
  	super();
  }
  
	@Override
	String getDBMSFQClassName() {
		return "org.inria.jdbc.DBMSTcp";
	}
}
