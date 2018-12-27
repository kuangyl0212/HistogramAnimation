import java.awt.Color;
import java.awt.Font;
import java.util.*;

class Canvas {
   int x = 512, y = 512;
   double[] xScale = { 0, 1.0 };  // MIN, MAX
   double[] yScale = { 0, 1.0 };  // MIN, MAX
   Color bgColor = Color.WHITE;
   Color color = Color.BLACK;
}

class Formats {
   double[] margins = { 0.15, 0.15, 0.15, 0.15 };  // NORTH, SOUTH, WEST, EAST
   boolean isBarFilled = true;
   Color barFillColor = new Color(0x32d3eb);
   boolean hasBarFrame = true;
   Color barFrameColor = new Color(0x60acfc);
   boolean hasBorder = true;
   Color borderColor = new Color(0x606060);
   Color rulerColor = new Color(0x333333);
   Color rulerMarkColor = new Color(0xf7f7f7);
   boolean hasRightRuler = true;
   Color keyColor = new Color(0x333333);
   boolean hasHeader = true;
   Color headerColor = new Color(0x333333);
   boolean hasFooter = true;
   Color footerColor = new Color(0x333333);
   Font rulerFont =  new Font( "consolas", Font.PLAIN, 12 );
   Font keyFont = new Font( "consolas", Font.PLAIN, 12 );
   Font headerFont = new Font( "calibri", Font.PLAIN, 20 );
   Font footerFont = new Font( "calibri", Font.PLAIN, 20 );
   String rulerNumberFormat = null;
   Font stepFont = new Font("consolas", Font.PLAIN, 42);

    int visibleCount = 10;
    boolean showRank = false;
//   int[][] colorList = {};
}

class XYZColor {
    double X;
    double Y;
    double Z;
}

class LabColor {
    double L;
    double A;
    double B;
}

class HistogramData {
   String header = "";
   String footer = "";
   double minValue = 0.0;
   ArrayList<String> steps = new ArrayList<>();
//   String[] steps = { };
   String[] labels = { };
   double[][] values = { };
   Item[][] items;
   int stepIndex = 0;
   HashMap<Integer, Color> colorMap;
 }

class Item {
    int labelIndex;
    double value;
    double grade = 0;
}

class ItemComparator implements Comparator<Item>{
    @Override
    public int compare(Item e1, Item e2) {
        if(e1.value < e2.value) return 1;
        if(e1.value > e2.value) return -1;
        return 0;
    }
}

public class HistogramAnimation {
   Canvas c;
   Formats f;
   HistogramData d;
   double[] xValue;  // MIN, MAX
   double[] yValue;  // MIN, MAX
   double[] xScale;  // MIN, MAX
   double[] yScale;  // MIN, MAX
   int rulerGrade;   
   double rulerStep;

   public HistogramAnimation(Canvas c, Formats f, HistogramData d) {
      this.c = c;
      this.f = f;
      this.d = d;
      xValue = new double[2];
      yValue = new double[2];
      xScale = new double[2];
      yScale = new double[2];

   }

   private static Color genRandColor() {
       Random random = new Random();

       int r = random.nextInt(256);
       int g = random.nextInt(256);
       int b = random.nextInt(256);
       // some little trick to get fine color
       // but can not get rid of duplicate colors
       while ((r>200 && g>200 && b>200) || (r<100 && g<100 && b<100))
       {
           r = random.nextInt(256);
           g = random.nextInt(256);
           b = random.nextInt(256);
       }
       return new Color(r, g, b);
   }

   public static XYZColor RGB2XYZ(Color color) {
       int r = color.getRed();
       int g = color.getGreen();
       int b = color.getBlue();
       double var_R,var_G,var_B;
       var_R = (r / 255.0f);
       var_G = (g / 255.0f);
       var_B = (b / 255.0f);

       if (var_R > 0.04045) {
           var_R = Math.pow((var_R + 0.055)/1.055, 2.4);

       }
       else {
           var_R = var_R / 12.92;
       }
       if (var_G > 0.04045) {
           var_G = Math.pow((var_G + 0.055)/1.055, 2.4);
       }
       else {
           var_G = var_G / 12.92;
       }
       if (var_B > 0.04045) {
           var_B = Math.pow((var_B + 0.055)/1.055, 2.4);
       }
       else {
           var_B = var_B / 12.92;
       }

       var_R = var_R * 100.0f;
       var_G = var_G * 100.0f;
       var_B = var_B * 100.0f;
        XYZColor xyzColor = new XYZColor();
       //Observer.   =   2°,   Illuminant   =   D65
       xyzColor.X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
       xyzColor.Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
       xyzColor.Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
       return xyzColor;
   }

