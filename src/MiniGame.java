import ea.*;

/**
 * Kleines Demo-Spielprojekt.
 *
 * Created by Michael on 11.04.2017.
 */
public class MiniGame
extends Game{

    /* STATIC STUFF */

    /**
     * Main-Methode startet ein Spiel
     * @param args Command-Line Argumente. Nicht relevant.
     */
    public static void main(String[] args) {
        new MiniGame();
    }

    private static final int BREITE=1080, HOEHE=720;


    /**
     * Steuerbare Spielfigur
     */
    private Figur spielfigur;

    public MiniGame() {
        super(BREITE, HOEHE, "EA Testgame");
    }

    @Override
    public void initialisieren() {
        spielfigur = new Figur(0,0,"ship.eaf");
        wurzel.add(spielfigur);
        spielfigur.faktorSetzen(10);
    }

    @Override
    public void tasteReagieren(int code) {
        switch(code) {
            case Taste.O:
                System.out.println("Pos = " + spielfigur.position.get());
                break;
        }
    }

    @Override
    public void frameUpdate(float ts) {
        if(tasteGedrueckt(Taste.RECHTS)) {
            spielfigur.position.verschieben(0.2f, 0);
        }
        if(tasteGedrueckt(Taste.LINKS)) {
            spielfigur.position.verschieben(-0.2f, 0);
        }
    }
}
