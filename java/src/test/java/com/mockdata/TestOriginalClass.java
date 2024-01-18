package com.mockdata ;

import com.genexus.*;

public final  class TestOriginalClass extends GXProcedure {
	public TestOriginalClass( int remoteHandle ) {
		super( remoteHandle , new ModelContext( TestMockData.class ), "" );
	}

	public TestOriginalClass( int remoteHandle, ModelContext context ) {
		super( remoteHandle , context, "" );
	}

	@SuppressWarnings("unchecked")
	public short executeUdp( short aP0, String aP1) {
		TestOriginalClass.this.aP2 = new short[] {0};
		execute_int(aP0, aP1, aP2);
		return aP2[0];
	}

	public void execute( short aP0, String aP1, short[] aP2 ) {
		execute_int(aP0, aP1, aP2);
	}

	private void execute_int( short aP0, String aP1, short[] aP2 ) {
		TestOriginalClass.this.AV8parm1 = aP0;
		TestOriginalClass.this.AV9parm2 = aP1;
		TestOriginalClass.this.AV10parm3 = aP2[0];
		this.aP2 = aP2;
		initialize();
		mockExecute();
	}

	protected String[] getParametersInternalNames( ) {
		return new String[]{"AV8parm1","AV9parm2","AV10parm3"} ;
	}

	protected void privateExecute( ) {
		AV10parm3 = (short)(AV8parm1+2) ;
		System.out.println( AV9parm2 );
		cleanup();
	}

	protected void cleanup( ) {
		this.aP2[0] = TestOriginalClass.this.AV10parm3;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors( ) {
	}

	/* Aggregate/select formulas */
	public void initialize( ) {
		/* GeneXus formulas. */
	}

	private short AV8parm1 ;
	private short AV10parm3 ;
	private String AV9parm2 ;
	private short[] aP2 ;
}

