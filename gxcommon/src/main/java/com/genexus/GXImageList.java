
package com.genexus;
import java.util.Hashtable;

public class GXImageList
{
	private static Hashtable<String, String> images;

	private static void add(String imageName)
	{
		images.put(imageName, "");
	}

	public static boolean contains(String imageName)
	{
		return images.get(imageName) != null;
	}

	static 
	{
		images = new Hashtable<>();
		add("copy.bmp");
		add("cut.bmp");
		add("first1.bmp");
		add("first2.bmp");
		add("get.bmp");
		add("help.bmp");
		add("last1.bmp");
		add("last2.bmp");
		add("next1.bmp");
		add("next2.bmp");
		add("open.bmp");
		add("paint.bmp");
		add("paste.bmp");
		add("prev1.bmp");
		add("prev2.bmp");
		add("prompt.bmp");
		add("refresh.bmp");
		add("copy.gif");
		add("cut.gif");
		add("first1.gif");
		add("first2.gif");
		add("gxconfirm_add.gif");
		add("gxconfirm_cnf.gif");
		add("gxconfirm_dlt.gif");
		add("gxconfirm_upd.gif");
		add("help.gif");
		add("last1.gif");
		add("last2.gif");
		add("next1.gif");
		add("next2.gif");
		add("open.gif");
		add("paint.gif");
		add("paste.gif");
		add("prev1.gif");
		add("prev2.gif");
		add("all_left.jpg");
		add("all_right.jpg");
		add("error.jpg");
		add("err_as.jpg");
		add("left.jpg");
		add("logochico1.jpg");
		add("right.jpg");
		add("gxjavaicon.jpg");
	}
}