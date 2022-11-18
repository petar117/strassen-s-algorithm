import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Worker extends Remote {
        int[][] multiply(int[][] A, int[][] B) throws RemoteException;
}
