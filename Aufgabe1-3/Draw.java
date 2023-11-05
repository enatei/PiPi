import codedraw.CodeDraw;
import codedraw.Palette;
import codedraw.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * STYLE:
 * This class uses objectoriented programming. It has a strong class cohesion, as it only contains methods that are relevant for Draw.
 * it uses nominal abstraction and an instance of Draw can be used where an instance of Simulation is needed, which means that it is replaceable
 * and it contains methods from Simulation (run()). this class has a weak object coupling as its methods are private (except the run()-methode).
 * the class' state is changeable as the class variables are not final
 */

/*
     Draw creates a window and (re-)draws the simulation whenever the run()-method gets called.
     To assure a decent simulation experience, the window size will always be ~700*700px - the pixel size will be adjusted accordingly.
     If the shortest path shall be calculated, the window size will be set to 1200*700px, with ~700*700px on the left for the simulation
     and ~500*700px on the right side for the shortest path comparison text.
     The home field(s) will be drawn in a brown color, the food fields will be drawn in yellow. ants will be drawn in the ant buildings' antColor
     and fields will fluctuate between a green and the ant buildings' scentColor, depending on the scent integer and the height of the field.
 */

//Module/Class Draw,
//Abtraction: real world, subtype of Simulation
//uses instances of Field, World, CodeDraw, Coordinate and HashMap, therefore operates on a higher level of abstraction
//implements the interface Simulation, therefore it is a subtype of Runnable
//gets used in class Test, therefore operates on a lower level of abstraction
public class Draw implements Simulation {
    private Field[][] fields;
    private int fieldsize;
    private CodeDraw cd;
    private int maxHeight;
    private String numberOfSimulation;
    private boolean shortestPath;
    private Coordinate[] foods;
    private int[] dijkstraPaths;
    private Map<Field, Integer> antsShortestPaths = new HashMap<>();
    private Coordinate home;


    //Class method
    /**
     * constructor, sets the objects' variables and calls the object-method draw() to draw the fields, ants, food and home in a window.
     * @param world the world which shall be drawn in a window
     * @param sideLength the side lengths of the window
     */
    public Draw(World world, int sideLength, String numberOfSimulation) {
        this.fields = world.getFields();
        this.maxHeight = world.getMaxHeight();
        this.numberOfSimulation = numberOfSimulation;
        this.shortestPath = world.isShortestPath();
        this.home = world.getHome();

        fieldsize = Math.round((float) 700 / sideLength); //sideLength * fieldsize = ~ 700 (max. fieldsize)

        if(shortestPath) {
            this.foods = world.foods();
            this.dijkstraPaths = world.getDijkstraPaths();
            this.antsShortestPaths = world.getAntsShortestPath();

            //Object cd: instance of type CodeDraw
            this.cd = new CodeDraw(1200, sideLength * fieldsize);
            String txt = "" + numberOfSimulation + " - shortest paths";
            cd.setTitle(txt);
        } else {
            //Object cd: instance of type CodeDraw
            this.cd = new CodeDraw(sideLength * fieldsize, sideLength * fieldsize);
            cd.setTitle(numberOfSimulation);
        }

        draw();
    }


    //Class method
    /**
     * this method clears the entire canvas and calls the objects' draw()-method.
     * @return a boolean to notify if a the window got closed or the simulation stopped
     */
    public boolean run() {
        try {
            cd.clear();
            draw();
            return true;
        } catch (Exception e) {
            return false;
       }
    }


    //Class method
    /**
     * this method draws the fields of the world. Depending on the field state, the color will either be a light brown,
     * yellow, or fluctuate between a shade of green and hot pink (depending on the scent integer and the height of the field).
     * If an ant is located on a field, the color is set to black. A field can either be pixel size or bigger depending on the fieldsize integer.
     * If the shortest path shall be located, it will draw text for the comparison of the dijkstra path and the ants' shortest path.
     */
    private void draw() {

        for (int i = 0; i < fields.length; i++) { //y-coordinate
            for (int j = 0; j < fields[0].length; j++) { //x-coordinate

                //Object field: instance of type Field
                Field field = fields[i][j];

                //this determines the color of the field by using the antstates HOME and FOOD as well as the getAntOnField()-method
                //and the number of scent.
                if (field.getFieldState() == FieldState.HOME) {
                    cd.setColor(new Color(235,156,92));
                } else if (field.getFieldState() == FieldState.FOOD) {
                    cd.setColor(Palette.YELLOW);
                } else if (!field.getAntsOnField().isEmpty()) {
                    cd.setColor(getMajorityColor(field));
                } else {
                    //this calculates the color of the field, depending on height and scent
                    cd.setColor(calculateColor(getScent(field), calculateColorHeight(field), getScentColor(field)));
                }

                //this takes care that pixels are drawn according to the fields size.
                if (j == 0 && i == 0) {
                    cd.fillRectangle(j, i, fieldsize, fieldsize);
                } else if (j == 0 && i != 0) {
                    cd.fillRectangle(j, i * fieldsize, fieldsize, fieldsize);
                } else if (j != 0 && i == 0) {
                    cd.fillRectangle(j * fieldsize, i, fieldsize, fieldsize);
                } else {
                    cd.fillRectangle(j * fieldsize, i * fieldsize, fieldsize, fieldsize);
                }
            }
        }

        if(shortestPath) {
            shortestPathText();
        }

        cd.show();
    }


    //Class method
    /**
     * @param field that needs color calculated
     * @return Color of that particular field
     */
    private Color calculateColorHeight(Field field) {
        int height = field.getHeight();
        return calculateColor((int) ((100.0/maxHeight)*height) + 1, new Color(51,72,63), new Color(85,120,105));
    }


