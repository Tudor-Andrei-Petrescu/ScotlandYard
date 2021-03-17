package uk.ac.bris.cs.scotlandyard.model;
import java.util.Comparator;

public class DetectiveLocation implements Comparator<Player> {
    public int compare(Player x, Player y){
        return x.location() -y.location();
    }
}
