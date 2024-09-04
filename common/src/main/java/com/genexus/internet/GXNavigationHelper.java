package com.genexus.internet;
import java.util.*;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

import json.org.json.*;

import java.io.Serializable;

public class GXNavigationHelper implements Serializable
    {
				private static final long serialVersionUID = 2608956804836620190L;    	
    	
        public static String POPUP_LEVEL = "gxPopupLevel";
        public static String CALLED_AS_POPUP = "gxCalledAsPopup";

        private Hashtable<String, Stack<String>> referers;

        public GXNavigationHelper()
        {
            referers = new Hashtable<>();
        }
        
        public String toJSonString(String lvl)
        {
            JSONArray array = new JSONArray();
            if (referers.containsKey(lvl))
            {
            	Stack levelStack = referers.get(lvl);
                for (int i = 0 ; i< levelStack.size(); i++)
                {
                    array.put(levelStack.get(i));
                }
            }
            return array.toString();            
        }

        public void pushUrl(String url)
        {
            if (url.indexOf(CALLED_AS_POPUP) != -1)
                return;
            String popupLevel = getUrlPopupLevel(url);
            if (!referers.containsKey(popupLevel))
            {
                Stack<String> stack = new Stack<>();
                stack.push(url);
                referers.put(popupLevel, stack);
            }
            else
            {
                Stack<String> stack = referers.get(popupLevel);
                stack.push(url);
            }
        }

        public void popUrl(String url)
        {
            if (url.indexOf(CALLED_AS_POPUP) != -1)
                return;
            String popupLevel = getUrlPopupLevel(url);
            if (referers.containsKey(popupLevel))
            {
                Stack stack = referers.get(popupLevel);
                if (stack.size() > 0)
                {
                    stack.pop();
                }
            }
        }

        public String peekUrl(String url)
        {
            if (url.indexOf(CALLED_AS_POPUP) != -1)
                return "";
            String popupLevel = getUrlPopupLevel(url);
            if (referers.containsKey(popupLevel))
            {
                Stack stack = referers.get(popupLevel);
                if (stack.size() > 0)
                {
                    return (String)stack.peek();
                }
            }
            return "";
        }
        
        public String getRefererUrl(String url)
        {
            if (url.indexOf(CALLED_AS_POPUP) != -1)
                return "";
            String popupLevel = getUrlPopupLevel(url);
            if (referers.containsKey(popupLevel))
            {
                Stack<String> stack = referers.get(popupLevel);
                if (stack.size() > 1)
                {
		    String topUrl = stack.pop();
                    String referer = stack.peek();
                    stack.push(topUrl);
                    return referer;
                }
            }
            return "";
        }

        public int count()
        {
            return referers.size();
        }
        
        public void deleteStack(String popupLevel)
        {
            if (referers.containsKey(popupLevel))
                referers.remove(popupLevel);
        }

        public String getUrlPopupLevel(String url)
        {
            url = SpecificImplementation.GXutil.URLDecode(url);
            String popupLevel = "-1";
            if (url != null)
            {
                int pIdx = url.indexOf(POPUP_LEVEL);
                if (pIdx != -1)
                {
                    int eqIdx = url.indexOf("=", pIdx);
                    if (eqIdx != -1)
                    {
                        int cIdx = url.indexOf(";", eqIdx);
                        if (cIdx > eqIdx)
                        {
                            try
                            {
                                popupLevel = url.substring(eqIdx+1, cIdx);
                            }
                            catch(Exception e)
                            {
                                popupLevel = "-1";
                            }
                        }
                    }
                }
            }
            return popupLevel;
        }
    }
