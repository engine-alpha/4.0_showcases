import ea.*;

import ea.Polygon;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.joints.*;

import java.awt.*;

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
    }

    @Override
    public void initialisieren() {
        super.initialisieren();
        wippeBauen().position.set(new Punkt(500, 500));

        ketteBauen(15).position.verschieben(new Vektor(500, 00));

        leashBauen().position.verschieben(100,100);

        hoverHolderBauen();

        ball = new Kreis(0, 0, 100);
        wurzel.add(ball);

        ball.setColor("Blau");

        ball.position.set(new Punkt(300, 200));
        ball.physik.typ(Physik.Typ.DYNAMISCH);
    }

    private Knoten hoverHolderBauen() {
        Knoten knoten = new Knoten();
        wurzel.add(knoten);

        final int FACT = 2;

        Polygon halter = new Polygon(
                new Punkt(0*FACT,50*FACT), new Punkt(25*FACT, 75*FACT),
                new Punkt(50*FACT, 75*FACT), new Punkt(75*FACT, 50*FACT),
                new Punkt(75*FACT, 100*FACT), new Punkt(0*FACT,100*FACT)
        );
        knoten.add(halter);
        halter.setColor("cyan");
        halter.physik.typ(Physik.Typ.DYNAMISCH);

        Rechteck item = new Rechteck(30*FACT, 0, 35*FACT, 20*FACT);
        knoten.add(item);
        item.setColor(Color.red);
        item.physik.typ(Physik.Typ.DYNAMISCH);


        knoten.position.verschieben(new Vektor(160, 200));

        halter.physik.createDistanceJoint(item, halter.position.mittelPunkt().alsVektor(), item.position.mittelPunkt().alsVektor());


        return knoten;
    }

    private Knoten leashBauen() {
        Knoten knoten = new Knoten();
        wurzel.add(knoten);

        Kreis kx = new Kreis(0,0, 30);
        knoten.add(kx);
        kx.setColor("Blau");
        kx.physik.typ(Physik.Typ.DYNAMISCH);

        Kreis ky = new Kreis(50, 0, 50);
        knoten.add(ky);
        ky.setColor("Gruen");
        ky.physik.typ(Physik.Typ.DYNAMISCH);

        knoten.position.verschieben(500,500);


        kx.physik.createRopeJoint(ky,
                //kx.position.mittelPunkt().alsVektor(),
                //ky.position.mittelPunkt().alsVektor(), 4);
                new Vektor(15,15),
                new Vektor(25,25), 4);

        return knoten;
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
                RevoluteJoint rj = kette[i-1].physik.createRevoluteJoint(kette[i], new Vektor(0, 5).summe(posrel));

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
                schwerkraftActive = !schwerkraftActive;
                ball.physik.schwerkraft(schwerkraftActive ? new Vektor(0, 10) : Vektor.NULLVEKTOR);
                break;
        }
    }
}
