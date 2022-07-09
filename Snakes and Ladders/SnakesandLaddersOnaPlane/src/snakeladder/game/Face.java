package snakeladder.game;

import ch.aplu.jgamegrid.Actor;

public class Face extends Actor {
    int nb;
    private NavigationPane np;
    public Face(NavigationPane np,int nb) {
        super("sprites/pips" + nb + ".gif", 7);
        this.nb = nb;
        this.np = np;
    }

    public void act()
    {
        showNextSprite();
        if (getIdVisible() == 6)
        {
            setActEnabled(false);
            np.finishRolling(nb);
        }
    }
}
