import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;

public class JKLMHelper implements NativeKeyListener {
    static ArrayList<String> words = new ArrayList<String>();
    static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    static Robot robot = null;
    {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F8) {
            selectAndCopyToClipboard();
            String answer = getJKLMAnswer(getClipboardText().toLowerCase());
            System.out.println(answer);
            words.remove(answer);

            try {
                typeString(answer);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public String getClipboardText() {
        String result = "";
        try {
            result = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getJKLMAnswer(String subString) {
        String result = "";
        for (String word : words) {
            if (word.contains(subString)) {
                result = word;
                break;
            }
        }

        return result;
    }

    public void typeString(String str) throws Exception {
        String upperCaseStr = str.toUpperCase();

        for (int i = 0; i < upperCaseStr.length(); i++) {
            String letter = Character.toString(upperCaseStr.charAt(i));
            String code = "VK_" + letter;

            Field f = KeyEvent.class.getField(code);
            int keyCode = f.getInt(null);

            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
    }

    public void selectAndCopyToClipboard() {
        try {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);
            robot.keyPress(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String joinToPath(String[] path) {
        if (System.getProperty("os.name").contains("Win")) {
            return String.join("\\", path);
        } else {
            return String.join("/", path);
        }
    }

    public static void main(String[] args) throws Exception {
        String absolutePath = Paths.get("").toAbsolutePath().toString();

        BufferedReader br = new BufferedReader(
                new FileReader(joinToPath(new String[] { absolutePath, "src", "assets", "sowpods.txt" })));

        while (true) {
            String word = br.readLine();

            if (word == null) {
                break;
            }

            words.add(word);
        }

        br.close();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new JKLMHelper());
    }
}