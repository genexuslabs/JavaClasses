package com.genexus.webpanels;

import java.util.Iterator;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.internet.IGxJSONAble;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;


public class GXWebRow implements IGxJSONAble
{
    ModelContext context;
    GXWebGrid _parentGrid;
    JSONObject _ThisRow;
    JSONArray _Columns;
    JSONArray _RenderProps;
    JSONObject _Hiddens;
    JSONObject _Grids;
    int _Count;
    JSONArray _Values;
    boolean firstRowAdded;

    public GXWebRow()
    {
        _ThisRow = new JSONObject();
        _Columns = new JSONArray();
        _RenderProps = new JSONArray();
        _Hiddens = new JSONObject();
        _Grids = new JSONObject();
        _Values = new JSONArray();
        _Count = 0;
    }

    public GXWebRow(ModelContext context, GXWebGrid parentGrid)
    {
        this();
        this.context = context;
		this._parentGrid = parentGrid;
    }

    public GXWebGrid getParentGrid()
	{
		return _parentGrid;
	}

    public JSONArray GetColumns()
    {
        return _Columns;
    }

    public void Clear()
    {
        _ThisRow.clear();
        _Columns.clear();
        _RenderProps.clear();
        _Hiddens.clear();
        _Grids.clear();
        _Values.clear();
        _Count = 0;
        firstRowAdded = false;
    }

    public void AddColumnProperties(String controlType, int valueIndex, boolean valueWithProps, Object[] props)
    {
        if (this._parentGrid != null && this._parentGrid.GetWrapped() == 1 && this.context != null)
        {
            if (this._parentGrid.isFreestyle() && controlType.equalsIgnoreCase("table"))
            {
                    this._parentGrid.setWritingTableContent(true);
            }
            if (this._parentGrid.isFreestyle() && controlType.equalsIgnoreCase("row") && !firstRowAdded)
            {
                    this.firstRowAdded = true;
                    return;
            }
			   context.getHttpContext().drawingGrid = true;
            GXWebStdMethods.callMethod(this.context, controlType, props, _parentGrid.getGridName());
			   context.getHttpContext().drawingGrid = false;
            if (!this._parentGrid.isFreestyle())
            {
           		GXWebStdMethods.closeTag(this.context, "cell");
            }
        }
        else
        {
            AddColumnProperties(valueIndex, valueWithProps, props);
        }
    }


	 public Iterator initializePptyIterator()
        {
			Iterator it = null;
            if (this._parentGrid != null && this._parentGrid.GetColsPropsCommon().size() > this._Count )
            {
            	it = ((JSONArray)(this._parentGrid.GetColsPropsCommon().get(_Count))).iterator();
            }
			return it;
        }

	 	public void AddColumnProperties(int valueIndex, boolean valueWithProps, Object[] props)
        {
	 		Iterator it = this.initializePptyIterator();
	 		JSONArray colPropsRev = new JSONArray(); //ColProps Reversed
			Object value = "";
			JSONArray colProps = new JSONArray();
			boolean equal =  it != null && !context.getHttpContext().isAjaxCallMode() ;
            Object current = null;

            for (int i = props.length - 1; i >= 0; i--)
            {
                Object prop = props[i];
				if (IGxJSONAble.class.isAssignableFrom(prop.getClass()))
					prop = ((IGxJSONAble)prop).GetJSONObject();

				if (i != valueIndex)
                {
                    equal = equal && it.hasNext();
                    if (equal)
                        current = it.next();
                    if (!(equal && (current.equals(prop))))
                    {
                        equal = false;
                        try{
                        	colProps.putIndex(0, prop);
                        }catch (JSONException e){}
                        if (it == null)
                            colPropsRev.put(prop);
                    }
                }
                else if (valueWithProps)
                    value = prop;
                else
                {
                	CommonUtil.strReplace(prop.toString(), "'", "\'");
                    _Values.put(prop);
                }
            }
            if (this._parentGrid != null && it == null) // If is the first Row.
            {
                this._parentGrid.GetColsPropsCommon().add(colPropsRev);
            }

            if (valueWithProps)
                colProps.put(value);
            else if (valueIndex < 0)
                _Values.put("");

            _Columns.put(colProps);
			_Count++;

        }


    public void AddRenderProperties(GXWebColumn column)
	{
		_RenderProps.put(column.GetJSONObject());
	}

    public static GXWebRow GetNew()
    {
        return GetNew(null);
    }

    public static GXWebRow GetNew(ModelContext context)
    {
        return GetNew(context, null);
    }

    public static GXWebRow GetNew(ModelContext context, GXWebGrid parentGrid)
    {
        return new GXWebRow(context, parentGrid);
    }

    public void AddGrid(String gridName, GXWebGrid grid)
    {
    	try
        {
            _Grids.put(gridName, grid.GetJSONObject());
        }
        catch (JSONException e) {}
    }

    public void AddHidden(String name, Boolean value)
    {
        AddHidden(name, (Object)value);
    }

    public void AddHidden(String name, String value)
    {
        AddHidden(name, (Object)value);
    }

    public void AddHidden(String name, Object value)
    {
        try
        {
            _Hiddens.put(name, value);
        }
        catch (JSONException e) {}
    }

    public String ToJavascriptSource()
    {
        return GetJSONObject().toString();
    }

    public JSONArray GetValues()
    {
        return _Values;
    }

    public void tojson()
    {
        AddObjectProperty("Props", _Columns);
        if (_RenderProps.length() > 0)
            AddObjectProperty("RenderProps", _RenderProps);
        if (_Hiddens.length() > 0)
            AddObjectProperty("Hiddens", _Hiddens);
    	AddObjectProperty("Grids", _Grids);
        AddObjectProperty("Count", new Integer(_Count));
    }

    public void AddObjectProperty(String name, Object prop)
    {
        try
        {
             _ThisRow.put(name, prop);
        }
        catch (JSONException e) {}
    }

    public Object GetJSONObject(boolean includeState)
    {
		return GetJSONObject();
    }

    public Object GetJSONObject()
    {
        tojson();
        return _ThisRow;
    }

    public void FromJSONObject(IJsonFormattable obj)
    {
    }
}
