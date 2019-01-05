package sample.Anims;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class Destroy {
    private ScaleTransition tt;

    public Destroy(Node node, Pane pane) {
        tt = new ScaleTransition(Duration.seconds(0.25), node);
        tt.setToX(0);
        tt.setToY(0);
        tt.setOnFinished(event -> {
            pane.getChildren().remove(node);
        });
    }

    public void play(){
        tt.playFromStart();
    }
}
