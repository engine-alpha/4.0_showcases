import ea.*;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.joints.*;

/**
 * Einfaches Programm zur Demonstration von Joints in der Engine
 * Created by Michael on 12.04.2017.
 */
public class JointDemo
extends ForceKlickEnvironment {

    public static void main(String[] args) {
        new JointDemo();
    }


    private Rechteck wippe;
    private Polygon  basis;

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

        wippe.physik.createRevoluteJoint(basis, new Vektor(50, 0));

        return bauwerk;
    }

    @Override
    public void tasteReagieren(int code) {
        super.tasteReagieren(code);
        switch(code){
            case  Taste.S:
                ball.physik.impulsWirken(new Vektor(0, 10));
                break;
        }
    }
}
