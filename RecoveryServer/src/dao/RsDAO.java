package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import beans.EncryptedUser;
import beans.FileDesc;
import beans.MyInfo;
import tools.Constants;

/**
 * 
 * @author Majdi Ben Fredj
 * 
 *  * the TCell DB Manager.

 */
public class RsDAO{

	private static RsDAO instance = null;
	Connection c = null;
	

	int idFile = 0;
	int idUSERSINFO = 0;
	int idUserContacts = 0;
	
		public static RsDAO getInstance() {
			if (instance == null) {
				synchronized (RsDAO.class) {
					if (instance == null) {
						instance = new RsDAO();
					}
				}
			}
			return instance;
		}

	
	public RsDAO() {
		
		try {
			Class.forName("org.sqlite.JDBC");
	        c = DriverManager.getConnection("jdbc:sqlite:recovery_database.db");
	        System.out.println("Opened RS DB successfully ");
		} catch (Exception e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		    System.exit(0);
		}
	}

	public void CreateTables() {
		System.out.println("Create the tables RS");
		Statement statement = null;
		try {
			statement = c.createStatement();
	        String sql = "CREATE TABLE FILE " +
		        		"(ID INT PRIMARY KEY     NOT NULL," +
	                    " UserID         INT     NOT NULL, "+
	                    " GID            TEXT    NOT NULL, "+ 
	                    " SKey           TEXT    NOT NULL, "+ //encrypted
	                    " IV             TEXT    NOT NULL, "+ //encrypted
	                    " COMMAND        TEXT    NOT NULL);"+
	                     "CREATE TABLE USERSINFO " +
	                     "(ID INT PRIMARY KEY     NOT NULL, " +
	                     " UserID         INT     NOT NULL, " + 
	                     " TcellIP        TEXT    NOT NULL, " + 
	                     " Port           INT     NOT NULL, " + 
	                     " PubKey         TEXT    NOT NULL);" + 
	                     "CREATE TABLE USERCONTACTS " +
	                     "(ID INT PRIMARY KEY     NOT NULL, " +
	                     " UserID         INT     NOT NULL, " + 
	                     " CUserID        TEXT    NOT NULL, " + 
	                     " CTcellIP       TEXT    NOT NULL, " + 
	                     " CPort          TEXT    NOT NULL, " + 
	                     " CPubKey        TEXT    NOT NULL);"  ;
	                     
	        
	        statement.executeUpdate(sql);
	        
	        System.out.println("Executing query : " + sql);	
	        
	        statement.close();
	        

		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	public ArrayList<String> getGidByUserID(int userID) {

		ArrayList<String> result = new ArrayList<String>();
		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			
			String query = "SELECT GID FROM FILE WHERE UserID = '"+ userID +"'";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);			

			
			while (rs.next()) {
				String gid = rs.getString("gid");
				result.add(gid);
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	public void DropTables() throws SQLException {
		System.out.println("Drop the tables RS");
		Statement stmt = null;
		
		stmt = c.createStatement();
        String sql = "DROP TABLE FILE; DROP TABLE USERSINFO; DROP TABLE USERCONTACTS";
        stmt.executeUpdate(sql);
        
        System.out.println("Executing query : " + sql);	
        
        stmt.close();
	}

	
	public boolean isUserExists(int userid) {
		boolean result = false;

		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			
			String query = "SELECT ID, USERID FROM USERSINFO WHERE USERID = '" + userid +"'";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);			

			
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	
	public boolean isUserContactsExists(int userid, String cUserid) {
		boolean result = false;

		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			
			String query = "SELECT ID, USERID, CUSERID FROM USERCONTACTS WHERE USERID = '" + userid +"' AND CUSERID = '" + cUserid +"'";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);			

			
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public void insertUserInfo(MyInfo user) {
		Statement stmt = null;
		try {

			int userID = user.getMyGid();
			String TcellIP = user.getMyTcellIp();
			int port = user.getMyTcellPort();
			String pubKey = user.getMyPubKey();
			
			stmt = c.createStatement();
			String sql = "INSERT INTO USERSINFO VALUES ("+idUSERSINFO+", "+userID+", '"+TcellIP+"', "+port+", '"+pubKey+"')";
			stmt.executeUpdate(sql);
			
			System.out.println("Executing query : " + sql);	
			
			stmt.close();
		    idUSERSINFO++;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertUserContacts(int userID, EncryptedUser encryptedUser) {
		Statement stmt = null;
		try {
			 
			String cUserID = encryptedUser.getEncryptedGID(); 
			String cTcellIP = encryptedUser.getEncryptedTCellIP();
			String cPort = encryptedUser.getEncryptedPort();
			String cPubKey = encryptedUser.getEncryptedPKey();
			
			stmt = c.createStatement();
			String sql = "INSERT INTO USERCONTACTS VALUES ("+idUserContacts+", "+userID+", '"+cUserID+"', '"+cTcellIP+"', '"+cPort+"', '"+cPubKey+"')";
			stmt.executeUpdate(sql);
			
			System.out.println("Executing query : " + sql);	
			
			stmt.close();
		    idUserContacts++;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<EncryptedUser> getContactsByUserID(int userID) {

		List<EncryptedUser> result = new ArrayList<>();
		
		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			String query = "SELECT CUserID, CTcellIP, CPort, CPubKey FROM USERCONTACTS WHERE UserID = '"+ userID +"';";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);			

			
			while (rs.next()) {
				String cUserID = rs.getString("CUserID");
				String cTcellIP = rs.getString("CTcellIP");
				String cPort = rs.getString("CPort");
				String cPubKey = rs.getString("CPubKey");
				
				EncryptedUser EncryptedUser = new EncryptedUser(cUserID, cTcellIP, cPort, cPubKey);
				result.add(EncryptedUser);
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public FileDesc getFileDescByGid(String fileGID) {
		
		FileDesc result = null;
		Statement stmt = null;
		
		try {
			stmt = c.createStatement();

			String query = "SELECT SKey, IV FROM FILE WHERE GID ='"+ fileGID +"'";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);
			
			String fileID = Constants.RS_Files_PATH + fileGID;
			
			while (rs.next()) {
				String SKey = rs.getString(1);
				String iv = rs.getString(2);
				result = new FileDesc(fileGID,fileID, SKey, iv,"","");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	
	public MyInfo getUserByID(int userID) {

		MyInfo result = null;
		
		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			
			String query = "SELECT TcellIP, Port, PubKey FROM USERSINFO WHERE UserID = '"+ userID +"';";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);			

			
			while (rs.next()) {
				String tcellIP = rs.getString("TcellIP");
				int port = rs.getInt("Port");
				String pubKey = rs.getString("PubKey");
				result = new MyInfo(userID, tcellIP, port, pubKey, null);
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void deleteByGID(String gid) {
		
		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			
			String query = "DELETE FROM FILE WHERE GID = '"+ gid + "'";
			stmt.executeUpdate(query);
			
			System.out.println("Executing query : " + query);		

			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public boolean isFileExists(String gid) {
		boolean result = false;

		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
			
			String query = "SELECT ID, GID FROM FILE WHERE GID = '" + gid +"'";
			ResultSet rs = stmt.executeQuery(query);
			
			System.out.println("Executing query : " + query);			

			
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public void insertFile(int userID, String gid, String SKey, String iv, String command) {
		Statement stmt = null;
		try {

			stmt = c.createStatement();
			String sql = "INSERT INTO FILE VALUES ("+idFile+", "+userID+", '"+gid+"', '"+SKey+"', '"+iv+"', '"+command+"')";
			stmt.executeUpdate(sql);
			
			System.out.println("Executing query : " + sql);	
			
			stmt.close();
		    idFile++;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}

