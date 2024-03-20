package HW2;

import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class Parser {

    // private static final String match_attr = "(\\+|-)(\\w+) +(\\w+)";

    public static void main(String[] args) {

        FileReader fr = new FileReader(args[0]);
        String TEST = fr.getCode();
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m = getClass(TEST);
        m = getAttrWithColon(TEST, m);
        m = getMethodWithColon(TEST, m);
        m = getMemberWithBrackets(TEST, m);
        // printMap(m);
        CodeGenerator cg  = new CodeGenerator(m);
        for (var i: m.entrySet()) {
            String output = cg.codeGenerate(i.getKey());
            fr.writeFile(output, i.getKey());
        }
    }

    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> getClass(String s) {

        final String MATCH_CLASS = "class +(\\w+) *";
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>>();
        Matcher matcher = matchRegex(MATCH_CLASS, s);
        while (matcher.find()) {
            m.put(matcher.group(1), new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
        }
        return m;
    }

    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> getAttrWithColon(
            String s, HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {

        final String MATCH_ATTR_WITH_COLON = "(\\w+) *: *(\\+|-)(\\w+) +(\\w+)";
        Matcher matcher = matchRegex(MATCH_ATTR_WITH_COLON, s);
        while (matcher.find()) {
            String key = matcher.group(2).equals("+") ? "public" : "private";
            if (m.get(matcher.group(1)).get(key) == null) {
                HashMap<String, ArrayList<String>> attribute = new HashMap<String, ArrayList<String>>();
                attribute.put(matcher.group(3), new ArrayList<String>(List.of(matcher.group(4))));
                HashMap<String, HashMap<String, ArrayList<String>>> attr = new HashMap<String, HashMap<String, ArrayList<String>>>();
                attr.put("attribute", attribute);
                m.get(matcher.group(1)).put(key, attr);
            } else {
                if (m.get(matcher.group(1)).get(key).get("attribute") == null)
                    m.get(matcher.group(1)).get(key).put("attribute", new HashMap<String, ArrayList<String>>());
                HashMap<String, ArrayList<String>> attribute = m.get(matcher.group(1)).get(key).get("attribute");
                if (attribute.get(matcher.group(3)) == null) {
                    attribute.put(matcher.group(3), new ArrayList<String>(List.of(matcher.group(4))));
                } else {
                    attribute.get(matcher.group(3)).add(matcher.group(4));
                }
            }
        }
        return m;
    }

    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> getMethodWithColon(
            String s, HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {
        final String MATCH_METHOD_WITH_COLON = "(\\w+) *: *(\\+|-)((\\w+)\\((.*)\\)) +(\\w+)";
        Matcher matcher = matchRegex(MATCH_METHOD_WITH_COLON, s);
        while (matcher.find()) {
            String key = matcher.group(2).equals("+") ? "public" : "private";
            if (m.get(matcher.group(1)).get(key) == null) {
                HashMap<String, ArrayList<String>> method = new HashMap<String, ArrayList<String>>();
                method.put(matcher.group(6), new ArrayList<String>(List.of(matcher.group(3))));
                HashMap<String, HashMap<String, ArrayList<String>>> meth = new HashMap<String, HashMap<String, ArrayList<String>>>();
                meth.put("method", method);
                m.get(matcher.group(1)).put(key, meth);
            } else {
                if (m.get(matcher.group(1)).get(key).get("method") == null)
                    m.get(matcher.group(1)).get(key).put("method", new HashMap<String, ArrayList<String>>());
                HashMap<String, ArrayList<String>> method = m.get(matcher.group(1)).get(key).get("method");
                if (method.get(matcher.group(6)) == null) {
                    method.put(matcher.group(6), new ArrayList<String>(List.of(matcher.group(3))));
                } else {
                    method.get(matcher.group(6)).add(matcher.group(3));
                }
            }
        }
        return m;
    }

    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> getMemberWithBrackets(
            String s, HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {

        final String BRACKETS = "class +(\\w+) *\\{([^\\{]*)\\}";
        Matcher matcher2 = matchRegex(BRACKETS, s);
        while (matcher2.find()) {
            final String class_name = matcher2.group(1);
            final String member = matcher2.group(2);
            final String MATCH_MEMBER = "(\\+|-)((\\w+)\\((.*)\\)) +(\\w+)|(\\+|-)(\\w+) +(\\w+)";
            Matcher matcher = matchRegex(MATCH_MEMBER, member);
            while (matcher.find()) {
                if (matcher.group(2) != null) { // method
                    String key = matcher.group(1).equals("+") ? "public" : "private";
                    if (m.get(class_name).get(key) == null) {
                        HashMap<String, ArrayList<String>> method = new HashMap<String, ArrayList<String>>();
                        method.put(matcher.group(5), new ArrayList<String>(List.of(matcher.group(2))));
                        HashMap<String, HashMap<String, ArrayList<String>>> meth = new HashMap<String, HashMap<String, ArrayList<String>>>();
                        meth.put("method", method);
                        m.get(class_name).put(key, meth);
                    } else {
                        if (m.get(class_name).get(key).get("method") == null)
                            m.get(class_name).get(key).put("method", new HashMap<String, ArrayList<String>>());
                        HashMap<String, ArrayList<String>> method = m.get(class_name).get(key).get("method");
                        if (method.get(matcher.group(5)) == null) {
                            method.put(matcher.group(5), new ArrayList<String>(List.of(matcher.group(2))));
                        } else {
                            method.get(matcher.group(5)).add(matcher.group(2));
                        }
                    }
                } else {
                    String key = matcher.group(6).equals("+") ? "public" : "private";
                    if (m.get(class_name).get(key) == null) {
                        HashMap<String, ArrayList<String>> attribute = new HashMap<String, ArrayList<String>>();
                        attribute.put(matcher.group(7), new ArrayList<String>(List.of(matcher.group(8))));
                        HashMap<String, HashMap<String, ArrayList<String>>> attr = new HashMap<String, HashMap<String, ArrayList<String>>>();
                        attr.put("attribute", attribute);
                        m.get(class_name).put(key, attr);
                    } else {
                        if (m.get(class_name).get(key).get("attribute") == null)
                            m.get(class_name).get(key).put("attribute", new HashMap<String, ArrayList<String>>());
                        HashMap<String, ArrayList<String>> attribute = m.get(class_name).get(key).get("attribute");
                        if (attribute.get(matcher.group(7)) == null) {
                            attribute.put(matcher.group(7), new ArrayList<String>(List.of(matcher.group(8))));
                        } else {
                            attribute.get(matcher.group(7)).add(matcher.group(8));
                        }
                    }
                }
            }

        }
        return m;
    }

    static void printMap(HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {
        for (var i : m.entrySet()) {
            System.out.println(i.getKey());
            for (var j : i.getValue().entrySet()) {
                System.out.println("  " + j.getKey());
                for (var k : j.getValue().entrySet()) {
                    System.err.println("    " + k.getKey());
                    for (var l : k.getValue().entrySet()) {
                        System.out.println("      " + l.getKey() + ": " + l.getValue());
                    }
                }
            }
        }
    }

    static void printKey(HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {
        for (var i : m.entrySet()) {
            System.out.println(i.getKey());
        }
    }

    static Matcher matchRegex(String format, String target) {
        Pattern pattern = Pattern.compile(format, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(target);
        return matcher;
    }

    /*
     * define a class call Token to store all the messages from parser
     * the structure be like:
     * |__`class name`
     * |__ `private or public`
     * |__ `attribute` -- *
     * | |__ `type`: `variable name`
     * |__ `method` -- *
     * |__ `type`: `function String needs to be parse`
     */

}

class FileReader {
    private String path;
    private String mermaid_code;

    public FileReader(String path) {
        this.path = path;
        try {
            this.mermaid_code = Files.readString(Paths.get(this.path));
        } catch (IOException e) {
            System.err.println("無法讀取文件 " + this.path);
            e.printStackTrace();
        }
    }

    public String getCode() {
        return this.mermaid_code;
    }

    public void writeFile(String content, String class_name) {
        try {
            String output = String.format("%s.java", class_name);
            File file = new File(output);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class CodeGenerator {
    private HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> data;
    private HashMap<String, String> default_value = new HashMap<String, String>();

    public CodeGenerator(HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {
        this.data = m;
        default_value.put("int", "0");
        default_value.put("boolean", "false");
        default_value.put("String", "\"\"");
    }

    public String codeGenerate(String class_name) {
        StringBuilder output = new StringBuilder();
        HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> i = this.data.get(class_name);
        output.append(String.format("public class %s {\n", class_name));
        for (var j : i.entrySet()) { // private or public
            for (var k : j.getValue().entrySet()) { // attribute or method
                if (k.getKey() == "attribute") {
                    for (var l : k.getValue().entrySet()) { // type
                        l.getValue().forEach((e) -> { // variable
                            output.append(String.format("    %s %s %s;\n", j.getKey(), l.getKey(), e));
                        });
                    }
                } else {
                    final String PATTEN = "get(\\w+)|set(\\w+)";
                    Pattern pattern = Pattern.compile(PATTEN, Pattern.MULTILINE);
                    for (var l : k.getValue().entrySet()) {
                        l.getValue().forEach((e) -> {
                            Matcher matcher = pattern.matcher(e);
                            if (matcher.find()) {
                                output.append(String.format("    %s %s %s {\n", j.getKey(), l.getKey(), e));
                                if (matcher.group(1) != null) { // getter
                                    output.append(
                                            String.format("        return %s;\n",
                                                    matcher.group(1).toLowerCase()));
                                } else if (matcher.group(2) != null) {
                                    output.append(
                                            String.format("        this.%s = %s;\n",
                                                    matcher.group(2).toLowerCase(),
                                                    matcher.group(2).toLowerCase()));
                                }
                                output.append("    }\n");
                            } else {
                                if (l.getKey().equals("void")) {
                                    output.append(
                                        String.format("    %s %s %s {;}\n", 
                                            j.getKey(), l.getKey(), e));
                                } else {
                                    output.append(
                                        String.format("    %s %s %s {return %s;}\n",
                                            j.getKey(),l.getKey(), 
                                            e, default_value.get(l.getKey())));
                                }
                            }
                        });
                    }
                }

            }
        }
        output.append("}\n");
        return output.toString();
    }

}