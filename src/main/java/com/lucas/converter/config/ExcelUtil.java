package com.lucas.converter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ExcelUtil {

    /**
     * Sxssf bulk Stream Download
     * @param model
     * @param response
     * @param addRow
     */
    public static void downloadExcelToSxssf(Map<String, Object> model, HttpServletResponse response, AddRowToSXSSF addRow) {
        try {
            // Retrieve entered information
            Map<String, Object> excelData = (Map<String, Object>) model.get(ExcelConstant.EX_EXCEL_DATA);

            // Get the file name to use when downloading
            String rawFileName = (String) model.get(ExcelConstant.EX_FILE_NAME);
            String fileName = "";
            if (StringUtils.isNotEmpty(rawFileName)) {
                rawFileName = createFileName(rawFileName) + "x";
//                setFileNameToResponse(request, response, fileName);
            } else {
                rawFileName = ExcelConstant.EX_EXCEL_DATA;
            }

            // Create a workbook
            SXSSFWorkbook workbook = new SXSSFWorkbook(100);
            // Create a new sheet in the workbook
            SXSSFSheet sheet = workbook.createSheet(rawFileName);

            // Set the Header Background Color
            IndexedColors headerBgColor = IndexedColors.YELLOW;
            if (excelData.containsKey(ExcelConstant.EX_HEADER_BG_COLOR)) {
                headerBgColor = (IndexedColors) excelData.get(ExcelConstant.EX_HEADER_BG_COLOR);
            }

            // Set the Header Style
            CellStyle headerStyle = workbook.createCellStyle();
//            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFillPattern(FillPatternType.forInt(FillPatternType.SOLID_FOREGROUND.ordinal()));
            headerStyle.setFillForegroundColor(headerBgColor.getIndex());

            // Get the header Information
            LinkedHashMap<String, String> headerList = (LinkedHashMap<String, String>) excelData.get(ExcelConstant.EX_HEADER_LIST);

            // Create a header row
            Row headerRow = sheet.createRow(0);
            Iterator<String> keys = headerList.keySet().iterator();
            int headerCnt = 0;
            while (keys.hasNext()) {
                String headerKey = keys.next();
                Cell headerCell = headerRow.createCell(headerCnt);
                headerCell.setCellValue(headerList.get(headerKey));
                headerCell.setCellStyle(headerStyle);
                headerCnt++;
            }

            // Get the data list
            addRow.row(sheet);

            response.setContentType(ExcelConstant.EX_CONTENT_TYPE);

            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);

            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }finally {
            log.info("Excel Download End");
        }
    }

    private static void setFileNameToResponse(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.indexOf("MSIE 5.5") >= 0) {
            response.setContentType("doesn/matter");
            response.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        }
        response.setHeader("Content-Transfer-Encoding", "binary");

    }

    private static String createFileName(String fname) throws UnsupportedEncodingException {
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String rs = new StringBuilder(fname)
                .append("_")
                .append(fileFormat.format(new Date()))
                .append(".xls")
                .toString();
        return URLEncoder.encode(rs, "UTF-8");
    }

    @FunctionalInterface
    public interface AddRow {
        void row(HSSFSheet sheet);
    }

    @FunctionalInterface
    public interface AddRowToXSSF {
        void row(XSSFSheet sheet);
    }
    @FunctionalInterface
    public interface AddRowToSXSSF {
        void row(SXSSFSheet sheet);
    }
}
