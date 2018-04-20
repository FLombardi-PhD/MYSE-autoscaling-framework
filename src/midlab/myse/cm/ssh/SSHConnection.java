package midlab.myse.cm.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import midlab.myse.utils.PropFileManager;


/**
 * Allow a set of operation through ssh connections towards a set of servers.
 * It is designed to work with VMWare ESX v5.x
 *
 * @author Federico Lombardi - Sapienza University of Rome
 */
public class SSHConnection{
	
	/**
	 * An array of servers
	 */
	private static String[] hosts;
	
	/**
	 * The ssh user
	 */
	private static String user;
	
	/**
	 * The password of the ssh user
	 */
	private static String password;
	
	/**
	 * The port of ssh servers
	 */
	private static int port;
	
	/**
	 * The prefix of the VM name on hypervisor
	 */
	private static String prefixVmName;
	
	/**
	 * Create a connection toward a server host
	 * @param host the server toward which create a connecton
	 * @return a Session
	 * @throws IOException 
	 */
	private Session connect(String host) throws IOException{
		
		PropFileManager propManConfig = new PropFileManager("config.properties");
		
		hosts = propManConfig.getProperty("hosts").split(",");
		user = propManConfig.getProperty("user");
		password = propManConfig.getProperty("password");
		port = Integer.parseInt(propManConfig.getProperty("port"));
		prefixVmName = propManConfig.getProperty(prefixVmName);
		
		try{
			// open ssh session
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Establishing SSH Connection towards " + host + "...");
            session.connect();
            System.out.println("Connection SSH established.");
            System.out.println();
            return session;
            
		} catch(Exception e){
			System.err.print(e);
		}
		return null;
	}
	
	public boolean action(String host, String vmid){
		try{
			Session session = connect(host);
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			boolean status = getVmStatus(session, vmid);
			System.out.println("Status "+vmid+": "+status);
			if(status==true){
				// if the vm is turned on, then it shutdown 
				channel.setCommand("vim-cmd vmsvc/power.shutdown "+vmid);
			}
			else{
				// if the vm is turned off, then turn it on
				channel.setCommand("vim-cmd vmsvc/power.on "+vmid);
			}
			channel.connect();
			
			// wait for apply of new configuration
			int cont=0; // iteration counter
			while(getVmStatus(session, vmid)==status){
				if(cont==0) System.out.println("Wait...");
				Thread.sleep(4000);
				++cont;
			}
			
			System.out.println("Done. New configuration is available.");
			
			// close channel and session
			channel.disconnect();
			session.disconnect();
			return true;
		}
		catch(Exception e){System.err.print(e);}
		return false;
	}
	
