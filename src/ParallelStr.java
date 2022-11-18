import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

class ParallelStr {
	public static void main(String[] args)
	{
		Scanner scan = new Scanner(System.in);
        System.out.println("Strassen Multiplication Algorithm  - parallel implementation\n");
        
        System.out.println("Enter order n: ");
        long startTime = System.nanoTime();
		
		int order = scan.nextInt();
		int A[][]=new int[order][order]; 
		int B[][]=new int[order][order];
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
		//cela main clasa do tuka e ista, razlika e so povikuvame metoda za paralelno mnozenje
		ParallelMatixMult t1 = new ParallelMatixMult(A, B, order); //zemi parametri za mnozenje
		double start = System.nanoTime();
		t1.proizvod(); //pocni so mnozenje 
		double end = System.nanoTime();
		int[][] C = t1.getResult(); //vrati rezultat od mnozenje
		
		DecimalFormat formatter = new DecimalFormat("#.######");
		double time = (end-start)/1000000000;
		System.out.println(" ");
		System.out.println("Execution time for matries of order "+order+": "+formatter.format(time)+" seconds");
	}

}

class ParallelMatixMult {
	//iniacilizacija na prazni matrici A B C
    private int[][] A;
    private int[][] B;
    private int[][] C;
    //slobodni procesori za da moze executor da kreira threads (dava moznost za prilagoduvanje od mashina do mashina)
    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final ExecutorService exec = Executors.newFixedThreadPool(POOL_SIZE);
    
    //oddavanje na parametri sho se vneseni
    ParallelMatixMult(int[][] A, int[][] B, int order) {
           this.A = A;
           this.B = B;
           this.C = new int[order][order];
       }
    //mnozenje so future - asinhrona execution 
    public void proizvod() {
		Future f = exec.submit(new Multiplier(A, B, C, 0, 0, 0, 0, 0, 0, A.length));
        try {
            f.get();
            exec.shutdown();
        } catch (Exception e) { System.out.println(e); }
    }
    
    public int[][] getResult() {
        return C;
    }
    //klasa koa so ke go implementria algoritamot
    class Multiplier implements Runnable {
        private int[][] A;
        private int[][] B;
        private int[][] C;
        private int a_i, a_j, b_i, b_j, c_i, c_j, size;

        Multiplier(int[][] A, int[][] B, int[][] C, int a_i, int a_j, int b_i, int b_j, int c_i, int c_j, int size)
		{
            this.A = A;
            this.B = B;
            this.C = C;
            this.a_i = a_i;
            this.a_j = a_j;
            this.b_i = b_i;
            this.b_j = b_j;
            this.c_i = c_i;
            this.c_j = c_j;
            this.size = size;
        }
    	//parametri za mat a i b i pocnuvanje na strassen()
        public void proizvod(int[][] a, int[][] b, int[][] c, int a_i, int a_j, int b_i, int b_j, int c_i, int c_j, int size)
		{
			if(size==1)
			{
				c[c_i][c_j]+=a[a_i][a_j]*b[b_i][b_j];
			}
			else	
			{
				int m=size/2;
				int [][] A11 = copy(A, a_i, a_i+m, a_j, a_j+m);
				int [][] A12 = copy(A, a_i+m, a_i+size, a_j, a_j+m);
				int [][] A21 = copy(A, a_i, a_i+m, a_j+m, a_j+size);
				int [][] A22 = copy(A, a_i+m, a_i+size, a_j+m, a_j+size);
				
				int [][] B11 = copy(B, b_i, b_i+m, b_j, b_j+m);
				int [][] B12 = copy(B, b_i+m, b_i+size, b_j, b_j+m);
				int [][] B21 = copy(B, b_i, b_i+m, b_j+m, b_j+size);
				int [][] B22 = copy(B, b_i+m, b_i+size, b_j+m, b_j+size);
				
				int [][] M1 = new int[m][m];
				int [][] M2 = new int[m][m];
				int [][] M3 = new int[m][m];
				int [][] M4 = new int[m][m];
				int [][] M5 = new int[m][m];
				int [][] M6 = new int[m][m];
				int [][] M7 = new int[m][m];
				
		        /** 
		        M1 = (A11 + A22)(B11 + B22)
		        M2 = (A21 + A22) B11
		        M3 = A11 (B12 - B22)
		        M4 = A22 (B21 - B11)
		        M5 = (A11 + A12) B22
		        M6 = (A21 - A11) (B11 + B12)
		        M7 = (A12 - A22) (B21 + B22)
		      **/
				
				proizvod(zbir(A11, A22, m), zbir(B11, B22, m), M1, 0, 0, 0, 0, 0, 0, m);
				proizvod(zbir(A21, A22, m), B11, M2, 0, 0, 0, 0, 0, 0, m);
				proizvod(A11, razlika(B12, B22, m), M3, 0, 0, 0, 0, 0, 0, m);
				proizvod(A22, razlika(B21, B11, m), M4, 0, 0, 0, 0, 0, 0, m);
				proizvod(zbir(A11, A12, m), B22, M5, 0, 0, 0, 0, 0, 0, m);
				proizvod(razlika(A21, A11, m), zbir(B11, B12, m), M6, 0, 0, 0, 0, 0, 0, m);
				proizvod(razlika(A12, A22, m), zbir(B21, B22, m), M7, 0, 0, 0, 0, 0, 0, m);
				
		        /**
		        C11 = M1 + M4 - M5 + M7
		        C12 = M3 + M5
		        C21 = M2 + M4
		        C22 = M1 - M2 + M3 + M6
		      **/	
				
				combine(C, zbir(razlika(zbir(M1,M4,m),M5,m),M7,m), 
						zbir(M3,M5,m), 
						zbir(M2,M4,m), 
						zbir(razlika(zbir(M1,M3,m), M2, m), M6,m),
						c_i,c_j,size);
			}
		}
		//klasata vo matricata C prave join
        public void combine(int[][] C, int[][] c11, int[][] c12, int[][] c21, int[][] c22, int c_i, int c_j, int n)
		{
			int m=n/2;
			
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < m; j++) {
					C[c_i+i][c_j+j] += c11[i][j];
				}
			}
			
