package uk.org.retep.pdf;

import java.io.File;
import java.util.HashMap;

public class TrueTypeFontCache
{
    private static HashMap<String, String> fontCache = new HashMap<>();
    
    public static String getFontFamilyName(File fontFile, int fontFormat, boolean isCopy)
    {
        String absolutePath = fontFile.getAbsolutePath();
        String cacheKey = "";
        String familyName = "";
		cacheKey = absolutePath + String.valueOf(fontFormat) + String.valueOf(isCopy);
		familyName = getFromCache(cacheKey);
		if (familyName != null)
		{
			return familyName;
		}
		try
		{
			familyName = java.awt.Font.createFont(0, fontFile).getFamily();
		}
		catch (Exception ex)
		{
			System.err.println("TrueTypeFontCache Error: Wrapper not found");
			ex.printStackTrace();
			return null;
		}
        if (cacheKey != null && !cacheKey.equals("") && familyName != null && !familyName.equals(""))
        {
            putToCache(cacheKey, familyName);
        }
        else
        {
            return null;
        }
        return familyName;
    }
    
    private static void putToCache(String cacheKey, String cacheValue)
    {
        synchronized(fontCache)
        {
            fontCache.put(cacheKey, cacheValue);
        }
    }
    
    private static String getFromCache(String cacheKey)
    {
        synchronized(fontCache)
        {
            return fontCache.get(cacheKey);
        }
    }
	
	public static void cleanup()
    {
        fontCache.clear();
        fontCache = null;
    }
}