	/**
	 * Get the VMs
	 * @return a matrix containing the status of VMs deployed on each server
	 */
	@SuppressWarnings("rawtypes")
	public String[][] get(){
			
		try{
			
			/*
			 * 1) for each server, open ssh sessions 
			 * 2) insert the result with the vm to filter in a LinkedList
			 * 3) filter the results through the pareser and insert the results in maps
			 */
			
			Session[] sessions = new Session[hosts.length];
			LinkedList[] toParsing = new LinkedList[hosts.length];
			Map[] maps = new Map[hosts.length];
			int contVm = 0;
			for(int i=0; i<sessions.length; ++i){
				sessions[i] = connect(hosts[i]);
				toParsing[i] = getAllVms(sessions[i]);
				maps[i] = parser(toParsing[i]);
				contVm+=maps[i].size();
			}
			
			// create a matrix containing blade-vmid-nameVM-statusVM
			String[][] blade_id_name_status = new String[contVm][4];
			int cont = 0;
			System.out.println("Available VMs:");
	        for(int j=0; j<hosts.length; j++){
	        	for(int i=0; i<maps[j].size(); ++i){
		          	String[] a = (String[]) maps[j].get(i);
		          	/* mapping: a[0]=vm-id, a[1]=vm-name, a[2]=vm-status */
		          	blade_id_name_status[cont][0] = hosts[j];
		          	blade_id_name_status[cont][1] = a[0];
		          	blade_id_name_status[cont][2] = a[1];
		          	if(getVmStatus(sessions[j],a[0])==true) blade_id_name_status[cont][3] = "ON";
		          	else blade_id_name_status[cont][3] = "OFF";
		          	System.out.println("SERVER: " + blade_id_name_status[cont][0] + " -- ID: " + blade_id_name_status[cont][1] + " -- NAME: " + blade_id_name_status[cont][2]+" -- STATUS: " + blade_id_name_status[cont][3]);
		          	++cont;
		        }
	        }
			System.out.println();
			
	        // disconnet the sessions
	        for(int i=0; i<hosts.length; ++i){
	        	sessions[i].disconnect();
	        }
            
	        // return the matrix
            return blade_id_name_status;
		}
		catch(Exception e){System.err.print(e);}
		
		return null;
	}
	
	
	/**
	 * Get the list of all VMs
	 * @param session
	 * @return a LinkedList with the available VMs
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static LinkedList getAllVms(Session session) throws IOException, InterruptedException{
		// opening shell channel
        System.out.println("Opening channel...");
        ChannelExec channel;
		try {
			String s;
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("vim-cmd vmsvc/getallvms");
			
			//redirect of output
			channel.setOutputStream(System.out);
			channel.connect();
			System.out.println("Channel opened.");
			System.out.println();
			
			InputStream in = channel.getInputStream();
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader rrr = new BufferedReader(isr);
			
			LinkedList<String> toParse = new LinkedList();
			rrr.readLine(); // skip header
			while((s=rrr.readLine())!=null){
				toParse.add(s);
				System.out.println(s);
			}
			System.out.println();
			
			channel.disconnect();
			return toParse;
		} catch (JSchException e) { e.printStackTrace(); }
		return null;
	}
		
	
	/**
	 * Parse the result obtained from the Hypervisor
	 * @param l is the list of VMs
	 * @return a map containing <id_VM, name_VM>
	 */
	@SuppressWarnings("rawtypes")
	private static Map parser(LinkedList l){
				
		Map<Integer, String[]> map = new HashMap<Integer, String[]>();
		String tmp;
				
		String vmid;
		String vmname;
		
		int cont;
		int cont2=0; // to insert in the map
		
		for(int i=0; i<l.size(); ++i){
			// read the string
			tmp = (String) l.get(i);
			
			// set to 0 the counter of char of the string
			cont=0;
			
			// read the vmid
			vmid="";
			while(tmp.charAt(cont)>='0' && tmp.charAt(cont)<='9'){
				vmid+=tmp.charAt(cont);
				++cont;
			}
			
			// remove spaces between vmid and vmname
			while(tmp.charAt(cont)==' '){
				++cont;
			}
			
			// read the vmname
			vmname="";
			while(tmp.charAt(cont)!=' '){
				vmname+=tmp.charAt(cont);
				++cont;
			}
			
			// write the couple vmid-vmname in the array
			// vmid_vmname[i][0] = vmid;
			// vmid_vmname[i][1] = vmname;
			if(vmname.startsWith(prefixVmName)){
				String[] a = {vmid, vmname};
				map.put(cont2, a);
				++cont2;
			}
			// iterate to read a new string....
		}
		
		// return vmid_vmname;
		return map;
	}
	
	
	/**
	 * Get the status of a VM
	 * @param session
	 * @param vmid
	 * @return true if it is turned on, false otherwise
	 * @throws IOException
	 */
	private static boolean getVmStatus(Session session, String vmid) throws IOException{

		// shell channel
        ChannelExec channel;
        String status="";
		try {
			
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("vim-cmd vmsvc/power.getstate "+vmid);
	
			// redirect of output
			channel.setOutputStream(System.out);
			channel.connect();
				
			InputStream in = channel.getInputStream();
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader rrr = new BufferedReader(isr);
			
			rrr.readLine(); // skip header
			status = rrr.readLine();
			
			channel.disconnect();
			
			if(status.contains("on")) return true;
			else return false;		
			
		} catch (JSchException e) { e.printStackTrace(); }
		return false;
	}
}
