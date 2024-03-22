import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeGenerator {

    static private final String REGEX = "((\\w+) *: *(\\+|-)(\\w+) +(\\w+))|((\\w+) *: *(\\+|-)(\\w+\\(.*\\)) +(\\w+))";

    public static void main(String[] args) {

        StringBuilder output = new StringBuilder();
        FileReader fr = new FileReader(args[0]);
        String input = fr.getCode();
        input = Parser.preprocessing(input);
        final String PATTEN = "get(\\w+)|set(\\w+)";
        HashMap<String, ArrayList<Member>> members_map = Parser.matchRegex(REGEX, input);
        for (var members : members_map.entrySet()) {
            String class_name = members.getKey();
            output.append(String.format("public class %s {\n", class_name));
            for (Member member : members.getValue()) {
                if (member.is_method) {
                    Pattern pattern = Pattern.compile(PATTEN, Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(member.name);
                    if (matcher.find()) {
                        output.append(String.format("    %s %s %s {\n", member.is_private,
                                member.type, member.name));
                        if (matcher.group(1) != null) { // getter
                            output.append(
                                    String.format("        return %s;\n",
                                            matcher.group(1).toLowerCase()));
                        } else if (matcher.group(2) != null) { // setter
                            output.append(
                                    String.format("        this.%s = %s;\n",
                                            matcher.group(2).toLowerCase(),
                                            matcher.group(2).toLowerCase()));
                        }
                        output.append("    }\n");
                    } else {
                        if (member.type.equals("void")) {
                            output.append(
                                    String.format("    %s %s %s {;}\n",
                                            member.is_private,
                                            member.type, member.name));
                        } else {
                            output.append(
                                    String.format("    %s %s %s {return %s;}\n",
                                            member.is_private, member.type,
                                            member.name, member.default_value));
                        }
                    }
                } else {
                    output.append(String.format("    %s %s %s;\n", member.is_private, 
                                                            member.type, member.name));
                }
            }
            output.append("}");
            fr.writeFile(output.toString(), class_name);
            output.setLength(0);
        }
    }
}

class Member {
    public String type;
    public String name;
    public boolean is_method;
    public String is_private;
    public String default_value;

    public Member(String is_private, String type, String name) {
        this.is_private = is_private.equals("+") ? "public" : "private";
        this.type = type;
        this.name = name;
        this.is_method = false;
    }

    public Member(String is_private, String type, String name, boolean is_method) {
        this.is_private = is_private.equals("+") ? "public" : "private";
        this.type = type;
        this.name = name;
        this.is_method = true;
        switch (type) {
            case "int":
                this.default_value = "0";
                break;
            case "String":
                this.default_value = "\"\"";
                break;
            case "boolean":
                this.default_value = "false";
                break;
            default:
                this.default_value = "";
                break;
        }
    }
}

class Parser {

    static String preprocessing(String s) {
        final String find_bracket = " *class +(\\w+) +\\{\\n([^\\}]*)\\}";
        Pattern pattern = Pattern.compile(find_bracket, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(s);
        StringBuilder output = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String class_name = matcher.group(1);
            String line = matcher.group(2);
            pattern = Pattern.compile(" *(.+)\\n");
            Matcher matcher2 = pattern.matcher(line);
            while (matcher2.find()) {
                output.append(String.format("    %s : %s\n", class_name, matcher2.group(1)));
            }
            matcher.appendReplacement(sb, output.toString());
            output.setLength(0);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    static HashMap<String, ArrayList<Member>> matchRegex(String format, String target) {
        HashMap<String, ArrayList<Member>> members_map = new HashMap<String, ArrayList<Member>>();
        
        String class_name;
        Pattern pattern = Pattern.compile(" *class +(\\w+)");
        Matcher match_class = pattern.matcher(target);
        while(match_class.find()) {
            if (members_map.get(match_class.group(1)) == null) {
                members_map.put(match_class.group(1), new ArrayList<Member>());
            }
        }
        pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        Member member;

        while (matcher.find()) {
            if (matcher.group(6) != null) {
                member = new Member(matcher.group(8), matcher.group(10),
                        matcher.group(9), true);
                class_name = matcher.group(7);
            } else {
                member = new Member(matcher.group(3),
                        matcher.group(4), matcher.group(5));
                class_name = matcher.group(2);
            }
            if (members_map.get(class_name) == null) {
                ArrayList<Member> members = new ArrayList<Member>();
                members_map.put(class_name, members);
            }
            ArrayList<Member> members = members_map.get(class_name);
            members.add(member);
            System.out.println(member.name);
            System.out.println(member.is_private);
        }

        return members_map;
    }
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