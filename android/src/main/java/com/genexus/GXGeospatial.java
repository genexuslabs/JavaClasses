package com.genexus;

import java.io.StringWriter;
import java.text.ParseException;
import java.util.Locale;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.distance.DistanceCalculator;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.io.ShapeWriter;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.SpatialRelation;
//import org.noggit.JSONParser.*;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import com.genexus.internet.IGxJSONSerializable;


@Root
public final class GXGeospatial implements java.io.Serializable, IGxJSONSerializable{

	private Shape innerShape;
	private String lastError;
	private int lastErrorCode;
	private int srid;
	private	SpatialContext ctx;
	private boolean isJTS=false;
	private static String emptyGeography = "GEOMETRYCOLLECTION EMPTY";
	private static String emptyPoint = "POINT(0,0)";
	private static String emptyLine = "LINESTRING( 0 0,0 1)";
	private static String emptyPoly = "POLYGON((0 0, 0 1, 1 0,0 0))";
	private static String s4j_emptyGeography = "GEOMETRYCOLLECTION ()";

	public GXGeospatial()
	{
		this(emptyGeography);
	}

	public GXGeospatial(String serialString, String format)
	{
		ctx = SpatialContext.GEO;
		if (format.equals("wkt"))
		{
			this.fromWKT(serialString);
		}
		else
		{
			this.fromGeoJSON(serialString);
		}
	}

	public GXGeospatial(double latitude, double longitude)
	{
		ctx = SpatialContext.GEO;
		String wktText = "";
		wktText =  wktText + "POINT(" + Double.toString(longitude) + " " + Double.toString(latitude) + ")";
		this.fromWKT(wktText);
	}

	public GXGeospatial(String wkt)
	{
		this(wkt, "wkt");
	}

	public void initJTSContext()
	{
		SpatialContextFactory euclidean = new JtsSpatialContextFactory();
		euclidean.shapeFactoryClass = org.locationtech.spatial4j.shape.jts.JtsShapeFactory.class;
		euclidean.geo = false;
		ctx = euclidean.newSpatialContext();
	}

	public boolean isJTSType()
	{
		return isJTS;
	}

	public Shape innerValue()
	{
		return innerShape;
	}

	public String errorDescription()
	{
		return lastError;
	}

	public int equalGeography(GXGeospatial geoData)
	{
		return ((this.toWKT().equals(geoData.toWKT()))? 0 : 1);
	}
	
	public Double getLatitude()
	{
		if (innerShape != null && innerShape instanceof Point)
		{
			return ((Point) innerShape).getY();
		}
		else
			return 0.0d;
	}

	public Double getLongitude()
	{
		if (innerShape != null && innerShape instanceof Point)
		{
			return ((Point) innerShape).getX();
		}
		else
			return 0.0d;
	}

	public int getSrid() {
		return srid;
	}

	public void setSrid(int value) {
		srid = value;
	}

	public String toWKT()
	{
		return this.toWKTSQL("");	
	}
	
	public String toWKTSQL()
	{
		return this.toWKTSQL( emptyGeography);	
	}

	public String toEWKTSQL()
	{
		String wktTxt = this.toWKTSQL( emptyGeography);	
		if (!wktTxt.equals(emptyGeography))
		{
			wktTxt = "SRID=" + Integer.toString(this.getSrid()) + ";" + wktTxt; 
		}
		return wktTxt;
	}

	public String toWKTSQL(String emptyVal)
	{
		ShapeWriter wtr = ctx.getFormats().getWktWriter();
		StringWriter stringWriter = new StringWriter();
		String buf;
		try
		{
			if (innerShape != null)
			{
				wtr.write(stringWriter, this.innerShape);
				buf = stringWriter.toString();				
				if (buf.equals(emptyGeography) || buf.equals(s4j_emptyGeography) || buf.equals(emptyLine) || buf.equals(emptyPoly)) 
				{
					buf = emptyVal;
				}
			}
			else
			{				
				buf = emptyVal;
			}
			stringWriter.close();
		}
		catch( java.io.IOException ex)
		{
			buf = emptyVal;
		}
		return buf;
	}

