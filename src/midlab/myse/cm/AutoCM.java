package midlab.myse.cm;

import java.util.LinkedList;

import midlab.myse.cm.ssh.SSHConnection;

/**
 * Automatic Configuration Manager
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class AutoCM {

	/**
	 * A main to prove the change of configuration
	 * @param args
	 */
	public static void main(String[] args){
		newConfiguration(4);
	}
	
	/**
	 * Configures a new configuration
	 * @param newConf the value of the new desiderata configuration
	 */
	public static void newConfiguration(int newConf){
		
		SSHConnection sshc = new SSHConnection();
		
		String[][] vm = sshc.get();
		
		int currentConf = currentConf(vm);
		int diff = Math.abs(newConf-currentConf); //difference between current and next configuration
		
		System.out.println("Current conf: "+currentConf+"; New conf: "+newConf);
		
		LinkedList<String[]> toSwitch = new LinkedList<String[]>();
		String[] host_vmid; //contains host e vmid
				
		if(newConf>currentConf){
			for(int i=0; i<vm.length; ++i){
				if(vm[i][3].equals("OFF") && diff!=0){
					host_vmid = new String[2];
					host_vmid[0] = vm[i][0];
					host_vmid[1] = vm[i][1];
					toSwitch.add(host_vmid);
					--diff;
					System.out.println("host: " + host_vmid[0] + "; vmid: " + host_vmid[1]);
				}
			}
		}
		if(newConf<currentConf){
			//turn off machine with policy LIFO
			for(int i=vm.length-1; i>0; --i){
				if(vm[i][3].equals("ON") && diff!=0){
					host_vmid = new String[2];
					host_vmid[0] = vm[i][0];
					host_vmid[1] = vm[i][1];
					toSwitch.add(host_vmid);
					--diff;
					System.out.println("host: " + host_vmid[0] + "; vmid: " + host_vmid[1]);
				}
			}
		}
		
		String[] current;
		String host = "";
		String vmid = "";
		boolean result = true;
		
		for(int i=0; i<toSwitch.size(); ++i){
			current = toSwitch.get(i);
			host = current[0];
			vmid = current[1];
			System.out.println("i=" + i + "; host=" + host + "; vmid=" + vmid);
			System.out.println("Changin state of " + vmid);
			result = sshc.action(host, vmid);
			if(result){
				System.out.println("State of " + vmid + " switched succesfully.");
			}
			else{
				System.out.println("Error during switching the state of " + vmid + ". Exiting...");
				break;
			}
			current = null;
			host = null;
			vmid = null;
		}
		if(result) System.out.println("Done. New configuration is available.\n");
		else System.out.println("Error committed changing configuration.\n");
		
	}
	
	/**
	 * Returns the current configuration (number of active replicas)
	 * @param vm
	 * @return
	 */
	private static int currentConf(String[][] vm){
		int conf = 0;
		for(int i=0; i<vm.length; ++i){
			if(vm[i][3].equals("ON")) ++conf;
		}
		return conf;
	}
}
