
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class DataAnalysis {

    public static void main(String[] args) {
        double[] X = { 12d, 131d, 11d, 345d, 74d } ;
        double[] y = { 4d, 5d, 12d, 4d, 65d };
        double[] m = new double[2];
        try {
        m = getLinearRegression(X, y);
        } catch (InterruptedException e) {
        System.out.println(e);
        }
        System.out.println(m[0]);
        // m.get("intercept")));
        // System.out.println(pow(10, 2));
        // System.out.println(round(10.00, 2));
        // System.out.println(10d);
        
        // System.out.println(topK(y, 2));
        // round(10.455, 2);
    }

    public static double[] getLinearRegression(double[] input_x, double[] input_y) throws InterruptedException {

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
        double temp = round(results[0], 2);
        results[0] = round(results[1], 2);
        results[1] = temp;

        return results;
    }

    public static double std(double[] x) {
        double ans, avg, avg_sum_square, sum = 0, sum_square = 0;

        for (var i : x) {
            sum += i;
        }

        avg = sum / x.length;

        for (var i : x) {
            sum_square += pow(i - avg, 2);    
        }
        avg_sum_square = sum_square / (x.length - 1);
        ans = sqrt(avg_sum_square);
        ans = round(ans, 2);

        return ans;
    }

    public static double sma(double[] x, int l, int n) {
        double avg, sum = 0;
        for (int i = 0; i < n; i++) {
            sum += x[l + i];
        }
        avg = sum / n;
        avg = round(avg, 2);
        return avg;
    }

    public static List<Double> topK(double[] x, int k) {
        Set<Double> sortedset = new TreeSet<>(Comparator.reverseOrder());
        List<Double> temp = Arrays.stream(x).boxed().toList();
        sortedset.addAll(temp);
        return sortedset.stream().limit(k).collect(Collectors.toList());
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

    public static double round(double a, int cnt) {
        double n = pow(10, cnt + 1);
        int temp = (int) (a * n);
        int temp2 = temp / 10;
        double ans = temp2 / n * 10;
        double k = 0.01001d;
        if (temp % 10  >= 5 || temp % 10 <= -5) {
            if (a < 0) k *= -1;
            ans += k;
        }
        return ans;
    }

    public static double sqrt(double x) {
        double x2 = x * 0.5f;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= 1.5f - x2 * x * x;
        x *= 1.5f - x2 * x * x;
        x = round(1 / x, 2);
        return x;
    }

}