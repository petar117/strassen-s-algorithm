import java.lang.Runtime;
import java.lang.InterruptedException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

class Address {
        private String host;
        private int port;

        public Address(String host, int port) 
        {
                this.host = host;
                this.port = port;
        }

        public String host() { return this.host; }

        public int port() { return this.port; }
}

//Ovaa klasa go presmetuva proizvodot na dve matrici remotely koristejci 
//Java's Remote Method Invocation. Si odbira random worker.
class RemoteMultiplier {
        private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
        public static final ExecutorService exec = Executors.newFixedThreadPool(POOL_SIZE);
        private static final Random rand = new Random();

        //Koristime nekolku porti - adresi na site workers registarski serveri, ke demonstrirame samo so edna - 3000
        private static final Address[] workers = {
                new Address("127.0.0.1", 3000)/*,
                new Address("127.0.0.1", 3001),
                new Address("127.0.0.1", 3002),*/
        };

        public Future<int[][]> compute(int[][] A, int[][] B) throws
                RemoteException, NotBoundException
        {
                Address addr = workers[rand.nextInt(workers.length)];
                Registry registry = LocateRegistry.getRegistry(
                        addr.host(), addr.port());
                Worker stub = (Worker)registry.lookup("Multiplier");
                return exec.submit(() -> {
                                return stub.multiply(A, B);
                        });
        }
}

public class Client {
        private static final int MAX_PRINTED_ORDER = 10;
        private static final Random rand = new Random();

        private Client() {}


        private static void printMatrix(int[][] M, int order)
        {
                for (int i = 0; i < order; i++) {
                        for (int j = 0; j < order; j++) 
                                System.out.print(M[i][j] + " | ");
                        System.out.println();
                }                
        }

        private static void fillMatrices(int[][] A, int[][] B, int order)
        {
                for (int i = 0; i < order; i++) {
                        for (int j = 0; j < order; j++) {
                                A[i][j] = rand.nextInt(10);
                                B[i][j] = rand.nextInt(10);
                        }
                }
        }

        //Ovaa metoda go presmetuva proizvodot na dvete matrici.
        //Go koriste prviot cekor od Strassens algoritam.
        //No sekoe mnozenje e presmetano od strana na remote worker.
        private static int[][] multiply(int[][] A, int[][] B) throws
                RemoteException, NotBoundException, InterruptedException,
                ExecutionException
        {
                int n = A.length;
                int[][] C = new int[n][n];

                if (n == 1) {
                        C[0][0] = A[0][0] * B[0][0];
                        return C;
                }

                int[][] A11 = split(A, n/2, 0, 0);
                int[][] A12 = split(A, n/2, 0, n/2);
                int[][] A21 = split(A, n/2, n/2, 0);
                int[][] A22 = split(A, n/2, n/2, n/2);

                int[][] B11 = split(B, n/2, 0, 0);
                int[][] B12 = split(B, n/2, 0, n/2);
                int[][] B21 = split(B, n/2, n/2, 0);
                int[][] B22 = split(B, n/2, n/2, n/2);

                // Run the remote products in parallel by executing
                // them in different threads.
                Future<int[][]> f1 = new RemoteMultiplier().compute(zbir(A11, A22), zbir(B11, B22));
                Future<int[][]> f2 = new RemoteMultiplier().compute(zbir(A21, A22), B11);
                Future<int[][]> f3 = new RemoteMultiplier().compute(A11, razlika(B12, B22));
                Future<int[][]> f4 = new RemoteMultiplier().compute(A22, razlika(B21, B11));
                Future<int[][]> f5 = new RemoteMultiplier().compute(zbir(A11, A12), B22);
                Future<int[][]> f6 = new RemoteMultiplier().compute(razlika(A21, A11), zbir(B11, B12));
                Future<int[][]> f7 = new RemoteMultiplier().compute(razlika(A12, A22), zbir(B21, B22));

                int[][] M1 = f1.get();
                int[][] M2 = f2.get();
                int[][] M3 = f3.get();
                int[][] M4 = f4.get();
                int[][] M5 = f5.get();
                int[][] M6 = f6.get();
                int[][] M7 = f7.get();

                RemoteMultiplier.exec.shutdown();
                
                int[][] C11 = razlika(zbir(zbir(M1, M4), M7), M5);
                int[][] C12 = zbir(M3, M5);
                int[][] C21 = zbir(M2, M4);
                int[][] C22 = razlika(zbir(zbir(M1, M3), M6), M2);

                join(C11, C, 0, 0);
                join(C12, C, 0, n/2);
                join(C21, C, n/2, 0);
                join(C22, C, n/2, n/2);
                
                return C;
        }

        private static int[][] split(int[][] M, int size, int imin, int jmin)
        {
                int[][] res = new int[size][size];

                for (int i = 0; i < size; i++)
                        for (int j = 0; j < size; j++)
                                res[i][j] = M[imin+i][jmin+j];

                return res;
        }

        private static void join(int[][] src, int[][] dst, int imin, int jmin)
        {
                for (int i = 0; i < src.length; i++)
                        for (int j = 0; j < src.length; j++)
                                dst[imin+i][jmin+j] = src[i][j];
        }

        private static int[][] zbir(int[][] A, int[][] B)
        {
                int n = A.length;
                int[][] C = new int[n][n];

                for (int i = 0; i < n; i++)
                        for (int j = 0; j < n; j++)
                                C[i][j] = A[i][j] + B[i][j];
                return C;
        }

        private static int[][] razlika(int[][] A, int[][] B)
        {
                int n = A.length;
                int[][] C = new int[n][n];

                for (int i = 0; i < n; i++)
                        for (int j = 0; j < n; j++)
                                C[i][j] = A[i][j] - B[i][j];
                return C;
        }
        
        public static void main(String[] args) throws
                RemoteException, NotBoundException, InterruptedException,
                ExecutionException
        {
                Scanner scan = new Scanner(System.in);
                System.out.println("Strassen Multiplication Algorithm - distributed implementation\n");
                System.out.print("Enter the order n: ");

                int order = scan.nextInt();

                // Strassen's algorithm only works when the order is a
                // power of two.  So we pad the matrices with zeros if
                // necessary. This doesn't change the complexity of
                // the algorithm since the new value is at most equal
                // to 2*order.
                int N = 1;
                while (N < order)
                        N *= 2;
                
                int[][] A = new int[N][N];
                int[][] B = new int[N][N];

                fillMatrices(A, B, order);

    /*         if (order < MAX_PRINTED_ORDER) {
                        System.out.println(
                                "\nThe first matrix has the values:");
                        printMatrix(A, order);
                        System.out.println(
                                "\nThe second matrix has the values:");
                        printMatrix(B, order);
                }
*/
                long startTime = System.nanoTime();

                int[][] C = multiply(A, B);               

                long timeElapsed = System.nanoTime() - startTime;

/*                System.out.println(
                        "\nThe product of the first and second matrices:");
                printMatrix(C, order);*/
        		
        System.out.println(" ");
		System.out.println("Execution time for matrices of order "+order+" is: " +timeElapsed+" nanoseconds");
		System.out.println("Execution time for matrices of order "+order+" is: " +timeElapsed / 1000000F+" milliseconds");
		System.out.println("Execution time for matrices of order "+order+" is: " +timeElapsed / 1000000000F+" seconds");

        }
}
