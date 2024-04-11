import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlParser {

    final private static String url = "https://pd2-hw3.netdb.csie.ncku.edu.tw/";

    public static void main(String[] args) {

        if (args[0].equals("0")) {
            writeFile("data.csv");
        } else if (args[1].equals("0")) {
            writeFile("output.csv");
        } else if (args[1].equals("1")) {

            String name = args[2];
            int l = Integer.parseInt(args[3]);
            int r = Integer.parseInt(args[4]);

            ArrayList<ArrayList<String>> csv = new ArrayList<>();
            ArrayList<String> sma_list = new ArrayList<String>();
            ArrayList<ArrayList<String>> csv_data;
            try {
                csv_data = parseCSV("data.csv");
            } catch (IOException e) {
                csv_data = new ArrayList<>();
                System.err.println(e);
            }
            double[] x = dataProcessing(csv_data, 1, 30, name);
            for (int i = l - 1; i < r; ++i) {
                sma_list.add(String.valueOf(DataAnalysis.sma(x, i, 5)));
            }
            ArrayList<String> title = new ArrayList<String>(3);

            title.add(args[2]);
            title.add(args[3]);
            title.add(args[4]);

            csv.add(title);
            csv.add(sma_list);

            writeFile(csv, "output.csv");
        } else if (args[1].equals("2")) {

            String name = args[2];
            int l = Integer.parseInt(args[3]);
            int r = Integer.parseInt(args[4]);

            ArrayList<ArrayList<String>> csv = new ArrayList<>();
            ArrayList<String> std_list = new ArrayList<String>();
            ArrayList<ArrayList<String>> csv_data;
            try {
                csv_data = parseCSV("data.csv");
            } catch (IOException e) {
                csv_data = new ArrayList<>();
                System.err.println(e);
            }
            double[] x = dataProcessing(csv_data, 1, r - l + 1, name);
            std_list.add(String.valueOf(DataAnalysis.std(x)));

            ArrayList<String> title = new ArrayList<String>(3);

            title.add(args[2]);
            title.add(args[3]);
            title.add(args[4]);

            csv.add(title);
            csv.add(std_list);

            writeFile(csv, "output.csv");
        } else if (args[1].equals("3")) {

            int l = Integer.parseInt(args[3]);
            int r = Integer.parseInt(args[4]);

            ArrayList<ArrayList<String>> csv = new ArrayList<>();
            ArrayList<ArrayList<String>> csv_data;
            try {
                csv_data = parseCSV("data.csv");
            } catch (IOException e) {
                csv_data = new ArrayList<>();
                System.err.println(e);
            }
            double[] std_list = new double[csv_data.get(0).size()];
            int i = 0;

            for (String name : csv_data.get(0)) {
                double[] x = dataProcessing(csv_data, l, l - r + 1, name);
                std_list[i] = DataAnalysis.std(x);
                i++;
            }
            List<Double> std_top3 = DataAnalysis.topK(std_list, 3);

            ArrayList<String> title = new ArrayList<String>(3);
            ArrayList<String> std_tok3_result = new ArrayList<String>(3);

            title.add(args[2]);
            title.add(args[3]);
            title.add(args[4]);

            std_tok3_result.add(String.valueOf(std_top3.get(0)));
            std_tok3_result.add(String.valueOf(std_top3.get(1)));
            std_tok3_result.add(String.valueOf(std_top3.get(2)));

            csv.add(title);
            csv.add(std_tok3_result);

            writeFile(csv, "output.csv");
        } else if (args[1].equals("4")) {

            String name = args[2];
            int l = Integer.parseInt(args[3]);
            int r = Integer.parseInt(args[4]);

            ArrayList<ArrayList<String>> csv = new ArrayList<>();
            ArrayList<String> lr_list = new ArrayList<String>();
            ArrayList<ArrayList<String>> csv_data;
            try {
                csv_data = parseCSV("data.csv");
            } catch (IOException e) {
                csv_data = new ArrayList<>();
                System.err.println(e);
            }
            double[] x = new double[r - l + 1];
            for (int i = 0; i < r - l + 1; ++i)
                x[i] = l + i;
            double[] y = dataProcessing(csv_data, l, r - l + 1, name);
            double[] ans = { 0, 0 };
            for (int i = l - 1; i < r; ++i) {
                try {
                    ans = DataAnalysis.getLinearRegression(x, y);
                } catch (InterruptedException e) {
                    System.err.println(e);
                    ;
                }
                lr_list.add(String.valueOf(ans[0]));
                lr_list.add(String.valueOf(ans[1]));
            }
            ArrayList<String> title = new ArrayList<String>(3);

            title.add(args[2]);
            title.add(args[3]);
            title.add(args[4]);

            csv.add(title);
            csv.add(lr_list);

            writeFile(csv, "output.csv");
        }

    }

    private static double[] dataProcessing(ArrayList<ArrayList<String>> arr, int l, int n, String name) {

        double[] ret = new double[n];
        int index = arr.get(0).indexOf(name);

        for (int i = 0; i < n; ++i) {
            ret[i] = Double.parseDouble(arr.get(l + i).get(index));
        }

        return ret;
    }

    private static ArrayList<ArrayList<String>> parseCSV(String path) throws IOException {
        File getCSVFiles = new File(path);
        getCSVFiles.createNewFile();
        Scanner sc = new Scanner(getCSVFiles);
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        sc.useDelimiter("\n");
        while (sc.hasNext()) {
            ret.add(new ArrayList<String>(Arrays.asList(sc.next().split(","))));
        }
        sc.close();
        return ret;
    }

    private static String formatCSV(ArrayList<ArrayList<String>> csv) {
        StringBuilder content = new StringBuilder();
        for (var i : csv) {
            for (int j = 0; j < i.size(); ++j) {
                content.append(i.get(j));
                content.append(j != i.size() - 1 ? "," : "\n");
            }
        }
        return content.toString();
    }

    public static void writeFile(String path) {

        ArrayList<ArrayList<String>> csv = new ArrayList<>();
        File file = new File(path);

        try {
            csv = parseCSV(path);
            Data data = grabData();

            if (csv.size() == 0) {
                csv.add(data.name);
            }
            csv.add(data.price);
            String content = formatCSV(csv);

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void writeFile(ArrayList<ArrayList<String>> csv, String path) {

        File file = new File(path);
        try {
            String content = formatCSV(csv);

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static Data grabData() {

        ArrayList<String> th = new ArrayList<String>();
        ArrayList<String> td = new ArrayList<String>();

        try {

            Document document = Jsoup.connect(url).get();
            Elements elements = document.body().select("table > tbody > tr");

            for (Element element : elements) {
                Elements tElements = element.select("th, td");
                for (Element e : tElements) {
                    if (e.tagName().equals("th"))
                        th.add(e.text());
                    else
                        td.add(e.text());
                }
            }
            assert th.size() == td.size() : "data sizes are different";

        } catch (IOException e) {
            System.err.println(e);
        }

        Data ret = new Data(th, td);

        return ret;
    }
}

class Data {
    public ArrayList<String> name;
    public ArrayList<String> price;

    public Data(ArrayList<String> th, ArrayList<String> td) {
        this.name = th;
        this.price = td;
    }
}
