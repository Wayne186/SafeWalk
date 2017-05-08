package safewalk;

/**
 * Project 5
 * @author Weifeng Huang, huang636, labsec07
 * @author Li Shen, shen212, labsec07 (can be omitted if working alone)
 */


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class SafeWalkServer extends ServerSocket implements Runnable {

	private int port;
	private ServerSocket serverSocket;
	private ArrayList<Socket> socket = new ArrayList<Socket>();
	private ArrayList<String[]> pendingEvent = new ArrayList<String[]>();
	private BufferedReader in;
	private PrintWriter out;
	private String[] events = {};
	private final String[] LOCATION = {"CL50", "EE", "LWSN", "PMU", "PUSH", "*"};
 
	public SafeWalkServer(int port) throws IOException, SocketException {
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
		if (port < 1025 || port > 65535) {
			System.out.println("Invalid port, exit.");
		}
	}
 
	public SafeWalkServer() throws IOException, SocketException {
		serverSocket = new ServerSocket(0);
		this.port = serverSocket.getLocalPort();
		serverSocket.setReuseAddress(true);
		System.out.println("Port not specified. Using free port "+ port +".");
	}
 
	public int getLocalPort() {
		return this.port;
	}
 
	public void run() {
		while (true) {
			Socket client = null;
			String commend = "";
			boolean br = false;
			try {
				client = serverSocket.accept();
			} catch (IOException e) {
				System.err.println(e);
			}
			try {
    
				InputStreamReader isr = new InputStreamReader(client.getInputStream());
				in = new BufferedReader(isr);
				out = new PrintWriter(client.getOutputStream(), true);
				commend = in.readLine();
			} catch (IOException e) {
				System.err.println(e);
			}
			
			try {
				boolean check = checkValidity(commend);
				System.out.println(check);
				//System.out.println(events.length);
				System.out.println(commend);
				if (check) {
					br = handleCommend(commend, client);
					System.out.println(br);
				}
			} catch ( IOException e) {
				System.err.println(e);
			}
			if(br) {
				try {
					cleanUp(client);
				} catch (IOException e ) {
					out.println(e);
				}
				break;
			}
		}
	}
 
	public boolean checkValidity(String str) {
		boolean c = false;
		if(str.equals(":RESET")) {
			return true;
		}
		if(str.equals(":SHUTDOWN")) {
			return true;
		}
		return c;
	}
	 
	public boolean handleCommend(String str, Socket s) throws IOException, SocketException {
		boolean b = false;
		if (str.equals(":RESET")) {
			Socket temp;
			PrintWriter tempOut;
			for(int i = 0; i < pendingEvent.size(); i++){
				temp = socket.get(i);
				tempOut = new PrintWriter(temp.getOutputStream(), true);
				tempOut.println("ERROR: connection reset");
				tempOut.close();
				temp.close();
				out.println("ERROR: connection reset");
			}
			out.println("RESPONSE: success");
			s.close();
			pendingEvent.clear();
			socket.clear();
			b = true;
		}
		else if (str.equals(":SHUTDOWN")) {
			Socket temp;
			PrintWriter tempOut;
			for(int i = 0; i < pendingEvent.size(); i ++){
				temp = socket.get(i);
				tempOut = new PrintWriter(temp.getOutputStream(), true);
				tempOut.println("ERROR: connection reset");
				tempOut.close();
				temp.close();
				out.println("ERROR: connection reset");
			}
			out.println("RESPONSE: shutting down");
			out.close();
			s.close();
			pendingEvent.clear();
			socket.clear();
			serverSocket.close();
			b = true;
		}
		return b;
	}
	 
	public void cleanUp(Socket c)  throws SocketException, IOException {
		out.close();
		in.close();
		c.close();
	}
	 
}
