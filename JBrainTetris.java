import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * Write a description of class JBrainTetris here.
 *
 * @author (nwang1)
 * @version (6/1/21)
 */
public class JBrainTetris extends JTetris
{
    private Brain brain;
    private boolean brainPower;
    private Move moveA;

    // constructor to construct JBrainTetris objects
    public JBrainTetris(int width, int height) {
        super(width, height);
    }

    @Override
    public Container createControlPanel() {
        Container newContainer = super.createControlPanel();
        BrainFactory newBrainFactory = new BrainFactory();
        ArrayList<Brain> brains = newBrainFactory.createBrains();

        String[] name = new String[brains.size()];
        for (int i = 0; i < brains.size(); i++){
            name[i] = brains.get(i).toString();
        }
        JComboBox newComboBox = new JComboBox(name);
        newContainer.add(newComboBox);
        newComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String tempBrain = (String) newComboBox.getSelectedItem();
                    for (int i = 0; i < name.length; i++) {
                        if (name[i].equals(tempBrain)) {
                            brain = brains.get(i);
                            return;
                        }
                    }
                }
            });        
        JButton newButton = new JButton("Enable Brain");
        this.brain = brains.get(0);
        this.brainPower = false;
        if (this.testMode) {
            this.brainPower = true;
            newButton.setText("Disable Brain");
        }       
        newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (brainPower == false) {
                        newButton.setText("Disable Brain");
                    } else {
                        newButton.setText("Enable Brain");
                    }
                    brainPower = !brainPower;
                }
            });
        newContainer.add(newButton);
        return newContainer;
}

@Override
public Piece pickNextPiece() {
Piece piece2 = super.pickNextPiece();
super.board.commit();
this.moveA = this.brain.bestMove(board,piece2,HEIGHT);
return piece2;
}

@Override
public void tick(int verb) {
if (this.brainPower && verb == DOWN && this.moveA != null) {
if (currentPiece.nextRotation() != moveA.getPiece().nextRotation()) {
tick(ROTATE);
} else if (moveA.getX() < currentX) {
tick(LEFT);
} else {
tick(RIGHT);
}  
}

if (!this.gameOn)
{
return;
}

if (this.currentPiece != null)
{
this.board.undo();   // remove the piece from its old position
}

// Sets the newXXX attributes
this.computeNewPosition(verb);

// try out the new position (rolls back if it doesn't work)
int status = this.setCurrent(this.newPiece, this.newX, this.newY);

// if row clearing is going to happen, draw the whole board so the green
//      row shows up
if (status ==  Board.PLACE_ROW_FILLED)
{
this.repaint();
}

boolean failed = (status >= Board.PLACE_OUT_BOUNDS);
// if it didn't work, put it back the way it was
if (failed)
{
if (this.currentPiece != null)
{
this.board.place(this.currentPiece, this.currentX, this.currentY);
}
}

/*
 * How to detect when a piece has landed:
 *      if this move hits something on its DOWN verb, and the previous
 *          verb was also DOWN (i.e. the player was not still moving it),
 *          then the previous position must be the correct "landed"
 *          position, so we're done with the falling of this piece.
 */
if (failed && verb==DOWN && !this.moved)   // it's landed
{
if (this.board.clearRows())
{
this.repaint();  // repaint to show the result of the row clearing
}

// if the board is too tall, we've lost
if (this.board.getMaxHeight() > this.board.getHeight() - TOP_SPACE)
{
this.stopGame();
}
// Otherwise add a new piece and keep playing
else
{
this.addNewPiece();
}
}

// Note if the player made a successful non-DOWN move --
//      used to detect if the piece has landed on the next tick()
this.moved = (!failed && verb!=DOWN);
}
}
