package com.genexus.msoffice.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.msoffice.excel.exception.ExcelDocumentNotSupported;
import com.genexus.msoffice.excel.exception.ExcelException;
import com.genexus.msoffice.excel.exception.ExcelTemplateNotFoundException;
import com.genexus.msoffice.excel.poi.xssf.ExcelCells;
import com.genexus.msoffice.excel.poi.xssf.ExcelWorksheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelSpreadsheetGXWrapper implements IGXError {
    private static final ILogger logger = LogManager.getLogger(ExcelSpreadsheetGXWrapper.class);
    private int _errCode;
    private String _errDescription = "";
    private IExcelWorksheet _currentWorksheet;
    private List<IExcelWorksheet> _worksheets;
    private IExcelSpreadsheet _document;
    private Boolean _isReadonly = false;
    private Boolean _autofit = false;
    private final String DEFAULT_SHEET_NAME = "Sheet";

    public Boolean getAutofit() {
        return _autofit;
    }

    public void setAutofit(Boolean _autofit) {
        this._autofit = _autofit;
        if (_document != null) {
            _document.setAutofit(_autofit);
        }
    }

    private Boolean initialize() {

        return initialize(DEFAULT_SHEET_NAME);
    }

    private Boolean initialize(String defaultSheetName) {
        boolean ok = selectFirstDefaultSheet(defaultSheetName);
        if (!ok) {
            setErrCod((short) 1);
            setErrDes("Could not get/set first Sheet on Document");
        } else {
            setErrCod((short) 0);
            setErrDes("");
        }
        return ok;
    }

    public boolean open(String filePath) {
        return open(filePath, "");
    }

    public boolean open(String filePath, String template) {
        try {
            logger.debug("Opening Excel file: " + filePath);
            _document = ExcelFactory.create((IGXError) this, filePath, template);
            if (_autofit) {
                _document.setAutofit(_autofit);
            }
        } catch (ExcelTemplateNotFoundException e) {
            this.setError("Excel Template file not found", e);
        } catch (IOException e) {
            logger.error("Excel File could not be loaded", e);
			this.setError(ErrorCodes.FILE_EXCEPTION, "Could not open file");
        } catch (ExcelDocumentNotSupported e) {
            this.setError("Excel file extension not supported", e);
        }
        return _document != null;
    }

    public Boolean save() {
        boolean ok = false;
        if (initialize()) {
            try {
                ok = _document.save();
                if (!ok){
                    this.setError(ErrorCodes.FILE_NOT_SAVED, "Excel File could not be saved");
                }
            } catch (ExcelException e) {
                this.setError("Excel File could not be saved", e);
            }
        }

        return ok;
    }

    public Boolean saveAs(String newFileName, String password) {
        return saveAsImpl(newFileName, password);
    }

    public Boolean saveAs(String newFileName) {
        return saveAsImpl(newFileName, null);
    }

    private Boolean saveAsImpl(String newFileName, String password) {
        boolean ok = true;
        if (initialize()) {
            try {
                _document.saveAs(newFileName);

            } catch (ExcelException e) {
                this.setError(e);
                ok = false;
            }
        }
        return ok;
    }

    public ExcelCells getCell(int rowIdx, int colIdx) {
        if (initialize()) {
            try {
                return (ExcelCells) _document.getCell(_currentWorksheet, rowIdx, colIdx);
            } catch (ExcelException e) {
                this.setError(e);
            }
        }
        return null;
    }

    public void setError(ExcelException e) {
        this.setError(e.get_errorCode(), e.get_errDsc());
        logger.error(e.get_errDsc(), e);
    }

    public void setError(String errorMsg, ExcelException e) {
        this.setError(e.get_errorCode(), e.get_errDsc());
        logger.error(errorMsg);
    }

    public ExcelCells getCells(int rowIdx, int colIdx, int rowCount, int colCount) {
        if (initialize()) {
            try {
                return (ExcelCells) _document.getCells(_currentWorksheet, rowIdx, colIdx, rowCount, colCount);
            } catch (ExcelException e) {
                this.setError(e);
            }
        }
        return null;
    }

    public Boolean setCurrentWorksheet(int sheetIdx) {
        int zeroIndexSheet = sheetIdx - 1;
        if (zeroIndexSheet >= 0 && initialize() && _document.getWorksheets().size() > zeroIndexSheet) {
            _currentWorksheet = _document.getWorksheets().get(zeroIndexSheet);
            if (_currentWorksheet != null) {
                _document.setActiveWorkSheet(_currentWorksheet.getName());
            }
            return true;
        }
        return false;
    }

    public Boolean setCurrentWorksheetByName(String sheetName) {
        if (initialize()) {
            ExcelWorksheet ws = _document.getWorkSheet(sheetName);
            if (ws != null) {
                _currentWorksheet = ws;
                _document.setActiveWorkSheet(sheetName);
                return true;
            }
        }
        return false;
    }

    public Boolean insertRow(int rowIdx, int rowCount) {
        if (initialize()) {
            return _document.insertRow(_currentWorksheet, rowIdx - 1, rowCount);
        }
        return false;
    }

    public Boolean insertColumn(int colIdx, int colCount) {
        //throw new Exception("NotImplemented");
        return false;
        /*
         * if (isOK()) { //return _document.(_currentWorksheet, colIdx, colCount); }
         * return false;
         */
    }

    public Boolean deleteRow(int rowIdx) {
        if (initialize()) {
            return _document.deleteRow(_currentWorksheet, rowIdx - 1);
        }
        return false;
    }

    public Boolean deleteColumn(int colIdx) {
        if (initialize()) {
            return _document.deleteColumn(_currentWorksheet, colIdx - 1);
        }
        return false;
    }

    public Boolean insertSheet(String sheetName) {
    	try {
			return _document != null && _document.insertWorksheet(sheetName, 0) && initialize(sheetName);
		}
    	catch (ExcelException e){
    		this.setError("Could not insert new sheet", e);
		}
		return false;
    }

    public Boolean cloneSheet(String sheetName, String newSheetName) {
        if (initialize()) {
            try {
                return _document.cloneSheet(sheetName, newSheetName);
            } catch (ExcelException e) {
                this.setError(2, e.getMessage());
            }
        }
        return false;
    }

    public Boolean toggleColumn(int colIdx, Boolean visible) {
        if (initialize()) {
            return _document.toggleColumn(_currentWorksheet, colIdx - 1, visible);
        }
        return false;
    }

    public Boolean toggleRow(int rowIdx, Boolean visible) {
        if (initialize()) {
            return _document.toggleRow(_currentWorksheet, rowIdx - 1, visible);
        }
        return false;
    }

    public Boolean deleteSheet(String sheetName) {
        if (initialize()) {
            ExcelWorksheet ws = _document.getWorkSheet(sheetName);
            if (ws != null)
                return _document.deleteSheet(sheetName);
        }
        setError(2, "Sheet not found");
        return false;
    }

    public Boolean deleteSheet(int sheetIdx) {
        if (initialize()) {
            if (_document.getWorksheets().size() >= sheetIdx)
                return _document.deleteSheet(sheetIdx - 1);
        }
        setError(2, "Sheet not found");
        return false;
    }


    public Boolean close() {
        if (initialize()) {
            try {
                _document.close();
            } catch (ExcelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        _currentWorksheet = null;
        _document = null;
        return true;
    }

    private void setError(int error, String description) {
        _errCode = error;
        _errDescription = description;
    }

    public int getErrCode() {
        return _errCode;
    }

    public String getErrDescription() {
        return _errDescription;
    }

    public ExcelWorksheet getCurrentWorksheet() {
        if (initialize()) {
            return (ExcelWorksheet) _currentWorksheet;
        }
        return null;
    }

    public List<ExcelWorksheet> getWorksheets() {
        if (initialize())
            return _document.getWorksheets();
        else
            return new ArrayList<ExcelWorksheet>();
    }

    private boolean selectFirstDefaultSheet(String sheetName) {
        if (_document != null) {
            int sheetsCount = _document.getWorksheets().size();
            if (sheetsCount == 0 && _isReadonly) {
                return true;
            }
            if (sheetsCount == 0) {
            	try {
					_document.insertWorksheet(sheetName, 0);
				}
            	catch (ExcelException e) {}
            }
            if (_currentWorksheet == null)
                _currentWorksheet = _document.getWorksheets().get(0);
        }
        return _currentWorksheet != null;
    }

    public void setColumnWidth(int colIdx, int width) {
        if (colIdx > 0 && initialize()) {
            _document.setColumnWidth(_currentWorksheet, colIdx, width);
        }
    }

    public void setRowHeight(int rowIdx, int height) {
        if (rowIdx > 0 && initialize()) {
            _document.setRowHeight(_currentWorksheet, rowIdx, height);
        }
    }

    @Override
    public void setErrCod(short arg0) {
        _errCode = arg0;
    }

    @Override
    public void setErrDes(String arg0) {
        _errDescription = arg0;
    }

	public XSSFWorkbook getUnderlyingObject(){
		return _document.getUnderlyingObject();

	}
}
