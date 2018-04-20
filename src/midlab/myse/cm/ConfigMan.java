package midlab.myse.cm;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import midlab.myse.cm.ssh.SSHConnection;



/**
 * Servlet implementation class ConfigMan
 */
@WebServlet("/ConfigMan")
public class ConfigMan extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConfigMan() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Enumeration<String> att = request.getParameterNames();
			
		int cont=0;
		while(att.hasMoreElements()){
			att.nextElement();
			++cont;
		}
		
		PrintWriter out = response.getWriter();
		out.println("servlet");
		out.println(cont);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Enumeration<String> att = request.getParameterNames();
		PrintWriter out = response.getWriter();
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		LinkedList<String> vmids = new LinkedList();
		while(att.hasMoreElements()) vmids.add(att.nextElement());
				
		SSHConnection sshc = new SSHConnection();
		
		boolean result = false;
		String current = "";
		String host = "";
		String vmid = "";
		
		for(int i=0; i<vmids.size(); ++i){
			current = vmids.get(i);
			host = parserHost(current);
			vmid = parserId(current);
			
			System.out.println("Switching state of " + vmid + " belonging to server " + host + "...");
			out.println("Switching state of " + vmid + " belonging to server " + host + "...");
			result = sshc.action(host, vmid);
			if(result){
				System.out.println("State of " + current + " switched succesfully.");
				out.println("State of " + current + " switched succesfully.");
			}
			else{
				out.println("Error during switching the state of " + current + ". Exiting...");
				break;
			}
			
		}
		
		if(result) out.println("Done. New configuration is available.\n");
		else out.println("Error committed changing configuration.\n");
		
		request.getRequestDispatcher("/ShowVM.jsp").forward(request,response);
		
	}

	private static String parserHost(String current){
		System.out.println("PARSER HOST: "+current);
		String host = current;
		int index = 0;
		while(!host.endsWith("&")){
			host = host.substring(0, current.length()-index);
			++index;
		}
		host = host.substring(0, current.length()-index);
		System.out.println("HOST: " + host);
		System.out.println();
		return host;
	}
	
	private static String parserId(String current){
		System.out.println("PARSER ID: " + current);
		String vmid = current;
		while(!vmid.startsWith("&")){
			vmid = vmid.substring(1);
		}
		vmid = vmid.substring(1);
		System.out.println("VMID: " + vmid);
		System.out.println();
		return vmid;
	}
}
