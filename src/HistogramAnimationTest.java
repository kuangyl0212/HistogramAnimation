import java.awt.Font;

import java.awt.Color;
import java.io.*;
import javax.json.*;

public class HistogramAnimationTest {
    public static void main(String[] args) {
        HistogramAnimation h = null;
        for (int i = 0; i < args.length; i++) {
            h = createHistogramAFrom(args[i]);
            h.animate();
        }
    }

    private static HistogramAnimation createHistogramAFrom (String fileName) {
        HistogramAnimation h = null;
        try (
                InputStream is = new FileInputStream( new File( fileName ));
                JsonReader rdr = Json.createReader(is)
        ) {
            JsonObject obj = rdr.readObject().getJsonObject( "histograma" );
            Canvas canvas = getCanvasFrom( obj.getJsonObject( "canvas" ));
            Formats fmts = getFormatsFrom( obj.getJsonObject( "formats" ));
            HistogramData data = getDataFrom( obj.getJsonObject( "data" ));
            h =  new HistogramAnimation( canvas, fmts, data);
        } catch (IOException e) {
            System.out.println( e.getMessage());
        };
        return h;
    }

    private static Canvas getCanvasFrom( JsonObject obj) {
        Canvas canvas = new Canvas();

        JsonArray szArray = obj.getJsonArray( "size" );
        if (szArray != null ) {  // otherwise, use the default size
            int[] size = toIntArray( szArray);
            canvas.x = size[0];
            canvas.y = size[1];
        }

        JsonArray xsArray = obj.getJsonArray( "xscale" );
        if (xsArray != null )  // otherwise, use the default xScale
            canvas.xScale = toDoubleArray( xsArray );

        JsonArray ysArray = obj.getJsonArray( "yscale" );
        if (ysArray != null )  // otherwise, use the default yScale
            canvas.yScale = toDoubleArray( ysArray );

        JsonArray bgcArray = obj.getJsonArray( "bgcolor");
        if (bgcArray != null )  // otherwise, use the default bgColor
            canvas.bgColor = getColorFrom( bgcArray);

        JsonArray cArray = obj.getJsonArray( "color");
        if (cArray != null )    // otherwise, use the default color
            canvas.color = getColorFrom( cArray);

        return canvas;
    }

    private static int[] toIntArray (JsonArray jsa) {
        int[] a = new int[jsa.size()];
        for (int i = 0; i < jsa.size(); i++)
            a[i] = jsa.getInt(i);
        return a;
    }

    private static double[] toDoubleArray (JsonArray jsa) {
        if (jsa == null) return new double[0];
        double[] a = new double[jsa.size()];
        for (int i = 0; i < jsa.size(); i++)
            a[i] = jsa.getJsonNumber(i).doubleValue();
        return a;
    }
    private static double[][] to2DDoubleArray (JsonArray jsas) {
        double[][] d = new double[jsas.size()][];
        int i = 0;
        for (JsonArray jsa : jsas.getValuesAs(JsonArray.class))
            d[i++] = toDoubleArray( jsa );
        return d;
    }

    private static int[][] to2DIntArray (JsonArray jsas) {
        int[][] d = new int[jsas.size()][];
        int i = 0;
        for (JsonArray jsa : jsas.getValuesAs(JsonArray.class))
            d[i++] = toIntArray( jsa );
        return d;
    }

    private static String[] toStringArray (JsonArray jsa) {
        String[] s = new String[jsa.size()];
        for (int i = 0; i < jsa.size(); i++)
            s[i] = jsa.getString(i);
        return s;
    }

    private static Color getColorFrom(JsonArray jsa) {
        int[] c = toIntArray( jsa);
        return new Color( c[0], c[1], c[2]);
    }
    private static Color getColorFrom (JsonArray jsa, Color defaultColor) {
        if (jsa == null) return defaultColor;
        int[] c = toIntArray( jsa);
        return new Color( c[0], c[1], c[2]);
    }
    private static Font getFontFrom(JsonArray jsa) {
        String[] s = toStringArray(jsa);
        return new Font(s[0], Font.PLAIN, Integer.parseInt(s[1]));
    }

