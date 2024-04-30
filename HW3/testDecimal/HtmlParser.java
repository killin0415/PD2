package testDecimal;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlParser {

    final private static String url = "https://pd2-hw3.netdb.csie.ncku.edu.tw/";

    public static void main(String[] args) throws IOException {

        if (args[0].equals("0")) {
            writeFile("data.csv");
        } else if (args[1].equals("0")) {
            ArrayList<ArrayList<String>> a = parseCSV("data.csv");
            writeFile(a, "output.csv");
        } else {
            String name = args[2];
            int l = Integer.parseInt(args[3]), r = Integer.parseInt(args[4]);
            ArrayList<ArrayList<String>> csv = new ArrayList<>();

            switch (args[1]) {
                case "1":
                    csv = task1(name, l, r);
                    break;
                case "2":
                    csv = task2(name, l, r);
                    break;
                case "3":
                    csv = task3(l, r);
                    break;
                case "4":
                    csv = task4(name, l, r);
                    break;

                default:
                    break;
            }

            writeFile(csv, "output.csv");

        }
    }

    private static ArrayList<ArrayList<String>> task1(String name, int l, int r) throws IOException {

        ArrayList<ArrayList<String>> csv = parseCSV("output.csv");
        ArrayList<String> sma_list = new ArrayList<String>();
        ArrayList<ArrayList<String>> csv_data;

        csv_data = parseCSV("data.csv");
        double[] x = dataProcessing(csv_data, 1, 30, name);
        for (int i = l - 1; i < r; ++i) {
            if (i + 5 > r)
                break;
            sma_list.add(DataAnalysis.sma(x, i, 5).toPlainString());
        }
        ArrayList<String> title = new ArrayList<String>();

        title.add(name);
        title.add(String.valueOf(l));
        title.add(String.valueOf(r));

        csv.add(title);
        csv.add(sma_list);

        return csv;
    }

    private static ArrayList<ArrayList<String>> task2(String name, int l, int r) throws IOException {

        ArrayList<ArrayList<String>> csv = parseCSV("output.csv");
        ArrayList<String> std_list = new ArrayList<String>();
        ArrayList<ArrayList<String>> csv_data;
        try {
            csv_data = parseCSV("data.csv");
        } catch (IOException e) {
            csv_data = new ArrayList<>();
            System.err.println(e);
        }
        double[] x = dataProcessing(csv_data, l, r - l + 1, name);
        std_list.add(DataAnalysis.std(x).toPlainString());

        ArrayList<String> title = new ArrayList<String>();

        title.add(name);
        title.add(String.valueOf(l));
        title.add(String.valueOf(r));

        csv.add(title);
        csv.add(std_list);

        return csv;
    }

    private static ArrayList<ArrayList<String>> task3(int l, int r) throws IOException {

        ArrayList<ArrayList<String>> csv = parseCSV("output.csv");
        ArrayList<ArrayList<String>> csv_data;
        try {
            csv_data = parseCSV("data.csv");
        } catch (IOException e) {
            csv_data = new ArrayList<>();
            System.err.println(e);
        }
        ArrayList<BigDecimal> std_list = new ArrayList<>();
        int i = 0;
        HashMap<BigDecimal, String> m = new HashMap<BigDecimal, String>();

        for (String name : csv_data.get(0)) {
            double[] x = dataProcessing(csv_data, l, r - l + 1, name);
            std_list.add(DataAnalysis.std(x));
            m.put(std_list.get(i), name);
            i++;
        }
        List<BigDecimal> std_top3 = DataAnalysis.topK(std_list, 3);

        ArrayList<String> title = new ArrayList<String>(3);
        ArrayList<String> std_tok3_result = new ArrayList<String>(3);

        title.add(m.get(std_top3.get(0)));
        title.add(m.get(std_top3.get(1)));
        title.add(m.get(std_top3.get(2)));
        title.add(String.valueOf(l));
        title.add(String.valueOf(r));

        std_tok3_result.add(std_top3.get(0).toPlainString());
        std_tok3_result.add(std_top3.get(1).toPlainString());
        std_tok3_result.add(std_top3.get(2).toPlainString());

        csv.add(title);
        csv.add(std_tok3_result);

        return csv;
    }

    private static ArrayList<ArrayList<String>> task4(String name, int l, int r) throws IOException {

        ArrayList<ArrayList<String>> csv = parseCSV("output.csv");
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
        BigDecimal[] ans = new BigDecimal[2];

        try {
            ans = DataAnalysis.getLinearRegression(x, y);
        } catch (InterruptedException e) {
            System.err.println(e);
            ;
        }
        lr_list.add(ans[0].toPlainString());
        lr_list.add(ans[1].toPlainString());

        ArrayList<String> title = new ArrayList<String>(3);

        title.add(name);
        title.add(String.valueOf(l));
        title.add(String.valueOf(r));

        csv.add(title);
        csv.add(lr_list);

        return csv;
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
            content.append(String.join(",", i));
            content.append("\n");
        }
        return content.toString();
    }

    public static void writeFile(String path) throws IOException {

        ArrayList<ArrayList<String>> csv = new ArrayList<>();
        File file = new File(path);
        file.createNewFile();

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
    }

    public static void writeFile(ArrayList<ArrayList<String>> csv, String path) throws IOException {

        File file = new File(path);
        file.createNewFile();
        String content = formatCSV(csv);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();
    }

    public static Data grabData() throws IOException {

        ArrayList<String> th = new ArrayList<String>();
        ArrayList<String> td = new ArrayList<String>();

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
