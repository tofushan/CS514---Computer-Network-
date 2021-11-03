import java.util.*;
import java.io.*;
import java.text.*;
import java.net.*;




public class Server {
    
    static int count = 0;
    static Vector<ClientHandler> clientList = new Vector<>();

    public static void main(String[] args) throws IOException {
        
        // esstablish a server listening on port 5056
        ServerSocket ss = new ServerSocket(5056);
        
        while (true) {
            
            Socket s = null;
            
            try {
                
                // accepting state : socket object receive incoming client request
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                DataInputStream datain = new DataInputStream(s.getInputStream());
                DataOutputStream dataout = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");
                
                // create a new thread object 
                String c_name = "Client "+ count;
                ClientHandler c = new ClientHandler(c_name, s, datain, dataout);
                
                // create new thread for this object
                Thread t = new Thread(c);
                
                clientList.add(c);

                t.start();
            }
            catch (Exception e) {
                s.close();
                e.printStackTrace();
            }

            count += 1;
        }
    }
}


class ClientHandler implements Runnable {
    
    private String name; 
    private boolean isLogin = false; 
    private Socket s = null;
    private DataInputStream datain = null;
    private DataOutputStream dataout = null;
    Scanner scn = new Scanner(System.in);

    // constructor 
    public ClientHandler( String name, Socket s, DataInputStream datain, DataOutputStream dataout ) {
        this.name = name;
        this.s = s;
        this.datain = datain;
        this.dataout = dataout;
        this.isLogin = true;
    }

    @Override
    public void run() {
        String received;
        String toreturn;
        
        while (true) {

            try {
                
                received = datain.readUTF();
                System.out.println(received);

                if (received.equals("logout")) {
                    this.isLogin = false;
                    s.close();
                    System.out.format("Closing %s connection.", this.name);
                    break;
                }
                
                StringTokenizer entire_msg = new StringTokenizer(received, "#");
                String message = entire_msg.nextToken();
                String recipient = entire_msg.nextToken();
            
                for ( ClientHandler c: Server.clientList ) {
                    if ( c.name.equals(recipient) && c.isLogin == true ) {
                        c.dataout.writeUTF(this.name + ": " + message);
                        break;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            this.datain.close();
            this.dataout.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


}
