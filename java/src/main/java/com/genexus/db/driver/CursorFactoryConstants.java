

package com.genexus.db.driver;
				
public final class CursorFactoryConstants
{
    /**
     * Constantes usadas para definir IDs unicos para los cursores
     * que no tienen ID en tiempo de generacion, como los ins/upd/del,
     * que usan los de las gxdb++
     */

	public static final int OPERATION_INSERT = 1;
	public static final int OPERATION_UPDATE = 2;
	public static final int OPERATION_DELETE = 3;
	public static final int OPERATION_SELECT = 4;
	public static final int MAX_CURSORNUM    = 10000;

	/**
	 *  Calcula el ID de un cursor dada la tabla / operacion.
	 */

	public static String calculateCursorId(int tableId, int operationId)
	{
		return "_" + (operationId *  MAX_CURSORNUM + tableId);
	}
}