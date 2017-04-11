import ea.*;

/**
 * Eine kleine Demo zum Verhalten vieler partikelähnlicher Physik-Objekte in der Engine.
 *
 * Created by Michael on 11.04.2017.
 */
public class MarbleDemo
extends Game {

    /**
     * Konstanten zur Beschreibung der Position des Trichters.
     */
    private static final int ABSTAND_OBEN=300, ABSTAND_LINKS=40, ABSTAND_RECHTS=470;

    /**
     * Der Boden des Trichters. Kann durchlässig gemacht werden.
     */
    private Rechteck boden;

    public static void main(String[] args) {
        new MarbleDemo();
    }

    public MarbleDemo(){
        super(512, 1024, "Engine Alpha - Marble Demo");

    }


    @Override
    public void initialisieren() {

        //Trichter
        Rechteck lo = new Rechteck(ABSTAND_LINKS, ABSTAND_OBEN, 50, 150);
        Rechteck lm = new Rechteck(ABSTAND_LINKS,ABSTAND_OBEN+150,  50, 200);
        Rechteck ro = new Rechteck(ABSTAND_RECHTS, ABSTAND_OBEN, 50, 150);
        Rechteck rm = new Rechteck(ABSTAND_RECHTS+14, ABSTAND_OBEN+120, 50, 200);
        Rechteck lu = new Rechteck(ABSTAND_LINKS+125, ABSTAND_OBEN+255, 50, 120);
        Rechteck ru = new Rechteck(ABSTAND_LINKS+304, ABSTAND_OBEN+255, 50, 120);
        boden = new Rechteck(ABSTAND_LINKS+125,ABSTAND_OBEN+375, 230, 40);

        Rechteck[] alleRechtecke = new Rechteck[] {
                lo, lm, ro, rm, lu, ru, boden
        };

        for(Rechteck r : alleRechtecke) {
            r.farbeSetzen("Weiss");
            wurzel.add(r);
            r.physik.typ(Physik.Typ.STATISCH);
        }

        lm.physik.schwerkraft(new Vektor(0,4));

        lm.position.rotation(-(float)Math.PI/4);
        rm.position.rotation((float)Math.PI/4);

        anmelden.ticker(new Ticker(){

            @Override
            public void tick() {
                Kreis marble = makeAMarble();
                wurzel.add(marble);
                marble.physik.typ(Physik.Typ.DYNAMISCH);
                marble.position.set(new Punkt(ABSTAND_LINKS+200, ABSTAND_OBEN-150));
                marble.physik.impulsWirken(new Vektor(Zufall.zFloat()*400-200,Zufall.zFloat()*-500));
            }
        }, 500);

    }

    @Override
    public void frameUpdate(float ts) {

    }

    @Override
    public void tasteReagieren(int code) {
        switch(code) {
            case Taste.X: //Boden togglen
                if(boden.physik.typ()== Physik.Typ.STATISCH) {
                    boden.physik.typ(Physik.Typ.PASSIV);
                    boden.farbeSetzen(new Farbe(255,255,255,100));
                } else {
                    boden.physik.typ(Physik.Typ.STATISCH);
                    boden.farbeSetzen(new Farbe(255,255,255));
                }
                break;
        }
    }

    /**
     * Erstellt eine neue Murmel.
     * @return eine Murmel. Farbe, Masse und Größe variieren.
     */
    public Kreis makeAMarble() {

        Kreis murmel = new Kreis(0,0, Zufall.zInt(50)+10);
        murmel.physik.typ(Physik.Typ.DYNAMISCH);
        murmel.physik.masse(Zufall.zFloat()*2+2);
        murmel.farbeSetzen(new Farbe(
                Zufall.zInt(255), Zufall.zInt(255), Zufall.zInt(255)));

        return murmel;
    }

}
