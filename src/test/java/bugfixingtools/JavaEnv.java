package bugfixingtools;

import java.util.Map;

public class JavaEnv {
    public static void main(String[] args) {
        for (Map.Entry<String, String> set : System.getenv().entrySet()) {
            if(set.getKey().contains("JAVA_O")) {
                System.out.println(set.getKey() + " : " + set.getValue());
            }
        }
    }
}
