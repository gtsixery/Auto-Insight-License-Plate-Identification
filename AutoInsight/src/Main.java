import javax.swing.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                runGui();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void runGui() throws IOException, ClassNotFoundException {
        OpenCv gui = new OpenCv();
        JFrame frame = new JFrame("AutoInsight");
        frame.setSize(850,850);
        frame.setContentPane(gui.getPanel());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}