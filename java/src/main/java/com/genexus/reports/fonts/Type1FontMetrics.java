
package com.genexus.reports.fonts;

/* Esta clase provee de las m�tricas de los fonts Type1 utilizados por Adobe
   Estas m�tricas son utilizadas para obtener el size de un String, etc

   Para los fonts CJK por ahora utilizamos widths constantes (monospaced)
   Para utilizar un font CJK se debe realizar un font substitution en el PDFReport.ini
   indicando cual font se va a cambiar por uno de los CJK
   Los fonts CJK v�lidos tienen los siguientes alias: (Acrobat 6)
	 TraditionalChinese
	 SimplifiedChinese
	 Japanese
	 Japanese2
	 Korean
   Para Acrobat 5 algunos fonts Type1 son diferentes. Lo alias son estos
	 TraditionalChineseAcro5
	 SimplifiedChineseAcro5
	 KoreanAcro5
*/
public class Type1FontMetrics
{
   public static String [][] CJKNames = {
											// Adobe Acrobat Reader 6
											{"SimplifiedChinese", "AdobeSongStd-Light-Acro"},
											{"TraditionalChinese", "AdobeMingStd-Light-Acro"},
											{"Japanese", "KozMinPro-Regular-Acro"},
											{"Japanese2", "KozGoPro-Medium-Acro"},
											{"Korean", "AdobeMyungjoStd-Medium-Acro"},
											// Adobe Acrobat Reader 5
											{"SimplifiedChineseAcro5", "STSongStd-Light-Acro"},
											{"TraditionalChineseAcro5", "MSungStd-Light-Acro"},
											{"KoreanAcro5", "HYSMyeongJoStd-Medium-Acro"}
										   };
}