	public void fromWKT(String wktString)
	{
		lastErrorCode = 0;
		lastError = "";
		String sridExp = "";
		String sridTxt = "";
		String wkText = "";
		wkText = wktString;
		if (wktString.contains(";")) {
			String[] parts = wktString.split(";", 2);
			sridExp = parts[0];
			wkText = parts[1];
			if (sridExp.contains("=")) {
				String[] sridparts = sridExp.split("=", 2);
				sridTxt = sridparts[1];			
				srid = Integer.parseInt(sridTxt);
			}
			else {
				srid = 4326;
			}
		}
		else
		{
			if (wktString.isEmpty()) 
			{
			  	wkText = emptyGeography;
			}	
			else
			{
				wkText = wktString;
			}
		}
		if (wkText.contains("POLYGON"))
		{
			isJTS = true;
			initJTSContext(); // set context to JTS
		}
		ShapeReader rdr =  ctx.getFormats().getWktReader();
		readShape(rdr, wkText);
		if (lastErrorCode != 0 && wktString.contains(",")) {
			String[] coords = wktString.split(",", 2);
			double dlat = Double.parseDouble(coords[0].trim());
			double dlong = Double.parseDouble(coords[1].trim());
			wkText = "POINT(" + String.format(Locale.ROOT, "%.4f", dlong) + " " + String.format(Locale.ROOT, "%.4f", dlat) + ")";
			rdr =  ctx.getFormats().getWktReader();
			readShape(rdr, wkText);
		}
	}

	public int readShape(ShapeReader rdr, String wkText) 
	{
		lastErrorCode = 0;	
		try {
			innerShape = rdr.read(wkText);
		}
		catch(InvalidShapeException ex) {
			innerShape = null;
			lastErrorCode = 1;
			lastError = ex.toString();
        	}
		catch(ParseException ex) {
			innerShape = null;
			lastErrorCode = 1;
			lastError = ex.toString();
	        }
		catch( java.io.IOException ex) {
			innerShape = null;
			lastErrorCode = 1;
			lastError = ex.toString();
		}
		//catch(org.noggit.JSONParser.ParseException ex)
		//{
		//	innerShape = null;
		//	lastError = ex.toString();
		//	lastErrorCode = 1;
		//}
		return lastErrorCode;
	}

	public String toGeoJSON()
	{
		ShapeWriter wtr = ctx.getFormats().getGeoJsonWriter();
		StringWriter stringWriter = new StringWriter();
		String buf;
		try{
			wtr.write(stringWriter, this.innerShape);
			buf = stringWriter.toString();
			stringWriter.close();
		}
		catch( java.io.IOException ex)
		{
			buf = "";
		}
		return buf;
	}

	public void fromGeoJSON(String geoJSONString)
	{
		lastErrorCode = 0;
		lastError = "";
		if (geoJSONString.contains("Polygon"))
		{
			isJTS = true;
			initJTSContext(); // set context to JTS
		}
		ShapeReader rdr =  ctx.getFormats().getGeoJsonReader();
		readShape(rdr, geoJSONString);
	}

	public static boolean isNullOrEmpty(GXGeospatial geo)
	{
        	return (geo.innerValue() == null || geo.toWKT().equals(""));
	}

	public double distance(GXGeospatial geoto)
	{
		DistanceCalculator cl = ctx.getDistCalc();
		/* works with any shape using getCenter()  */
		Point pointB = geoto.innerValue().getCenter();
		Point pointA = this.innerValue().getCenter();
		double distance = cl.distance(pointA, pointB); /* distance in deg. */
		return (distance * DistanceUtils.DEG_TO_KM * 1000);
	}

	public boolean intersect(GXGeospatial geowith)
	{
		if (this.innerValue() != null && geowith.innerValue() != null) {
			SpatialRelation rel = this.innerValue().relate(geowith.innerValue());
			return (rel.intersects());
		}
		else {
			return false;
		}
	}
	
	@Text
	public String getXML(){
		return toWKT();
	}
	
	 @Text
	public void setXML(String xml){
		fromWKT(xml);
	}

	/* json serialization */
	public Object GetJSONObject()  
	{
		return toJSonString();
	}

 	public String toJSonString() {
 		return this.toWKT();
 	}

    public boolean fromJSonString(String s) {
    	this.fromWKT(s);
    	return (lastErrorCode == 0);
    }

    public boolean fromJSonString(String s, GXBaseCollection<SdtMessages_Message> messages) {
    	this.fromWKT(s);
    	if (lastErrorCode != 0)
    	{
    		com.genexus.GXutil.ErrorToMessages("fromjson error", String.valueOf(lastErrorCode) + " " + lastError, messages);
    		return false;
    	}
    	else {
    		return true;
    	}
    }

}
