// File:          BeaconController.java
// Date:
// Description:
// Author:
// Modifications:

// You may need to add other webots classes such as
//  import com.cyberbotics.webots.controller.DistanceSensor;
import com.cyberbotics.webots.controller.LED;
import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.TouchSensor;
import java.util.Random;


// Here is the main class of your controller.
// This class defines how to initialize and how to run your controller.
public class BeaconController {

  // This is the main function of your controller.
  // It creates an instance of your Robot instance and
  // it uses its function(s).
  // Note that only one instance of Robot should be created in
  // a controller program.
  // The arguments of the main function can be specified by the
  // "controllerArgs" field of the Robot node
  public static void main(String[] args) {

    // create the Robot instance.
    Robot robot = new Robot();
    Random rand = new Random();
    
    // get the time step of the current world.
    int timeStep = (int) Math.round(robot.getBasicTimeStep());
    
    LED leftLED = robot.getLED("leftLED");
    LED rightLED = robot.getLED("rightLED");
    
    TouchSensor backButton = robot.getTouchSensor("backButton");
    TouchSensor leftButton = robot.getTouchSensor("leftButton");
    TouchSensor rightButton = robot.getTouchSensor("rightButton");
    backButton.enable(timeStep);
    leftButton.enable(timeStep);
    rightButton.enable(timeStep);
    
    leftLED.set(0x0000FF);
    rightLED.set(0xFF0000);
    
    // You should insert a getDevice-like function in order to get the
    // instance of a device of the robot. Something like:
    //  LED led = robot.getLED("ledname");
    //  DistanceSensor ds = robot.getDistanceSensor("dsname");
    //  ds.enable(timeStep);

    double lastTime = robot.getTime();
    double stepTime = .15;
    
    int state = 0;
    int mode = 0;
    int nextMode = 0;
    
    double randomizeStopTime = 0.0;
    double randomizeTime = 2.0;
    double lockoutTime = 0.0;
    
    boolean enabled = true;
    System.out.println("Beacon Controller Initialized: " + robot.getName());
    
    // Main loop:
    // - perform simulation steps until Webots is stopping the controller
    while (robot.step(timeStep) != -1) {
      double currentTime = robot.getTime();
      
      switch(state)
      {
        // Start Randomizing
        case 0:
          nextMode = rand.nextInt(100);
          System.out.println(String.format("Randomizing %s (%d)", robot.getName(), nextMode));
          lastTime -= ((double)nextMode) / 1000.0;
          
          nextMode = nextMode % 2;          

          randomizeStopTime = currentTime + randomizeTime;
          state = 1;
          break;
        
        // Cycle Lights during Randomizing
        case 1:
          if ( (currentTime - lastTime) > stepTime) {
            mode ++;
            mode = mode % 2;
            lastTime = currentTime;
          }
          
          if (currentTime > randomizeStopTime) 
          {
            mode = nextMode;
            state = 2;
            System.out.println("Completed randomizing beacon: "+robot.getName());
            lockoutTime = currentTime;
          }
          break;
          
        // Normal Mode
        case 2:
          if (backButton.getValue() == 1.0) {
            state = 0;
          }
          
          nextMode = mode;
          if (currentTime > lockoutTime) {
            if (leftButton.getValue() == 1.0) {
              
              if (mode == 0 || mode == 3) {
                  nextMode = 2;

              } else if (mode == 1 || mode == 2) {
                  nextMode = 3;
              } 
              
              lockoutTime = currentTime + 5.0;
            }
            
            if (rightButton.getValue() == 1.0) {
              
              if (mode == 1 || mode == 3) {
                  nextMode = 2;

              } else if (mode == 0 || mode == 2) {
                  nextMode = 3;
              } 
              
              lockoutTime = currentTime + 5.0;
            }
          }
          
          mode = nextMode;
          break;
        
        // Sleep Mode
        case 3:
          break;
      }
            
      if (!enabled) {
          leftLED.set(0x000000);
          rightLED.set(0x000000);
      } else {
        switch(mode)
        {
          case 0:
            leftLED.set(0x0000FF);
            rightLED.set(0xFF0000);
            break;
          case 1:
            leftLED.set(0xFF0000);
            rightLED.set(0x0000FF);
            break;
          case 2:
            leftLED.set(0x0000FF);
            rightLED.set(0x0000FF);
            break;
          case 3:
            leftLED.set(0xFF0000);
            rightLED.set(0xFF0000);
            break;
        }
      }
    }

    // Enter here exit cleanup code.
  }
}
