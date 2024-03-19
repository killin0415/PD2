package HW2;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class Parser {

    // private static final String match_attr = "(\\+|-)(\\w+) +(\\w+)";
    private static final String TEST = "classDiagram\n" + //
            "class Person \n" + //
            "    Person : +introduceSelf(String name) void\n" + //
            "\n" + //
            "class Student {\n" + //
            "    +String studentID\n" + //
            "    +study() void\n" + //
            "}\n" + //
            "    \n" + //
            "class Teacher {\n" + //
            "    +String teacherID\n" + //
            "    +teach() void\n" + //
            "}\n" + //
            "Person : -int age\n" + //
            "Person : -String name\n" + //
            "\n" + //
            "class Student {\n" + //
            "    int studClass;\n" + //
            "    Teacher correspondingTeacher;\n" + //
            "}";

    public static void main(String[] args) {
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m = getClass(TEST);
        m = getAttrWithColon(TEST, m);
        printMap(m);
    }

    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> getClass(String s) {

        final String MATCH_CLASS = "^class +(\\w+) +";
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>>();
        Matcher matcher = matchRegex(MATCH_CLASS, s);
        while (matcher.find()) {
            m.put(matcher.group(1), new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
        }
        return m;
    }

    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> getAttrWithColon(String s, HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {

        final String MATCH_ATTR_WITH_COLON = "(\\w+) *: *(\\+|-)(\\w+) +(\\w+)";
        Matcher matcher = matchRegex(MATCH_ATTR_WITH_COLON, s);
        while (matcher.find()) {
            String key = matcher.group(2) == "+" ? "public": "private"; 
            if (m.get(matcher.group(1)).get(key) == null){
                HashMap<String, ArrayList<String>> attribute = new HashMap<String, ArrayList<String>>();
                attribute.put(matcher.group(3), new ArrayList<String>(List.of(matcher.group(4))));
                HashMap<String, HashMap<String, ArrayList<String>>> attr = new HashMap<String, HashMap<String, ArrayList<String>>>();
                attr.put("attribute", attribute);
                m.get(matcher.group(1)).put(key, attr);
            } else {
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

    static void printMap(HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>> m) {
        for (var i : m.entrySet()) {
            System.out.println(i.getKey());
            for (var j : i.getValue().entrySet()) {
                System.out.println("  " + j.getKey());
                for (var k: j.getValue().entrySet()) {
                    System.err.println("    " + k.getKey());
                    for (var l: k.getValue().entrySet()) {
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
     *      |__ `private or public`
     *                  |__ `attribute` -- *
     *                  |                  |__ `type`: `variable name`
     *                  |__ `method` -- *
     *                                  |__ `type`: `function String needs to be parse`
     */
    

}