import ea.*;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.joints.*;

/**
 * Einfaches Programm zur Demonstration von Joints in der Engine
 * Created by Michael on 12.04.2017.
 */
public class JointDemo
extends ForceKlickEnvironment {

    private boolean schwerkraftActive=false;

    public static void main(String[] args) {
        new JointDemo();
    }


    private Rechteck wippe;
    private Polygon  basis;


    private Rechteck[] kette;


    private Kreis ball;

    /**
     * Erstellt das Demo-Objekt
     */
    public JointDemo() {
        super(1200, 820, "EA - Joint Demo", false);
        ppmSetzen(100);
    }

    @Override
    public void initialisieren() {
        super.initialisieren();
        //wippeBauen().position.set(new Punkt(500, 500));

        ketteBauen(15).position.verschieben(new Vektor(300, 00));


        ball = new Kreis(0, 0, 100);
        wurzel.add(ball);

        ball.setColor("Blau");

        ball.position.set(new Punkt(300, 200));
        ball.physik.typ(Physik.Typ.DYNAMISCH);
    }

    private Knoten wippeBauen() {
        Knoten bauwerk = new Knoten();
        wurzel.add(bauwerk);

        basis = new Polygon(new Punkt(0, 100), new Punkt(100, 100), new Punkt(50, 0));
        bauwerk.add(basis);
        basis.physik.typ(Physik.Typ.STATISCH);
        basis.setColor("Weiss");

        wippe = new Rechteck(0, 0, 500, 40);
        bauwerk.add(wippe);
        wippe.physik.typ(Physik.Typ.DYNAMISCH);

        wippe.position.mittelpunktSetzen(50, 0);

        wippe.setColor("Grau");

        Vektor verzug = new Vektor(100,100);

        wippe.position.verschieben(verzug);
        basis.position.verschieben(verzug);

        wippe.physik.createRevoluteJoint(basis, new Vektor(50, 0).summe(verzug));

        return bauwerk;
    }

    private Knoten ketteBauen(int kettenlaenge) {
        Knoten ketteK = new Knoten();
        wurzel.add(ketteK);

        kette = new Rechteck[kettenlaenge];
        for(int i = 0; i < kette.length; i++) {
            kette[i] = new Rechteck(0, 0, 50, 10);
            Vektor posrel = new Vektor(45*i,30);
            ketteK.add(kette[i]);
            kette[i].position.verschieben(posrel);
            kette[i].setColor("Gruen");

            kette[i].physik.typ(i == 0 ? Physik.Typ.STATISCH : Physik.Typ.DYNAMISCH);

            if(i != 0) {
                kette[i-1].physik.createRevoluteJoint(kette[i], new Vektor(0, 0).summe(posrel));
            }
        }

        Kreis gewicht = new Kreis(0, 0, 100);
        ketteK.add(gewicht);
        gewicht.setColor("Weiss");

        gewicht.physik.typ(Physik.Typ.DYNAMISCH);
        gewicht.physik.masse(40);

        Vektor vektor = new Vektor(45*kette.length, 35);
        gewicht.position.mittelpunktSetzen(new Punkt(vektor.realX(), vektor.realY()));
        gewicht.physik.createRevoluteJoint(kette[kette.length-1], vektor);

        return ketteK;
    }

    @Override
    public void tasteReagieren(int code) {
        super.tasteReagieren(code);
        switch(code){
            case  Taste.S:
                ball.physik.schwerkraft(schwerkraftActive ? new Vektor(0, 10) : Vektor.NULLVEKTOR);
                schwerkraftActive = !schwerkraftActive;
                break;
        }
    }
}
