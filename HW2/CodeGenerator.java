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
    public static void main(String[] args) {

        FileReader fr = new FileReader(args[0]);
        String input = fr.getCode();
        Parser parser = new Parser(input);
        for (var members : parser.members_map.entrySet()) {
            String class_name = members.getKey();
            String output = generate(members.getValue(), class_name);
            fr.writeFile(output.toString(), class_name);
        }
    }
    
    private static String generate(ArrayList<Member> members, String class_name){
        StringBuilder output = new StringBuilder();
        output.append(String.format("public class %s {\n", class_name));
        for (Member member : members) {
            output.append(formatMember(member));
        }
        output.append("}\n");
        return output.toString();
    }

    private static String formatMember(Member member) {
        StringBuilder output = new StringBuilder();
        if (member.is_method) {
            output.append(String.format("    %s %s %s {", member.is_private, member.type, member.name));
            if (member.name.startsWith("set")) {
                output.append(String.format("\n        this.%s = %s;\n    }\n",
                        toLowerCase(member.name.substring(3, member.name.indexOf("("))),
                        member.name.substring(member.name.indexOf(" ")+1, member.name.indexOf(")"))));
            } else if (member.name.startsWith("get")) {
                output.append(String.format("\n        return %s;\n    }\n",
                        toLowerCase(member.name.substring(3, member.name.indexOf("(")))));
            } else {
                output.append(String.format("%s;}\n", member.default_value));
            }
        } else {
            output.append(String.format("    %s %s %s;\n", member.is_private, member.type, member.name));
        }

        return output.toString();
    }

    private static String toLowerCase(String s) {
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
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
        this.type = this.type.replaceAll(" *(?=\\[)(\\[) *(?=\\])", "$1");
    }

    public Member(String is_private, String type, String name, boolean is_method) {
        this.is_private = is_private.equals("+") ? "public" : "private";
        this.type = type;
        this.name = name;
        this.is_method = true;
        switch (type) {
            case "int":
                this.default_value = "return 0";
                break;
            case "String":
                this.default_value = "return \"\"";
                break;
            case "boolean":
                this.default_value = "return false";
                break;
            default:
                this.default_value = "";
                break;
        }
        String variable = this.name.substring(this.name.indexOf('(')+1, this.name.indexOf(')'));
        String e = variable.replaceAll("^ +| +$| *(, ) *|(\\w+ ) +(\\w+)|( ) +", "$1$2$3$4");
        e = e.replaceAll(",(?! )", ", ");
        e = e.replaceAll(" *(?=\\[)(\\[) *(?=\\])", "$1");
        this.name = this.name.replace(variable, e);
        this.name = this.name.replaceAll(" +(?=\\()", "");
    }
}

class Parser {

    private final String MATCH_METHOD_AND_ATTRIBUTE = "((\\w+) *: *(\\+|-) *(\\w+\\[* *\\]*) +(\\w+))|((\\w+) *: *(\\+|-) *(\\w+ *\\(.*\\)) *(\\w+\\[*\\]*)*)";
    private final String MATCH_CLASS = " *class +(\\w+)";
    private final String CLEAR_BRACKET = " *class +(\\w+) *\\{([^\\}]*)\\}";

    private Pattern pattern;
    private Matcher matcher;

    public HashMap<String, ArrayList<Member>> members_map;

    public Parser(String input) {
        this.members_map = new HashMap<String, ArrayList<Member>>();
        matchRegex(preprocessing(input));
    }

    private String preprocessing(String s) {
        this.pattern = Pattern.compile(this.CLEAR_BRACKET, Pattern.MULTILINE);
        this.matcher = pattern.matcher(s);
        StringBuilder output = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String class_name = matcher.group(1);
            String line = matcher.group(2);
            pattern = Pattern.compile(" *(.+)\\n*", Pattern.MULTILINE);
            Matcher matcher2 = pattern.matcher(line);
            while (matcher2.find()) {
                output.append(String.format("    %s : %s\n", class_name, matcher2.group(1)));
            }
            matcher.appendReplacement(sb, output.toString()); //replace entire segment of bracket into colon format
            output.setLength(0);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void matchRegex(String target) {
        String class_name;
        this.pattern = Pattern.compile(this.MATCH_CLASS, Pattern.MULTILINE);
        this.matcher = pattern.matcher(target);
        while (matcher.find()) {
            if (this.members_map.get(matcher.group(1)) == null) {
                this.members_map.put(matcher.group(1), new ArrayList<Member>());
            }
        }
        this.pattern = Pattern.compile(this.MATCH_METHOD_AND_ATTRIBUTE, Pattern.MULTILINE);
        this.matcher = pattern.matcher(target);
        Member member;

        /*
         * `Teacher`(2) : `+`(3)`String`(4) `className`(5)
         * `Student`(7) : `+`(8)`getStudentName()`(9) `String`(10)
         */

        while (matcher.find()) {
            if (matcher.group(6) != null) {
                String type = matcher.group(10) != null ? matcher.group(10): "void";
                member = new Member(matcher.group(8), type,
                        matcher.group(9), true);
                class_name = matcher.group(7);
            } else {
                member = new Member(matcher.group(3),
                        matcher.group(4), matcher.group(5));
                class_name = matcher.group(2);
            }
            if (this.members_map.get(class_name) == null) {
                ArrayList<Member> members = new ArrayList<Member>();
                this.members_map.put(class_name, members);
            }
            ArrayList<Member> members = this.members_map.get(class_name);
            members.add(member);
        }
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