			for (int i = m; i < 2*m; i++) {
				for (int j = 0; j < m; j++) {
					C[c_i+i][c_j+j] += c12[i-m][j];
				}
			}
			
			for (int i = 0; i < m; i++) {
				for (int j = m; j < 2*m; j++) {
					C[c_i+i][c_j+j] += c21[i][j-m];
				}
			}
			
			for (int i = m; i < 2*m; i++) {
				for (int j = m; j < 2*m; j++) {
					C[c_i+i][c_j+j] += c22[i-m][j-m];
				}
			}
		}
		
        public int[][] zbir(int[][] A, int[][] B,int n)
		{
			int[][] C = new int[n][n];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					C[i][j] = A[i][j] + B[i][j];
				}
			}
			return C;
		}
		
		public int[][] razlika(int[][] A, int[][] B,int n)
		{
			int[][] C = new int[n][n];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					C[i][j] = A[i][j] - B[i][j];
				}
			}
			return C;
		}
		
		public int[][] copy(int[][] A, int n1, int n2, int p1, int p2) 
		{
			int[][] C = new int[n2-n1][p2-p1];
			for (int i = n1; i < n2; i++) {
				for (int j = p1; j < p2; j++) {
					C[i-n1][j-p1] = A[i][j];
				}
			}
			return C;
		}
    	
    	//sekoja sekcenca podelena od originalnite matrici so obsekog vo koj treba da se izvrse mnozenjeto
		public void run() 
		{
            int h = size/2;
            if (size <= 64) 
			{
                proizvod(A, B, C, a_i, a_j, b_i, b_j, c_i, c_j, size);
            } 
			else 
			{
                Multiplier[] tasks = {
                    new Multiplier(A, B, C, a_i, a_j, b_i, b_j, c_i, c_j, h),
                    new Multiplier(A, B, C, a_i, a_j+h, b_i+h, b_j, c_i, c_j, h),

                    new Multiplier(A, B, C, a_i, a_j, b_i, b_j+h, c_i, c_j+h, h),
                    new Multiplier(A, B, C, a_i, a_j+h, b_i+h, b_j+h, c_i, c_j+h, h),

                    new Multiplier(A, B, C, a_i+h, a_j, b_i, b_j, c_i+h, c_j, h),
                    new Multiplier(A, B, C, a_i+h, a_j+h, b_i+h, b_j, c_i+h, c_j, h),

                    new Multiplier(A, B, C, a_i+h, a_j, b_i, b_j+h, c_i+h, c_j+h, h),
                    new Multiplier(A, B, C, a_i+h, a_j+h, b_i+h, b_j+h, c_i+h, c_j+h, h)
                };
                FutureTask[] fs = new FutureTask[tasks.length/2];
                for (int i = 0; i < tasks.length; i+=2) 
				{
                    fs[i/2] = new FutureTask(new Sequentializer(tasks[i], tasks[i+1]), null);
                    exec.execute(fs[i/2]);
                }
                for (int i = 0; i < fs.length; ++i) 
				{
                    fs[i].run();
                }
                try 
				{
                    for (int i = 0; i < fs.length; ++i) 
					{
                        fs[i].get();
                    }
                } 
				catch (Exception e) 
				{
					System.out.println(e);
                }
            }
        }
    }
    
    class Sequentializer implements Runnable
	{
        private Multiplier first, second;
        Sequentializer(Multiplier first, Multiplier second) 
		{
            this.first = first;
            this.second = second;
        }
        public void run() 
		{
            first.run();
            second.run();
        }
    }
}
