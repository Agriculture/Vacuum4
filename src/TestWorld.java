
import java.awt.Point;
import vacuumcleaner.environments.ObstacleRectangleEnvironment;
import vacuumcleaner.environments.RectangleEnvironment;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author konrad
 */
public class TestWorld extends ObstacleRectangleEnvironment {

    public TestWorld() {
        super( 20, 1);
        for(int i=0; i<20; i++){
            super.cleanDirt(i, 0);
        }
        super.putDirt(19, 0);

        super.setInitialAgentLocation(new Point(0,0));
        super.initAgentHome(new Point(0, 0));
    }

    @Override
    public boolean isRoom(int x, int y) {
        if(x==17 && y==0){
            return false;
        } else {
            return true;
        }
    }

}
