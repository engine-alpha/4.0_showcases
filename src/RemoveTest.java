import ea.*;

/**
 * Created by Michael on 12.04.2017.
 */
public class RemoveTest
extends Game{

    public static void main(String[] args) {
        new RemoveTest();
        EngineAlpha.setVerbose(true);
    }

    private Kreis ball;
    private Rechteck[] walls;
    private Rechteck[] blocks;

    public RemoveTest() {
        super(1200, 820, "EA Remove Test");

        Rechteck r = new Rechteck(0,0, 200, 200);
    }

    @Override
    public void initialisieren() {
        ppmSetzen(200);
        ball = new Kreis(300, 300, 50);
        wurzel.add(ball);
        ball.setColor("Rot");
        ball.physik.typ(Physik.Typ.DYNAMISCH);

        walls = new Rechteck[] {
                new Rechteck(0,0, 20, 1000),
                new Rechteck(0,0, 1000, 20),
                new Rechteck(1000, 0, 20, 1000),
                new Rechteck(0, 1000, 1000, 20)
        };

        blocks = new Rechteck[5];
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Rechteck(450 + (i*40), 250, 20, 100);
            wurzel.add(blocks[i]);
            blocks[i].setColor("Grau");
            blocks[i].physik.typ(Physik.Typ.STATISCH);
            final Raum thisBlock = blocks[i];
            final int index = i;
            anmelden.kollisionsReagierbar(raum->{
                wurzel.entfernen(thisBlock);
            }, blocks[i]);
        }

        for(Rechteck r : walls) {
            wurzel.add(r);
            r.setColor("Weiss");
            r.physik.typ(Physik.Typ.STATISCH);
        }
    }

    @Override
    public void tasteReagieren(int code) {
        switch (code) {
            case Taste.S:
                ball.physik.impulsWirken(new Vektor(10,0));
                break;
            case Taste.X: // Froce Garbage Collection
                System.gc();
                break;
        }
    }
}
