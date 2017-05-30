/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import dao.RsDAO;

/**
 * Initialization of the DataBase.
 * 
 * @author Majdi Ben Fredj
 */
public class DatabaseMain {
	/**
	 * Initialization of db.
	 * 
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 */


	public static void main(String[] args) throws NoSuchAlgorithmException,
			FileNotFoundException {

		try {
			RsDAO.getInstance().DropTables();
			RsDAO.getInstance().CreateTables();
			

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
			
	}

}

