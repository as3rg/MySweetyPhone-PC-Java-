package sample.Anims;

import javafx.animation.ScaleTransition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class Destroy {
    private ScaleTransition tt;

    public Destroy(Node node) {
        tt = new ScaleTransition(Duration.seconds(0.25), node);
        tt.setToX(0);
        tt.setToY(0);

    }

    public void play(EventHandler e) {
        tt.setOnFinished(e);
        tt.playFromStart();

    }
}
