package curr;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Matrix {
    final public double[][] matrix;
    public double[][] T;
    public int sizer, sizec;
    private int sign = 1;

    public Matrix(double[][] m, int sizer, int sizec) {
        this.matrix = m;
        this.sizec = sizec;
        this.sizer = sizer;
    }

    public void print() {
        for (int i = 0; i < this.sizer; ++i) {
            for (int j = 0; j < this.sizec; ++j) {
                System.out.print(this.matrix[i][j]);
                System.out.print(j == this.sizec - 1 ? "\n" : " ");
            }
        }
    }

    static double[][] arrayCopy(double[][] arr) {
        double[][] newArr = new double[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; ++i) {
            System.arraycopy(arr[i], 0, newArr[i], 0, arr[0].length);
        }
        return newArr;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(new double[this.sizec][this.sizer], this.sizec, this.sizer);
        for (int i = 0; i < this.sizer; ++i) {
            for (int j = 0; j < this.sizec; ++j) {
                result.matrix[j][i] = this.matrix[i][j];
            }
        }
        return result;
    }

    public Matrix cofactor(int p, int q) {
        assert this.sizec == this.sizer : "the matrix must be a square matrix";

        Matrix result = new Matrix(new double[this.sizer][this.sizec], this.sizer-1, this.sizec-1);
        int i = 0;
        int j = 0;

        for (int row = 0; row < this.sizer; ++row) {
            for (int col = 0; col < this.sizec; ++col) {
                if (row != p && col != q) {
                   result.matrix[i][j++] = this.matrix[row][col];
                    if (j == this.sizer - 1) {
                        j = 0;
                        i++;
                    } 
                }
                
            }
        }
        return result;
    }

    // public double determinant(int n) {
    //     assert this.sizec == this.sizer : "the matrix must be a square matrix";

    //     double d = 0;
    //     if (this.sizer == 1)
    //         return this.matrix[0][0];
    //     Matrix temp = new Matrix(new double[this.sizer][this.sizec], this.sizer, this.sizec);
    //     double sign = 1;

    //     for (int i = 0; i < n; ++i) {
    //         temp = this.cofactor(0, i);
    //         d += sign * this.matrix[0][i] * temp.determinant(n - 1);
    //         sign = -sign;
    //     }

    //     return d;
    // }

    public Matrix adjoint() {
        Matrix adj = new Matrix(new double[this.sizer][this.sizec], this.sizer, this.sizec);

        if (this.sizer == 1) {
            adj.matrix[0][0] = 1;
            return adj;
        }

        int sign = 1;
        Matrix temp;

        for (int i = 0; i < this.sizer; i++) {
            for (int j = 0; j < this.sizer; j++) {
                // Get cofactor of A[i][j]
                temp = this.cofactor(i, j);

                // sign of adj[j][i] positive if sum of row
                // and column indexes is even.
                sign = ((i + j) % 2 == 0) ? 1 : -1;

                // Interchanging rows and columns to get the
                // transpose of the cofactor sizer
                adj.matrix[j][i] = (sign) * (temp.determinant());
            } 
        }
        return adj;
    }

    // Function to calculate and store inverse, returns false if
    // sizer is singular
    public Matrix inverse() {
        // Find determinant of A[][]
        Matrix inv = new Matrix(new double[this.sizer][this.sizec], this.sizer, this.sizec);
        Matrix copy = new Matrix(arrayCopy(matrix), sizer, sizec);
        double det = copy.determinant();
        if (det == 0) {
            System.out.print("Singular sizer, can't find its inverse");
            return null;
        }
        
        Matrix adj = this.adjoint();

        for (int i = 0; i < this.sizer; i++)
            for (int j = 0; j < this.sizer; j++)
                inv.matrix[i][j] = adj.matrix[i][j] / det;

        return inv;
    }

    public Matrix multiply(Matrix m) throws InterruptedException {
        assert this.sizec == m.sizer : "the size of the matrix must be the same";

        Matrix result = new Matrix(new double[this.sizer][m.sizec], this.sizer, m.sizec);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < this.sizer; ++i) {
            for (int j = 0; j < m.sizec; ++j) {
                pool.submit(new Multi(this.sizec, i, j, this.matrix, m.matrix, result.matrix));
            }
        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

        return result;
    }

    static class Multi implements Runnable {
        final int N;
        final double[][] a;
        final double[][] b;
        final double[][] c;
        final int i;
        final int j;

        public Multi(int N, int i, int j, double[][] a, double[][] b, double[][] c) {
            this.N = N;
            this.i = i;
            this.j = j;
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public void run() {
            for (int k = 0; k < N; ++k)
                c[i][j] += a[i][k] * b[k][j];
        }
    }
    public int getSign() {
        return sign;
    }

    public double determinant() {

        if (sizer == 1) return matrix[0][0];

        double deter;
        if (isUpperTriangular() || isLowerTriangular())
            deter = multiplyDiameter() * sign;

        else {
            makeTriangular();
            deter = multiplyDiameter() * sign;

        }
        return deter;
    }

    /*
     * receives a matrix and makes it triangular using allowed operations
     * on columns and rows
     */
    public void makeTriangular() {

        for (int j = 0; j < matrix.length; j++) {
            sortCol(j);
            for (int i = matrix.length - 1; i > j; i--) {
                if (matrix[i][j] == 0)
                    continue;

                double x = matrix[i][j];
                double y = matrix[i - 1][j];
                multiplyRow(i, (-y / x));
                addRow(i, i - 1);
                multiplyRow(i, (-x / y));
            }
        }
    }

    public boolean isUpperTriangular() {

        if (matrix.length < 2)
            return false;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < i; j++) {
                if (matrix[i][j] != 0)
                    return false;

            }

        }
        return true;
    }

    public boolean isLowerTriangular() {

        if (matrix.length < 2)
            return false;

        for (int j = 0; j < matrix.length; j++) {
            for (int i = 0; j > i; i++) {
                if (matrix[i][j] != 0)
                    return false;

            }

        }
        return true;
    }

    public double multiplyDiameter() {

        double result = 1;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i == j)
                    result = result * matrix[i][j];

            }

        }
        return result;
    }

    // when matrix[i][j] = 0 it makes it's value non-zero
    public void makeNonZero(int rowPos, int colPos) {

        int len = matrix.length;

        outer: for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (matrix[i][j] != 0) {
                    if (i == rowPos) { // found "!= 0" in it's own row, so cols must be added
                        addCol(colPos, j);
                        break outer;

                    }
                    if (j == colPos) { // found "!= 0" in it's own col, so rows must be added
                        addRow(rowPos, i);
                        break outer;
                    }
                }
            }
        }
    }

    // add row1 to row2 and store in row1
    public void addRow(int row1, int row2) {
        double[][] _matrix = matrix.clone(); 
        for (int j = 0; j < matrix.length; j++)
            _matrix[row1][j] += matrix[row2][j];
    }

    // add col1 to col2 and store in col1
    public void addCol(int col1, int col2) {

        for (int i = 0; i < matrix.length; i++)
            matrix[i][col1] += matrix[i][col2];
    }

    // multiply the whole row by num
    public void multiplyRow(int row, double num) {

        if (num < 0)
            sign *= -1;

        for (int j = 0; j < matrix.length; j++) {
            matrix[row][j] *= num;
        }
    }

    // multiply the whole column by num
    public void multiplyCol(int col, double num) {

        if (num < 0)
            sign *= -1;

        for (int i = 0; i < matrix.length; i++)
            matrix[i][col] *= num;

    }

    // sort the cols from the biggest to the lowest value
    public void sortCol(int col) {

        for (int i = matrix.length - 1; i >= col; i--) {
            for (int k = matrix.length - 1; k >= col; k--) {
                double tmp1 = matrix[i][col];
                double tmp2 = matrix[k][col];

                if (Math.abs(tmp1) < Math.abs(tmp2))
                    replaceRow(i, k);
            }
        }
    }

    // replace row1 with row2
    public void replaceRow(int row1, int row2) {

        if (row1 != row2)
            sign *= -1;

        double[] tempRow = new double[matrix.length];

        for (int j = 0; j < matrix.length; j++) {
            tempRow[j] = matrix[row1][j];
            matrix[row1][j] = matrix[row2][j];
            matrix[row2][j] = tempRow[j];
        }
    }

    // replace col1 with col2
    public void replaceCol(int col1, int col2) {

        if (col1 != col2)
            sign *= -1;

        System.out.printf("replace col%d with col%d, sign = %d%n", col1, col2, sign);
        double[][] tempCol = new double[matrix.length][1];

        for (int i = 0; i < matrix.length; i++) {
            tempCol[i][0] = matrix[i][col1];
            matrix[i][col1] = matrix[i][col2];
            matrix[i][col2] = tempCol[i][0];
        }
    }
}