   public static LabColor XYZ2Lab(XYZColor xyzColor) {
       double var_X,var_Y,var_Z;
       double x = xyzColor.X;
       double y = xyzColor.Y;
       double z = xyzColor.Z;
       var_X = x / 95.047f;
       var_Y = y / 100.000f;
       var_Z = z / 108.883f;

       if ( var_X > 0.008856 ) {
           var_X = Math.pow((float)var_X,1.0f/3.0f);
       }
       else {
           var_X = ( 7.787 * var_X ) + ( 16 / 116 );
       }
       if (var_Y > 0.008856) {
           var_Y = Math.pow((float)var_Y,1.0f/3.0f);
       }
       else {
           var_Y = ( 7.787 * var_Y ) + ( 16 / 116 );
       }
       if ( var_Z > 0.008856 ) {
           var_Z = Math.pow((float)var_Z,1.0f/3.0f);
       }
       else {
           var_Z = ( 7.787 * var_Z ) + ( 16 / 116 );
       }
       LabColor labColor = new LabColor();
       labColor.L = ( 116 * var_Y ) - 16;
       labColor.A = 500 * ( var_X - var_Y );
       labColor.B = 200 * ( var_Y - var_Z );
        return labColor;
   }

   public static double deltaE(Color a, Color b) {
       LabColor la = XYZ2Lab(RGB2XYZ(a));
       LabColor lb = XYZ2Lab(RGB2XYZ(b));
       double deltaL = la.L - lb.L;
       double deltaA = la.A - lb.A;
       double deltaB = la.B - lb.B;
       return Math.sqrt(Math.pow(deltaL, 2) + Math.pow(deltaA, 2) + Math.pow(deltaB, 2));
   }

   private void interpolate(int i) {
       int nLabel = d.labels.length;
       for (int j = 0; j < nLabel; j++) {
           Item item = d.items[(i) * INTERP_COUNT][j];
           Item[] nextItems = d.items[(i+1)* INTERP_COUNT];
           // find the matching one TODO this algorithm should be improved (or improve data structure?) otherwise the complexity would explode
           Item nextItem = null;
           for (int k = 0; k < nLabel; k++) {
                if (item.labelIndex == nextItems[k].labelIndex) {
                    nextItem = nextItems[k];
                    break;
                }
           }
           double vStep = (nextItem.value - item.value) / INTERP_COUNT;
           double gSpan = nextItem.grade - item.grade;

           for (int m = 1; m < INTERP_COUNT; m++) {
               d.items[(i) * INTERP_COUNT + m][j] = new Item();
               Item targetItem = d.items[(i) * INTERP_COUNT + m][j];
               targetItem.labelIndex = item.labelIndex;
               targetItem.value = item.value + vStep * m;
               // non-linear interpolate ref:http://inloop.github.io/interpolator?Library=AccelerateDecelerate
               targetItem.grade = item.grade + ((Math.cos(((double)m / INTERP_COUNT + 1) * Math.PI) / 2.0) + 0.5) * gSpan;

           }
       }
   }

   private void preCompute() {
       int nStep = d.steps.size();
       int nLabel = d.labels.length;
       d.items = new Item[nStep* INTERP_COUNT + 1][nLabel];
       String temp_step = d.steps.get(0);
        d.steps.add(0, temp_step);
       for (int i = 1; i <= nStep; i++) {    // for every country

           // for every year
           for (int j = 0; j < nLabel; j++) {
               Item e = new Item();
               e.labelIndex = j;
               e.value = d.values[j][i-1];
               d.items[i* INTERP_COUNT][j] = e; // the index is inverse
           }
           Arrays.sort(d.items[i*INTERP_COUNT], new ItemComparator());
           for (int k = 0; k < d.items[i*INTERP_COUNT].length; k++) {
               if (k < f.visibleCount)
                   d.items[i* INTERP_COUNT][k].grade = k;
               else
                   d.items[i* INTERP_COUNT][k].grade = f.visibleCount; // not valuable
           }

           if (i>1) {
                interpolate(i - 1);
           }
       }
       for (int m = 0; m < nLabel; m++) {
           Item e = new Item();
           e.labelIndex = d.items[INTERP_COUNT][m].labelIndex;
           e.value = 0;
           e.grade = d.items[INTERP_COUNT][m].grade;
           d.items[0][m] = e; // the index is inverse
       }
       interpolate(0);
       Item[] tempItems = d.items[(nStep - 1) *INTERP_COUNT];

       // assign random color
       d.colorMap = new HashMap<>();
       for (int i = 0; i < nLabel; i++) {
           Color color = genRandColor();
           boolean fineColor = false;
           if (i == 0) fineColor = true;
           while (!fineColor) {
               boolean flag = false;
               for (int j = 0; j < i; j++) {
                   Color oColor = d.colorMap.get(tempItems[j].labelIndex);
                   if (deltaE(color, oColor) < 10) {
                       color = genRandColor();
                       flag = true;
                       break;
                   }
               }
               if (!flag) fineColor = true;
           }
           d.colorMap.put(tempItems[i].labelIndex,color);
       }
   }

