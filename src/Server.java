import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements Worker {

        public Server() {}

        public int[][] multiply(int[][] A, int[][] B)
        {
                // bi mozele za distributivno da go koristime i paralelniot kod 
                Strassen s = new Strassen();
                
                return s.proizvod(A, B);
        }
        //pravime konekciaj na serverot, host 127.0.0.1 i porta 3000(toa e na arg[0], go vnesuvame u run config -> server -> arguments)
        public static void main(String args[]) 
        {                
                try {
                        int port = Integer.parseInt(args[0]);
                        System.err.println("Creating a new RMI registry at port: " + args[0]);
                        Registry registry = LocateRegistry.createRegistry(port);

                        Server obj = new Server();
                        Worker stub = (Worker) UnicastRemoteObject.exportObject(obj, 0);
                        
                        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
                        registry.bind("Multiplier", stub);
                        System.err.println("Server ready...");
                } catch (Exception e) {
                        System.err.println("Server exception: " + e.toString());
                        e.printStackTrace();
                }
        }
}
