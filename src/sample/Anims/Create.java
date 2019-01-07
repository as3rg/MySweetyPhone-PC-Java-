package sample.Anims;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class Create {
    private ScaleTransition tt;

    public Create(Node node) {
        tt = new ScaleTransition(Duration.seconds(0.25), node);
        tt.setFromX(0);
        tt.setFromY(0);
        tt.setToX(node.getScaleX());
        tt.setToY(node.getScaleY());
    }

    public void play(){
        tt.playFromStart();
    }
}
