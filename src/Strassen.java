
/**
Strassen Algorithm - Petar Todorovski, 89181109
 **/
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Strassen 
{
	public int[][] proizvod(int[][] A, int[][] B)
{
	int x = A.length;
	int [][] Base = new int[x][x];

	if (x == 1)
		Base[0][0] = A[0][0] * B[0][0];
	else {
		
		int[][] A11  = new int[x/2][x/2];
		int[][] A12  = new int[x/2][x/2];
		int[][] A21  = new int[x/2][x/2];
		int[][] A22  = new int[x/2][x/2];
		int[][] B11  = new int[x/2][x/2];
		int[][] B12  = new int[x/2][x/2];
		int[][] B21  = new int[x/2][x/2];
		int[][] B22  = new int[x/2][x/2];
		
	/**split matrix A and B into 4 halves**/
		split(A, A11, 0, 0);
		split(A, A12, 0, x/2);
		split(A, A21, x/2, 0);
		split(A, A22, x/2, x/2);
		
		split(B, B11, 0, 0);
		split(B, B12, 0, x/2);
		split(B, B21, x/2, 0);
		split(B, B22, x/2, x/2);

        /** 
        M1 = (A11 + A22)(B11 + B22)
        M2 = (A21 + A22) B11
        M3 = A11 (B12 - B22)
        M4 = A22 (B21 - B11)
        M5 = (A11 + A12) B22
        M6 = (A21 - A11) (B11 + B12)
        M7 = (A12 - A22) (B21 + B22)
      **/
		int [][] M1 = proizvod(zbir(A11, A22), zbir(B11, B22));
		int [][] M2 = proizvod(zbir(A21, A22), B11);
		int [][] M3 = proizvod(A11, razlika(B12, B22));
		int [][] M4 = proizvod(A22, razlika(B21, B11));
		int [][] M5 = proizvod(zbir(A11, A12), B22);
		int [][] M6 = proizvod(razlika(A21, A11), zbir(B11, B12));
		int [][] M7 = proizvod(razlika(A12, A22), zbir(B21, B22));
        /**
        C11 = M1 + M4 - M5 + M7
        C12 = M3 + M5
        C21 = M2 + M4
        C22 = M1 - M2 + M3 + M6
      **/	
        int [][] C11 = zbir(razlika(zbir(M1, M4), M5), M7);
        int [][] C12 = zbir(M3, M5);
        int [][] C21 = zbir(M2, M4);
        int [][] C22 = zbir(razlika(zbir(M1, M3), M2), M6);
        
        join(C11, Base, 0 , 0);
        join(C12, Base, 0 , x/2);
        join(C21, Base, x/2, 0);
        join(C22, Base, x/2, x/2);
	}
	return Base;	
}

public void split(int[][] T, int[][] C, int iB, int jB) 
	{
		for(int i1 = 0, i2 = iB; i1 < C.length; i1++, i2++)
			for(int j1 = 0, j2 = jB; j1 < C.length; j1++, j2++)
				C[i1][j1] = T[i2][j2];
	}

public int[][] zbir(int[][] A, int[][] B)
	{
	 int x = A.length;
	    int[][] C = new int[x][x];
	    for (int i = 0; i < x; i++)
	        for (int j = 0; j < x; j++)
	            C[i][j] = A[i][j] + B[i][j];
	    return C;
	}

public int[][] razlika(int[][] A, int[][] B)
	{
    int x = A.length;
    int[][] C = new int[x][x];
    for (int i = 0; i < x; i++)
        for (int j = 0; j < x; j++)
            C[i][j] = A[i][j] - B[i][j];
    return C;
	}

public void join(int[][] C, int[][] T, int iB, int jB) 
	{
    	for(int i1 = 0, i2 = iB; i1 < C.length; i1++, i2++)
    		for(int j1 = 0, j2 = jB; j1 < C.length; j1++, j2++)
    			T[i2][j2] = C[i1][j1];
	} 

	public static void main (String[] args)  throws InterruptedException 
		{
        Scanner scan = new Scanner(System.in);
        System.out.println("Strassen Multiplication Algorithm - sequential implementation\n");
        Strassen s = new Strassen();
        
        System.out.println("Enter order n:");
        long startTime = System.nanoTime();
        
        int order = scan.nextInt();
        int[][] A = new int[order][order];
        int[][] B = new int[order][order];
        Random rand = new Random();

        if (order < 10)
        System.out.println("The first matrix has the values: ");
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                A[i][j] = rand.nextInt(10);
                if (order < 10) {
                    System.out.print(A[i][j] + " | ");
                }
            }
            if (order < 10)
            System.out.println();
        } 
        
        
        if (order < 10) {
        System.out.println("  ");
        System.out.println("The second matrix has the values: "); }
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                B[i][j] = rand.nextInt(10);
                if (order < 10) {
                    System.out.print(B[i][j] + " | ");}
            }
            if (order < 10)
            System.out.println();
        }
        
        if (order < 10) {
        int[][] C = s.proizvod(A, B);
        
        System.out.println("\nProduct of matrices A and  B : ");

        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++)
                System.out.print(C[i][j] +" | ");
            System.out.println();
        }
        }
        
		TimeUnit.SECONDS.sleep(1);
		
		long endTime = System.nanoTime();
		long timeElapsed = endTime - startTime;

		System.out.println(" ");
		System.out.println("Execution time for matrices of order "+order+" is: " +timeElapsed+" nanoseconds");
		System.out.println("Execution time for matrices of order "+order+" is: " +timeElapsed / 1000000F+" milliseconds");
		System.out.println("Execution time for matrices of order "+order+" is: " +timeElapsed / 1000000000F+" seconds");
	}
}