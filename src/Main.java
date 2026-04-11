import javax.swing.SwingUtilities;
import app.AppController;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                AppController controller = new AppController();
                controller.start();

            }
        });
    }
}