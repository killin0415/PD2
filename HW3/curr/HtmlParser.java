package curr;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        } else if (args[1].equals("1")) {

            String name = args[2];
            int l = Integer.parseInt(args[3]);
            int r = Integer.parseInt(args[4]);

            ArrayList<ArrayList<String>> csv = parseCSV("output.csv");
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
                if (i + 5 > r) break;
                sma_list.add(format(DataAnalysis.sma(x, i, 5)));
            }
            ArrayList<String> title = new ArrayList<String>();

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
            std_list.add(format(DataAnalysis.std(x)));

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

            ArrayList<ArrayList<String>> csv = parseCSV("output.csv");
            ArrayList<ArrayList<String>> csv_data;
            try {
                csv_data = parseCSV("data.csv");
            } catch (IOException e) {
                csv_data = new ArrayList<>();
                System.err.println(e);
            }
            double[] std_list = new double[csv_data.get(0).size()];
            int i = 0;
            HashMap<Double, String> m = new HashMap<Double, String>();

            for (String name : csv_data.get(0)) {
                double[] x = dataProcessing(csv_data, l, r - l + 1, name);
                std_list[i] = DataAnalysis.std(x);
                m.put(std_list[i], name);
                i++;
            }
            List<Double> std_top3 = DataAnalysis.topK(std_list, 3);

            ArrayList<String> title = new ArrayList<String>(3);
            ArrayList<String> std_tok3_result = new ArrayList<String>(3);

            title.add(m.get(std_top3.get(0)));
            title.add(m.get(std_top3.get(1)));
            title.add(m.get(std_top3.get(2)));
            title.add(args[3]);
            title.add(args[4]);

            std_tok3_result.add(format(std_top3.get(0)));
            std_tok3_result.add(format(std_top3.get(1)));
            std_tok3_result.add(format(std_top3.get(2)));

            csv.add(title);
            csv.add(std_tok3_result);

            writeFile(csv, "output.csv");
        } else if (args[1].equals("4")) {

            String name = args[2];
            int l = Integer.parseInt(args[3]);
            int r = Integer.parseInt(args[4]);

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
            double[] ans = { 0, 0 };
            try {
                ans = DataAnalysis.getLinearRegression(x, y);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
            lr_list.add(format(ans[0]));
            lr_list.add(format(ans[1]));

            ArrayList<String> title = new ArrayList<String>(3);

            title.add(args[2]);
            title.add(args[3]);
            title.add(args[4]);

            csv.add(title);
            csv.add(lr_list);

            writeFile(csv, "output.csv");
        }
    }

    private static String format(double d) {
        String s = String.format("%.2f", d);
        if (s.contains(".")) {
            s = s.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
        return s;
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
        content.setLength(content.length() - 1);
        return content.toString();
    }

    private static void log(int s) {

        try {
            StringBuilder sb = new StringBuilder();
            File log = new File("logs.txt");
            log.createNewFile();
            sb.append(Files.readString(Paths.get("logs.txt")));
            sb.append(s + "\n");
            BufferedWriter bw = new BufferedWriter(new FileWriter(log));
            bw.write(sb.toString());
            bw.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    public static void writeFile(String path) {

        ArrayList<ArrayList<String>> csv = new ArrayList<>();
        File file = new File(path);
        
        try {

            file.createNewFile();

            csv = parseCSV(path);

            log(csv.size());

            Data data = grabData();
            if (csv.size() == 0) {
                csv.add(data.name);
                ArrayList<String> temp = new ArrayList<String>();
                temp.add("1");
		temp.add("1");
                for(int i = 1; i < 31; i++) {
                    csv.add(temp);
                }
            }
            csv.set(data.day, data.price);
            
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
        int day = 0;
        try {

            Document document = Jsoup.connect(url).get();
            String title = document.title();
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

            day = Integer.parseInt(title.replaceAll("day(\\d+)", "$1"));

        } catch (IOException e) {
            System.err.println(e);
        }

        Data ret = new Data(th, td, day);

        return ret;
    }
}

class Data {
    public ArrayList<String> name;
    public ArrayList<String> price;
    public int day;

    public Data(ArrayList<String> th, ArrayList<String> td, int day) {
        this.name = th;
        this.price = td;
        this.day = day;
    }
}