   private void setHistogramParameters () {
       Item[] a = null;
       if (d.stepIndex < INTERP_COUNT) {
           a = d.items[INTERP_COUNT];
       }
      else a = d.items[d.stepIndex];
      yValue[MIN] = 0;
      yValue[MAX] = f.visibleCount;
      double max = a[0].value;
      xValue[MIN] = d.minValue;
      double span = max - xValue[MIN];
      double factor = 1.0;
      if (span >= 1)
         while (span >= 10) { span /= 10; factor *= 10; } 
      else
         while (span < 1)   { span *= 10; factor /= 10; }
      int nSpan = (int)Math.ceil(span);
      xValue[MAX] = max;

      switch (nSpan) {
         case 1 :  rulerGrade = 5; rulerStep = factor/5; break;
         case 2 :
         case 3 :  rulerGrade = nSpan*2; rulerStep = factor/2; break;
         default : rulerGrade = nSpan; rulerStep = factor; break;
      }
   }
   public void animate() {
       preCompute();
       StdDraw.enableDoubleBuffering();
       setCanvas();
       int n = d.items.length;
       for (int i = 0; i < n; i++) {
           StdDraw.clear();
           d.stepIndex = i;
           setHistogramParameters();
           drawBase();
           StdDraw.show();
           StdDraw.pause(50);
       }
   }

   public void drawBase() {
      plotRuler();
      plotBars();
      plotStep();
      if (f.hasHeader) plotHeader();
   }

   private void setCanvas () {
      StdDraw.setCanvasSize( c.x, c.y );
      setOriginalScale();
      StdDraw.clear( c.bgColor);
      StdDraw.setPenColor( c.color);
   }

   private void setHistogramScale (int nBars) {
      double ySpacing = (nBars + 1) / (1 - f.margins[NORTH] - f.margins[SOUTH]);
      yScale[MIN] = yValue[MIN] - f.margins[SOUTH] * ySpacing;
      yScale[MAX] = yValue[MAX] + f.margins[NORTH] * ySpacing;
      StdDraw.setYscale( yScale[MIN], yScale[MAX]);

      double span = xValue[MAX] - xValue[MIN];
      double xSpacing = span / (1 - f.margins[WEST] - f.margins[EAST]);
      xScale[MIN] = xValue[MIN] - f.margins[WEST] * xSpacing;
      xScale[MAX] = xValue[MAX] + f.margins[EAST] * xSpacing; // TODO always scale to the maximum value
      StdDraw.setXscale( xScale[MIN], xScale[MAX]);
   };

   private void setOriginalScale() {
      StdDraw.setXscale( c.xScale[MIN], c.xScale[MAX]);
      StdDraw.setYscale( c.yScale[MIN], c.yScale[MAX]);
   }

