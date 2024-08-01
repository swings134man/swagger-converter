package com.lucas.converter.util;

import com.lucas.converter.config.ExcelConstant;
import com.lucas.converter.config.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/************
 * @info : Swagger To Docs Convert util
 * @name : ConvertUtil
 * @date : 2024. 7. 31. 오후 12:33
 * @author : SeokJun Kang(swings134@gmail.com)
 * @version : 1.0.0
 * @Description : 
 ************/
@Slf4j
public class ConvertUtil {
    //    String URL = "http://localhost:8080/v2/api-docs";


    public void toExcel(String url, HttpServletResponse response) throws Exception {
        log.info("---------------------- Swagger API To Excel Start ----------------------");

        // 1. Get Swagger API Docs
        String jsonSwagger = readUrl(url);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonSwagger);
        JSONObject paths = (JSONObject)json.get("paths");
        HashMap<String,JSONObject> apiSet = (HashMap<String, JSONObject>) paths;


        // 2. Excel Config Set
        Map<String, Object> excelData = new HashMap<>();

        excelData.put(ExcelConstant.EX_HEADER_BG_COLOR, IndexedColors.YELLOW);
        LinkedHashMap<String, String> headerList = new LinkedHashMap<String, String>();
            headerList.put("S.No", "No");
            headerList.put("API_URL", "API_URL");
            headerList.put("CONTROLLER", "Controller");
            headerList.put("DESCRIPTION", "Description");
            headerList.put("RESPONSE_TYPE", "Response_type");

        Map<String, Object> tempData = new HashMap<>();
        tempData.put(ExcelConstant.EX_HEADER_LIST, headerList);

        // FIXME:Need to DateFrom Change
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String fileName = "Swagger_API_DOCS";
        tempData.put(ExcelConstant.EX_FILE_NAME, fileName);
        excelData.put(ExcelConstant.EX_EXCEL_DATA, tempData);

        ExcelUtil.downloadExcelToSxssf(excelData, response, (sheet) -> {
            AtomicInteger rowIdx = new AtomicInteger();

            // 3. Excel Data Set
            for (Map.Entry<String, JSONObject> e : apiSet.entrySet()) {

                String key = e.getKey();
                JSONObject api = e.getValue();
                System.out.println();
                JSONObject method = (JSONObject) api.get("post");
                if(method == null){
                    method = (JSONObject) api.get("get");
                }

                if(method == null){
                    method = (JSONObject) api.get("delete");
                }

                if(method == null){
                    method = (JSONObject) api.get("put");
                }

                if(method == null){
                    method = (JSONObject) api.get("patch");
                }

                String controller = ((JSONArray)method.get("tags")).toString();
                String description = (String) method.get("summary");

                JSONObject schema = (JSONObject)(((JSONObject)((JSONObject)method.get("responses")).get("200")).get("schema"));
                String responses = null;
                if(schema != null){

                    responses = (String)schema.get("$ref");

                    if(responses == null){

                        if(((String)schema.get("type")).equals("array"))
                            responses = (String)((JSONObject)schema.get("items")).get("$ref");
                        else if(((String)schema.get("type")).equals("object"))
                            responses = (String)((JSONObject)schema.get("additionalProperties")).get("type");
                    }

                    if(responses !=null){
                        String[]  res= responses.split("/");

                        responses = res[res.length - 1];
                    }
                }
                System.out.println("api:" + key + "\tcontroller:" + controller + "\tdescription:" + description + "\tresponses:" + responses);

//            row = sheet.createRow(rownum++);
//            objArr = new String[]{key,controller, description, responses};
//            cellnum = 0;
//
//            Cell cell = row.createCell(cellnum++);
//            cell.setCellValue(++count);
//
//            for (String obj : objArr)
//            {
//                cell = row.createCell(cellnum++);
//                cell.setCellValue((String)obj);
//
//            }

                // 데이터 입력
                Row row = sheet.createRow(rowIdx.incrementAndGet());
                int cellCnt = 0;
                row.createCell(cellCnt++).setCellValue(rowIdx.get());
                row.createCell(cellCnt++).setCellValue(key);
                row.createCell(cellCnt++).setCellValue(controller);
                row.createCell(cellCnt++).setCellValue(description);
                row.createCell(cellCnt++).setCellValue(responses);

            }

//            for (Board item : page.getContent()) {
//                // 데이터 입력
//                Row row = sheet.createRow(rowIdx.incrementAndGet());
//                int cellCnt = 0;
//                row.createCell(cellCnt++).setCellValue(item.getId());
//                row.createCell(cellCnt++).setCellValue(item.getTitle());
//                row.createCell(cellCnt++).setCellValue(item.getWriter());
//                row.createCell(cellCnt++).setCellValue(item.getContent());
//                row.createCell(cellCnt++).setCellValue(item.getCreatedDate().toString());
//                row.createCell(cellCnt++).setCellValue(item.getModifiedDate().toString());
//                //          row.createCell(cellCnt++).setCellValue(formatSafely(LocalDateTime.parse(item.getCreatedDate()), formatter));
//                //          row.createCell(cellCnt++).setCellValue(formatSafely(LocalDateTime.parse(item.getModifiedDate()), formatter));
//            }
        });

        log.info("---------------------- Swagger API To Excel END ----------------------");
    }



    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
