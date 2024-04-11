import static java.util.stream.Collectors.joining;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class hw1 {

    public static void main(String[] args) {
        final String str1 = args[1].toLowerCase();
        final String str2 = args[2].toLowerCase();
        final int s2Count = Integer.parseInt(args[3]); // string to int

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase(); // turn everything into lower case
                List<Boolean> ans = Arrays.asList(palindrome(line), findSubstring(line, str1, 1),
                        findSubstring(line, str2, s2Count), isContainSubstring(line));
                System.out.println(ans.stream().map((a) -> a ? "Y" : "N").collect(joining(",")));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean palindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) // check if the letter is same by two pointer
                return false;
            left++;
            right--;
        }
        return true;
    }

    private static boolean findSubstring(String s, String target, int count) {
        for (int i = 0; i < s.length() - target.length() + 1; i++) {
            for (int j = 0; j < target.length(); j++) {
                if (s.charAt(i + j) != target.charAt(j))
                    break;
                if (j == target.length() - 1)
                    count--;
            }
        }
        return count <= 0 ? true : false;
    }

    private static boolean isContainSubstring(String s) {
        for (int i = 0; i < s.length() - 2; i++) {
            if (s.charAt(i) == 'a') {
                for (int j = i + 1; j < s.length() - 1; j++) {
                    if (s.charAt(j) == 'b' && s.charAt(j + 1) == 'b')
                        return true;
                }
                return false;
            }
        }
        return false;
    }
}