    //Class method
    /**
     * this method calculates the color of a field. If the integer is 0, the method will return the first color. If the
     * integer is 100, it will return the second color. Otherwise the colour will be calculated.
     * @param number an integer which will be used for the color calculation
     * @param color1 the color that will be returned if the given integer is 0
     * @param color2 the color that will be returned if the given integer is 100
     * @return the color of the field
     */
    private Color calculateColor(int number, Color color1, Color color2) {

        if (number == 0) {
            return color1;
        } else if (number >= 100) {
            return color2;
        } else {
            int red1 = color1.getRed();
            int green1 = color1.getGreen();
            int blue1 = color1.getBlue();

            int red2 = color2.getRed();
            int green2 = color2.getGreen();
            int blue2 = color2.getBlue();

            //these interpolate each color1-channel
            int newRed = (int) (red1 + (red2 - red1) * (number * 0.01));
            int newGreen = (int) (green1 + (green2 - green1) * (number * 0.01));
            int newBlue = (int) (blue1 + (blue2 - blue1) * (number * 0.01));

            return new Color(newRed, newGreen, newBlue);
        }
    }


    //Class method
    /**
     * this method formats text
     * @param position the text shall be placed (can be TOP LEFT, CENTER, BOTTOM RIGHT, etc.)
     * @param fontSize the text shall have
     * @param italic a boolean which will set the text italic if desired
     * @param bold a boolean which will set the text bold if desired
     * @param underline a boolean which will set a solid underline underneath the text if desired
     */
    private void formatText(TextOrigin position, int fontSize, boolean italic, boolean bold, boolean underline) {
        TextFormat format = cd.getTextFormat();
        format.setTextOrigin(position);
        format.setFontSize(fontSize);
        format.setItalic(italic);
        format.setBold(bold);
        if(underline) {
            format.setUnderlined(Underline.SOLID);
        } else {
            format.setUnderlined(Underline.NONE);
        }
    }


    //Class method
    /**
     * this method draws the text which will be comparing the dijksta-path and the ants path
     */
    private void shortestPathText() {
        cd.setColor(Palette.BLACK);

        formatText(TextOrigin.TOP_LEFT, 20, false, true, false);
        String coordinate = "Home (" + home.getPosX() + "," + home.getPosY() +  "," + home.getPosZ() + ")";
        cd.drawText((fields.length * fieldsize) + 40, 0, coordinate);

        for(int i = 0; i < foods.length; i++) {
            formatText(TextOrigin.TOP_LEFT, 14, false, true, true);
            coordinate = "Food" + i + " (" + foods[i].getPosX() + "," + foods[i].getPosY() + "," + foods[i].getPosZ() + ")";
            cd.drawText((fields.length * fieldsize) + 40, ((fields.length * fieldsize) / 11) * (i+0.8), coordinate);

            formatText(TextOrigin.TOP_LEFT, 14, false, false, false);
            String dijkstraTxt = "The shortest path is: " + dijkstraPaths[i];
            cd.drawText((fields.length * fieldsize) + 40, ((fields.length * fieldsize) / 11) * (i+0.8) + 18, dijkstraTxt);

            formatText(TextOrigin.TOP_LEFT, 14, true, false, false);
            int value = antsShortestPaths.get(fields[foods[i].getPosY()][foods[i].getPosX()]);
            String antTxt = "";
            if(value == Integer.MAX_VALUE) {
                antTxt = "The ants' shortest path has not been found yet.";
            } else {
                antTxt = "The ants' shortest path is: " + value;
            }
            cd.drawText((fields.length * fieldsize) + 40, ((fields.length * fieldsize) / 11) * (i+0.8) + 34, antTxt);
        }
    }


    //Class method
    /**
     * this method checks in which building the majority of the ants on the given field belong to and returns
     * the ants' color of that building.
     * @param field that contains a list of ants
     * @return the ants' color of the ant building which is mostly represented on that field
     */
    private Color getMajorityColor(Field field) {
        Map<Building,Integer> antsPerBuilding = new HashMap<>();
        int max = 0;
        Color antColor = Color.BLACK;

        for (Ant ant : field.getAntsOnField()) {
            int current = antsPerBuilding.getOrDefault(ant.getAntColony(), 0) + 1;
            antsPerBuilding.put(ant.getAntColony(), current);
            if(current > max) {
                max = current;
                antColor = ant.getAntColony().antColor;
            }

        }
        return antColor;
    }


    //Class method
    /**
     * this method returns the ant building to which the ants' belong to that set the highest scent sum on the given field
     * @param field that holds the sum of scents per ant building
     * @return the ant building that has the highest scent amount on the given field
     */
    private Building getStrongestBuilding(Field field) {
        Building building = null;
        int max = 0;
        for(Map.Entry<Building, Integer> entry : field.getScentPerBuilding().entrySet()) {
            if(entry.getValue() > max) {
                building = entry.getKey();
                max = entry.getValue();
            }
        }
        return building;
    }


    //Class method
    /**
     * @param field that holds the sum of scents per ant building
     * @return an integer of the sum of scents of the strongest ant building on the given field
     */
    private int getScent(Field field) {
        return (getStrongestBuilding(field) == null) ? 0 : field.getScentOfBuilding(getStrongestBuilding(field));
    }


    //Class method
    /**
     *
     * @param field that holds the sum of scents per ant building
     * @return the scents' color of the ant building that holds the highest sum of scent on the given field
     */
    private Color getScentColor(Field field) {
        return (getStrongestBuilding(field) == null) ? Color.WHITE : getStrongestBuilding(field).scentColor;
    }
}