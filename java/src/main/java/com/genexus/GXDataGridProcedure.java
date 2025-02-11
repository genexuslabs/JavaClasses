package com.genexus;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.webpanels.HttpContextWeb;

public abstract class GXDataGridProcedure extends GXProcedure{

	private static final ILogger logger = LogManager.getLogger(GXDataGridProcedure.class);

	private static final String HAS_NEXT_PAGE = "HasNextPage";
	private static final String RECORD_COUNT = "RecordCount";
	private static final String RECORD_COUNT_SUPPORTED = "RecordCountSupported";
	private long totalRecordCount = -1;

	public GXDataGridProcedure(int remoteHandle, ModelContext context, String location) {
		super(false, remoteHandle, context, location);
	}

	protected long recordCount(){
		return -1;
	}

	protected boolean recordCountSupported(){
		return true;
	}

	protected void setPaginationHeaders(boolean hasNextPage) {
		try {
			setHasNextPageHeader(hasNextPage);
			setRecordCountSupportedHeader();
		}
		catch (Exception ex) {
			logger.warn("A processing error occurred while setting pagination headers", ex);
		}
	}

	private void setRecordCountSupportedHeader() {
		if (!recordCountSupported()) {
			logger.debug("Adding '{RECORD_COUNT_SUPPORTED}' header");
			((HttpContextWeb) context.getHttpContext()).setHeader(RECORD_COUNT_SUPPORTED, String.valueOf(false));
		}
	}

	private void setHasNextPageHeader(boolean hasNextPage) {
		((HttpContextWeb) context.getHttpContext()).setHeader(HAS_NEXT_PAGE, String.valueOf(hasNextPage));
	}

	private void setRecordCountHeader() {
		boolean recordCountHeaderRequired = false;
		boolean setHeader = false;
		if (context.getHttpContext() != null) {
			recordCountHeaderRequired = !context.getHttpContext().getHeader(RECORD_COUNT).isEmpty();
		}
		if (totalRecordCount != -1) {
			setHeader = true;
		}
		else if (recordCountHeaderRequired) {
			totalRecordCount = recordCount();
			setHeader = true;
		}
		if (setHeader) {
			logger.debug("Adding '{RECORD_COUNT}' header: " + totalRecordCount);
			((HttpContextWeb) context.getHttpContext()).setHeader(RECORD_COUNT, String.valueOf(totalRecordCount));
		}
	}

	protected long getPaginationStart(long start, long count) {
		if (start < 0) { //last page
			totalRecordCount = recordCount();
			long lastPageRecords = totalRecordCount % count;
			if (lastPageRecords == 0)
				start = totalRecordCount - count;
			else
				start = totalRecordCount - lastPageRecords;
		}
		setRecordCountHeader();
		return start;
	}
}