package apps;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import tools.Constants;
import api.ClientAPI;
import beans.User;
import configuration.Configuration;
import cryptoTools.KeyManager;
import javax.swing.JPasswordField;
import javax.swing.JTextPane;
import javax.swing.JTextArea;


public class AppMain extends JFrame 
{
	private JPasswordField passwordField;
	public AppMain(User user){
		setTitle("SIPD"); 

		setSize(650,450); 

		setLocationRelativeTo(null); 

		setResizable(false); 

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setContentPane(buildContentPane(user));
	}
	
	

	private JPanel buildContentPane(User user){

		JPanel panel = new JPanel();	
		panel.setBackground(Color.white);
		
		/* Message Area */
		JTextArea txtrDssds = new JTextArea();
		txtrDssds.setEditable(false);
		txtrDssds.setForeground(Color.RED);
		txtrDssds.setBounds(94, 129, 454, 45);
		panel.add(txtrDssds);
		/* End Message Area */
		
		String[] tcells = { "10", "11", "12", "13", "14"};
		JComboBox tcellsCB = new JComboBox(tcells);
		tcellsCB.setBounds(397, 65, 44, 24);
		tcellsCB.setSelectedIndex(0);

		JButton btn = new JButton("Store a file");
		btn.setBounds(132, 5, 112, 25);

		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				JFileChooser choix = new JFileChooser();

				int retour=choix.showOpenDialog(null);

				if(retour==JFileChooser.APPROVE_OPTION){
					ArrayList<String> files = new ArrayList<String>();
					String filePath = choix.getSelectedFile().getAbsolutePath();
					ClientAPI.storeFile(filePath, user);				  
					
					files = ClientAPI.getFileDesc(user);
					String msg = "";
					for (int i = 0 ; i < files.size() ; i++)
						msg += files.get(i);				
					
					txtrDssds.setText(msg);
					//System.out.println("store_btn");

				}else {};

			}

		});
		panel.setLayout(null);

		panel.add(btn);

		/* Start Hidden Password Form */
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(210, 189, 70, 15);
		panel.add(lblPassword);
		lblPassword.setVisible(false);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(337, 187, 104, 19);
		passwordField.setVisible(false);
		panel.add(passwordField);
		
		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(passwordField.getPassword().length == 0)
					txtrDssds.setText("You must enter the password");
				else{	
					String pass = new String(passwordField.getPassword());
					if( pass.equals("sipd")){
						JFileChooser choix = new JFileChooser();
		
						int retour=choix.showOpenDialog(null);
		
						if(retour==JFileChooser.APPROVE_OPTION){
							String pKeyPath = choix.getSelectedFile().getAbsolutePath();
							txtrDssds.setText(pKeyPath);
							ClientAPI.recoverAllData( pKeyPath , user);
							txtrDssds.setForeground(Color.GREEN);
							txtrDssds.setText("All files are well recovered");
		
						}else {};// No file chosen
					}
					else 
						txtrDssds.setText("Wrong Password !");
				}
			}
		});
		btnApply.setBounds(267, 240, 117, 25);
		btnApply.setVisible(false);
		panel.add(btnApply);
		
		/* End Password Form */
		
		JButton btn_restore = new JButton("Restore all");
		btn_restore.setBounds(256, 5, 128, 25);

		btn_restore.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Display the Password Form
				lblPassword.setVisible(true);
				passwordField.setVisible(true);
				btnApply.setVisible(true);				
			}

		});
		
		panel.add(btn_restore);	
		JButton btn_share = new JButton("Share with ..");
		btn_share.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> files = new ArrayList<String>();
			
				int userContactID = Integer.parseInt(String.valueOf(tcellsCB.getSelectedItem()));
				System.out.println(userContactID);
				System.out.println(user.getPort());
				files = ClientAPI.getFileDesc(user);				
				// SHAREFILE
				for (int i = 0 ; i < files.size() ; i++){
					System.out.println(files.get(i));
					try {
						ClientAPI.shareFile(files.get(i), 11 , user);
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
						System.out.println("UnknownHostException Share with .. ");
					} catch (NullPointerException ne) {
						System.out.println("NullPointerException Share with .. ");
					} catch (IOException e1) {
						e1.printStackTrace();
						System.out.println("IOException Share with .. ");
					}
				}
			}
		});
		btn_share.setBounds(224, 65, 152, 25);
		panel.add(btn_share);			
		
		panel.add(tcellsCB);
		
		
		
		JButton btnReadFromDb = new JButton("Read From TCell");
		btnReadFromDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// GETFILEDESC
				ArrayList<String> files = new ArrayList<String>();
				files = ClientAPI.getFileDesc(user);
				
				// READFILE
				for (int i = 0 ; i < files.size() ; i++)
					ClientAPI.readFile(files.get(i), user);
				
				String msg;
				if(files.size() == 0)
					msg = "There are no file in the TCell to read ";
				else
					msg = "The Files are now decrypted !";
				
				txtrDssds.setText(msg);
				
				
			}
		});
		btnReadFromDb.setBounds(396, 5, 152, 25);
		panel.add(btnReadFromDb);		
		
		
		return panel;
	}
	/**
	 * APP Main
	 * 
	 * @author Majdi Ben Fredj
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int userGID = Integer.parseInt(Configuration.getConfiguration().getProperty("myGID"));
		String tCellIP = Configuration.getConfiguration().getProperty("myIP");
		int port = Integer.parseInt(Configuration.getConfiguration().getProperty("myPort"));

		User user= null;

		// load user PubKey
		try {
			String KeyPath = Configuration.getConfiguration().getProperty("keyPath");
			KeyManager keygen = new KeyManager();
			String publicKeyPath = KeyPath + Constants.PUB_KEY_PREFIX + userGID + Constants.KEY_EXT;
			PublicKey pubKey = keygen.LoadPublicKey(publicKeyPath, Constants.RSA_ALG);
			String pubkey = keygen.PublicKeyToString(pubKey);

			user = new User(userGID, tCellIP, port, pubkey);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		AppMain app = new AppMain(user);
		app.setVisible(true);
		
		System.out.println("Dooooone");
	}
}