   private void plotBars () {
      Item[] a = d.items[d.stepIndex];
      int n = f.visibleCount;
      setHistogramScale( n );

      double span = xValue[MAX] - xValue[MIN];
      double xSpacing = span / (1 - f.margins[WEST] - f.margins[EAST]);


     for (int i = 0; i < a.length; i++) {
         if (a[i].grade > f.visibleCount + 1) continue;
         Color color = d.colorMap.get(a[i].labelIndex);
         // for fade in and out
         int alpha = 255;
         double offset = 0; // move slightly left then disappear or appear from left
         if (a[i].grade > f.visibleCount-1) {
             offset = a[i].grade - (f.visibleCount-1);
             alpha = (int)((1 - offset) * 255);
             alpha = alpha > 255 ? 255 : alpha;
             alpha = alpha < 0 ? 0 : alpha;
         }
         offset = 0.01*xSpacing*offset;
         color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
         StdDraw.setPenColor( color);
         StdDraw.filledRectangle(a[i].value/2 - offset, n -a[i].grade, a[i].value/2, 0.25);
         StdDraw.setFont( f.rulerFont);
         StdDraw.textRight(-0.005*xSpacing- offset, n-a[i].grade, d.labels[a[i].labelIndex]);
         StdDraw.textLeft(a[i].value + 0.005*xSpacing - offset, n-a[i].grade,  String.valueOf((int)(a[i].value)));
         if (f.showRank && a[i].grade < 3){
             StdDraw.setPenColor( new Color(160,160,160));
             StdDraw.setFont( f.headerFont);
             StdDraw.textRight(a[i].value - 0.005*xSpacing, n-a[i].grade+0.1,  d.labels[a[i].labelIndex]);
         }
         /* only for debug
                //             StdDraw.setPenColor( new Color(160,160,160));
                //             StdDraw.textRight(a[i].value - 0.005*xSpacing, n-a[i].grade+0.1,  String.valueOf((a[i].grade)));
        */

     }
   }

   private void plotStep() {
       StdDraw.setFont( f.stepFont );
       StdDraw.setPenColor(new Color(100, 100, 100));
       String step = d.steps.get(d.stepIndex / INTERP_COUNT);

       StdDraw.textRight(xValue[MAX], yValue[MIN] + 1.5, step);
   }

   private void plotRuler() {
//      Font font = new Font( "consolas", Font.PLAIN, 12 ); // TO BE Customized
      StdDraw.setFont( f.rulerFont );
      StdDraw.setPenColor( f.rulerColor );
      final double y0 = yValue[MIN], y1 = yValue[MAX];
      String[] mark = new String[rulerGrade+1];
      for (int i = 0; i <= rulerGrade; i++) {
         double x = xValue[MIN] + i * rulerStep;
         mark[i] = numberForRuler( x );
         if (x < xValue[MAX])
             StdDraw.line( x, y0, x, y1 );
      }
      int len = maxMarkLength( mark );      
      final double ys = yScale[MIN] + 0.7 * (yValue[MIN] - yScale[MIN]);
      for (int i = 0; i <= rulerGrade; i++) {
         double x = xValue[MIN] + i * rulerStep;
         if (x < xValue[MAX])
            StdDraw.text( x, ys, String.format( "%" + len + "s", mark[i] ));
      }
   }
   
   private String numberForRuler (double x) {   // TO BE Customized
      if (f.rulerNumberFormat != null) return String.format(f.rulerNumberFormat, x); // only accept formats for double type!
      if (yValue[MAX] >= 5 && rulerStep > 1) return "" + (int)x;
      if (rulerStep > 0.1) return String.format( "%.1f", x ); 
      if (rulerStep > 0.01) return String.format( "%.2f", x ); 
      if (rulerStep > 0.001) return String.format( "%.3f", x ); 
      if (rulerStep > 0.0001) return String.format( "%.4f", x ); 
      if (rulerStep > 0.00001) return String.format( "%.5f", x ); 
      return String.format( "%g", x );
   }      

   private int maxMarkLength (String[] sa) {
      int n = sa[0].length();
      for (String s : sa)
         if (n < s.length()) n = s.length(); 
      return n;
   }

   private void plotBorder() {
      double x = .5 * (xValue[MIN] + xValue[MAX]);
      double y = .5 * (yValue[MIN] + yValue[MAX]);
      double halfWidth  = .5 * (xValue[MAX] - xValue[MIN]);
      double halfHeight = .5 * (yValue[MAX] - yValue[MIN]);
      StdDraw.setPenColor( f.borderColor );
      StdDraw.rectangle( x, y, halfWidth, halfHeight);
   } 

   private void plotRightRuler() {} //TODO
   
   private void plotHeader() {
//      Font font = new Font( "calibri", Font.PLAIN, 20 ); // TO BE Customized
      StdDraw.setFont( f.headerFont );
      double x = .5 * (xScale[MIN] + xScale[MAX]);
      double y = .5 * (yValue[MAX] + yScale[MAX]);
      StdDraw.setPenColor( f.headerColor );
      StdDraw.text( x, y, d.header );
   } 

   
   private final static int NORTH = 0;
   private final static int SOUTH = 1;
   private final static int WEST  = 2;
   private final static int EAST  = 3;
   private final static int MIN  = 0;
   private final static int MAX  = 1;
   private final static int INTERP_COUNT = 36;
}