    private static Formats getFormatsFrom( JsonObject obj) {
        Formats fmts = new Formats();
        if(obj.containsKey( "margins"))
            fmts.margins = toDoubleArray( obj.getJsonArray( "margins"));

        fmts.isBarFilled = obj.getBoolean( "isbarfilled", true);

        if(obj.containsKey("barfillcolor"))
            fmts.barFillColor = getColorFrom( obj.getJsonArray( "barfillcolor"));

        fmts.hasBarFrame = obj.getBoolean( "hasbarframe", true);

        if(obj.containsKey("barframecolor"))
            fmts.barFrameColor = getColorFrom( obj.getJsonArray( "barframecolor"));

        fmts.hasBorder = obj.getBoolean( "hasborder", true);

        if(obj.containsKey("bordercolor"))
            fmts.borderColor = getColorFrom( obj.getJsonArray( "bordercolor"));

        if(obj.containsKey("rulercolor"))
            fmts.rulerColor = getColorFrom( obj.getJsonArray( "rulercolor"));

        if (obj.containsKey("rulermarkcolor"))
            fmts.rulerMarkColor = getColorFrom( obj.getJsonArray( "rulermarkcolor"));

        fmts.hasRightRuler = obj.getBoolean( "hasrightruler", true);

        if(obj.containsKey("keycolor"))
            fmts.keyColor = getColorFrom( obj.getJsonArray( "keycolor"));

        fmts.hasHeader = obj.getBoolean( "hasheader", true);

        if(obj.containsKey("headercolor"))
            fmts.headerColor = getColorFrom( obj.getJsonArray( "headercolor"));

        fmts.hasFooter = obj.getBoolean( "hasfooter", true);

        if (obj.containsKey("footercolor"))
            fmts.footerColor = getColorFrom( obj.getJsonArray( "footercolor"));

        if(obj.containsKey("rulerfont"))
            fmts.rulerFont = getFontFrom(obj.getJsonArray("rulerfont"));

        if(obj.containsKey("keyfont"))
            fmts.keyFont = getFontFrom(obj.getJsonArray("keyfont"));

        if(obj.containsKey("headerfont"))
            fmts.headerFont = getFontFrom(obj.getJsonArray("headerfont"));

        if(obj.containsKey("footerfont"))
            fmts.footerFont = getFontFrom(obj.getJsonArray("footerfont"));

        if(obj.containsKey("rulernumberformat"))
            fmts.rulerNumberFormat = obj.getString("rulernumberformat");

        if(obj.containsKey("stepfont"))
            fmts.stepFont = getFontFrom(obj.getJsonArray("stepfont"));

        if (obj.containsKey("visiblecount"))
            fmts.visibleCount = obj.getInt("visiblecount");

//      fmts.colorList = to2DIntArray(obj.getJsonArray("colors"));
        fmts.showRank = obj.getBoolean( "showrank", false);

        return fmts;
    }

    private static HistogramData getDataFrom( JsonObject obj) {
        HistogramData data = new HistogramData();
        data.header = obj.getString( "header", "");
        data.footer = obj.getString( "footer", "");
        data.minValue = obj.getJsonNumber( "minvalue") != null ?
                obj.getJsonNumber( "minvalue").doubleValue()
                : 0;
        String[] steps_list = toStringArray( obj.getJsonArray( "steps"));
        for (int i = 0; i < ((String[]) steps_list).length; i++) {
            data.steps.add(steps_list[i]);
        }
        data.labels = toStringArray(obj.getJsonArray("labels"));
        data.values = to2DDoubleArray( obj.getJsonArray( "values"));
        return data;
    }
}
