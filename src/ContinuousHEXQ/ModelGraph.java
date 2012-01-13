package ContinuousHEXQ;

import javax.swing.*; // For JPanel, etc.
import java.awt.*;           // For Graphics, etc.
import java.awt.geom.*;      // For Ellipse2D, etc.

/**
 *
 * @author bernhardhengst
 */
public class ModelGraph extends JPanel {
    private Ellipse2D.Double circState = new Ellipse2D.Double(0, 0, 0, 0);
    private Ellipse2D.Double circNextState = new Ellipse2D.Double(0, 0, 0, 0);
    float scaleX = 0;
    float scaleY = 0;
    float originX = 0;
    float originY = 0;
    int ix, iy, ixd, iyd = 0;
    int t1x, t1y, t2x, t2y = 0;
    Color colour;
    Model m;
    public boolean drawTransitions = false;
    public boolean clear = false;
    public boolean drawTransition = false;
    public boolean policy = false;
    
    ModelGraph(Model model){
        m = model;
    }

  @Override
  public void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D)g;
      if(drawTransition) {g.drawLine(t1x, t1y, t2x, t2y); drawTransition=false;}
      if(clear) {clear(g); clear = false;}
      // draw transition cases
      if(drawTransitions){
          for (int i=0;i<m.pCases;i++){
              Tset t = m.cases[i];
              ix = (int)(20*t.state[0]+400);
              iy = (int)(20*t.state[1]+400);
              circState = new Ellipse2D.Double(ix-2, iy-2, 4, 4);
              g.setColor(Color.blue);
              g2d.draw(circState);
              for(int a=0;a<t.action.length;a++){
                  if(t.action[a]==-1) continue; // no transition availabel for this action
                  ixd = (int)(20*t.nextStates[a][0]+400);
                  iyd = (int)(20*t.nextStates[a][1]+400); 
                  switch (a) {
                      case 0: colour = Color.RED; break;
                      case 1: colour = Color.GREEN; break;
                      case 2: colour = Color.BLUE; break;
                      default: colour = Color.GRAY;
                  }
                  g.setColor(colour);
                  g.drawLine(ix, iy, ixd, iyd);
              }
          }
          drawTransitions = false;
      }
      //draw policy
      if(policy){
          clear(g);
          for (int x=0; x<800; x+=10) for(int y=0;y<=800;y+=10){
              float s [] = {(float)x/20f-400f/20f,(float)y/20f-400f/20f};
              int a = m.greedyAction(s);
              switch (a) {
                  case 0: colour = Color.RED; break;
                  case 1: colour = Color.GREEN; break;
                  case 2: colour = Color.BLUE; break;
                  case -1: colour = Color.black; break;
                  case -2: colour = Color.GRAY; break;
                  default: colour = Color.YELLOW;
              }
              g.setColor(colour);
              circState = new Ellipse2D.Double(x-2, y-2, 4, 4);
              g2d.fill(circState);
          }
          policy = false;
      }
      //axes
      g.setColor(Color.BLACK);
      g.drawLine(400, 0, 400, 800);
      g.drawLine(0, 400, 800, 400);      
  }
  
//  public void go(Tset c, int a){
//      ix = (int)(20*c.state[0]+400);
//      iy = (int)(20*c.state[1]+400);
//      circState = new Ellipse2D.Double(ix-2, iy-2, 4, 4);
//      ixd = (int)(20*c.nextStates[a][0]+400);
//      iyd = (int)(20*c.nextStates[a][1]+400); 
//      circNextState = new Ellipse2D.Double(ixd-1, iyd-1, 2, 2);
//      switch (a) {
//          case 0: colour = Color.magenta; break;
//          case 1: colour = Color.red; break;
//          case 2: colour = Color.cyan; break;
//          default: colour = Color.black;
//      }
//      repaint();    
//  }
  
//    public void drawModel(Model model){
//        m = model;
//        repaint();
//    }
    
    public void drawTransitions(){
        drawTransitions = true;
        repaint();
    }
    
    public void drawClear(){
        clear = true;
        repaint();
    }
  
    public void drawTransition(Transition c){
      t1x = (int)(20*c.state[0]+400);
      t1y = (int)(20*c.state[1]+400);
      t2x = (int)(20*c.nextState[0]+400);
      t2y = (int)(20*c.nextState[1]+400); 
      drawTransition = true;
      repaint();    
  }
    
    public void drawPolicy(){
        policy = true;
        repaint();
    }

  // super.paintComponent clears offscreen pixmap,
  // since we're using double buffering by default.

  protected void clear(Graphics g) {
    super.paintComponent(g);
  }
  
//  protected Ellipse2D.Double getCircle() {
//    return(circState);
//  } 
}

