package testDecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class DataAnalysis {

    public static void main(String[] args) {
        // double[] X = { 12d, 131d, 11d, 345d, 74d };
        // double[] y = { 4d, 5d, 12d, 4d, 65d };
        // double[] m = new double[2];
        // try {
        // m = getLinearRegression(X, y);
        // } catch (InterruptedException e) {
        // System.out.println(e);
        // }
        System.out.println(sqrt(4d));
        // System.out.println(m[0]);
        // m.get("intercept")));
        // System.out.println(pow(10, 2));
        // System.out.println(round(10.00, 2));
        // System.out.println(10d);
        
        // System.out.println(topK(y, 2));
        // round(10.455, 2);
        // topK(y, 2);
    }

    public static BigDecimal[] getLinearRegression(double[] input_x, double[] input_y) throws InterruptedException {

        int size_x = input_x.length;
        int size_y = input_y.length;

        assert size_x == size_y : "The  size of data must be unanimous";

        double[][] X = new double[2][size_x];
        double[][] y = new double[][] {input_y};

        for(int i = 0; i < size_x; ++i) {
            X[0][i] = 1;
            X[1][i] = input_x[i];
        }

        Matrix data = new Matrix(X, 2, size_x).transpose();
        Matrix target = new Matrix(y, 1, size_y).transpose();

        Matrix beta = data.transpose().multiply(data).inverse().multiply(data.transpose()).multiply(target);
        // beta.print();
        // System.out.println(beta.matrix[0][0]);
        
        double[] results = beta.transpose().matrix[0];
        BigDecimal[] ret = {round(results[1], 2), round(results[0], 2)};

        return ret;
    }

    public static BigDecimal std(double[] x) {
        double avg, avg_sum_square, sum = 0, sum_square = 0;

        for (var i : x) {
            sum += i;
        }

        avg = sum / x.length;

        for (var i : x) {
            sum_square += pow(i - avg, 2);    
        }
        avg_sum_square = sum_square / (x.length - 1);
        BigDecimal ret  = round(sqrt(avg_sum_square), 2);
        return ret;
    }

    public static BigDecimal sma(double[] x, int l, int n) {
        double avg, sum = 0;
        for (int i = 0; i < n; i++) {
            sum += x[l + i];
        }
        avg = sum / n;
        BigDecimal ret = round(avg, 2);
        return ret;
    }

    public static List<BigDecimal> topK(ArrayList<BigDecimal> x, int k) {
        x.sort(Comparator.reverseOrder());
        return x.subList(0, k);
    }

    public static double pow(double x, int n) {
        double ans = 1;
        while (n != 0) {
            if (n % 2  == 1) {
                ans *= x;
            }
            x *= x;
            n /= 2;
        }
        return ans;
    }

    public static BigDecimal round(double a, int cnt) {
        BigDecimal bd = new BigDecimal(a).setScale(cnt, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd;
    }

    public static double sqrt(double x) {
        double x2 = x * 0.5f;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= 1.5f - x2 * x * x;
        x *= 1.5f - x2 * x * x;
        x = 1 / x;

        return x;
    }

}