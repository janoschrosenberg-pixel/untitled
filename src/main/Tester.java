import stackmachine.StackUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Tester {
    public static void main(String[] args) {
      var tokens =   StackUtils.tokenize(": menu_up \"up\" SEND_MENU_COMMAND").toArray(new String[0]);

        System.out.println(String.join("\n", tokens));

    }
}
