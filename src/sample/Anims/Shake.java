package sample.Anims;

import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import javafx.scene.Node;

public class Shake {
    private TranslateTransition tt;

    public Shake(Node node){
        tt = new TranslateTransition(Duration.millis(70), node);
        tt.setFromX(0);
        //tt.setFromY(0);
        tt.setByX(20);
        //tt.setByY(10);
        tt.setCycleCount(5);
        tt.setAutoReverse(true);
    }

    public void play(){
        tt.playFromStart();
    }
}
