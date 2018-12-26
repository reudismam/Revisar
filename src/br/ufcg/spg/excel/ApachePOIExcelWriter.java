package br.ufcg.spg.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterFormatter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ApachePOIExcelWriter {

  public static void main(String[] args) throws IOException, InvalidFormatException {
    try {
      // create .xls and create a worksheet.
      FileOutputStream fos = new FileOutputStream("data.xls");
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet worksheet = workbook.createSheet("My Worksheet");

      // Create ROW-1
      HSSFRow row1 = worksheet.createRow((short) 0);

      // Create COL-A from ROW-1 and set data
      HSSFCell cellA1 = row1.createCell((short) 0);
      cellA1.setCellValue("Sno");
      HSSFCellStyle cellStyle = workbook.createCellStyle();
      cellStyle.setFillForegroundColor(HSSFColor.GOLD.index);
      // cellStyle.setFillPattern(HSSFCellStyle);
      cellA1.setCellStyle(cellStyle);

      // Create COL-B from row-1 and set data
      HSSFCell cellB1 = row1.createCell((short) 1);
      cellB1.setCellValue("Name");
      cellStyle = workbook.createCellStyle();
      cellStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
      // cellStyle.setFillPattern(0);
      cellB1.setCellStyle(cellStyle);

      // Create COL-C from row-1 and set data
      HSSFCell cellC1 = row1.createCell((short) 2);
      cellC1.setCellValue(true);

      // Create COL-D from row-1 and set data
      HSSFCell cellD1 = row1.createCell((short) 3);
      cellD1.setCellValue(new Date());
      cellStyle = workbook.createCellStyle();
      cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
      cellD1.setCellStyle(cellStyle);

      // Save the workbook in .xls file
      workbook.write(fos);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void save(List<QuickFix> qfs) 
      throws IOException, InvalidFormatException {
    try {
      // create .xls and create a worksheet.
      FileOutputStream fos = new FileOutputStream("data.xls");
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet worksheet = workbook.createSheet("My Worksheet");

      for (int i = 0; i < qfs.size(); i++) {
        // Create ROW-1
        HSSFRow row1 = worksheet.createRow((short) i);

        // Create COL-A from ROW-1 and set data
        HSSFCell cellA1 = row1.createCell((short) 0);
        cellA1.setCellValue(qfs.get(i).getId());
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.GOLD.index);
        // cellStyle.setFillPattern(HSSFCellStyle);
        cellA1.setCellStyle(cellStyle);

        // Create COL-B from row-1 and set data
        HSSFCell cellB1 = row1.createCell((short) 1);
        Cluster c = qfs.get(i).getCluster();
        String result = ClusterFormatter.getInstance().formatStringNodes(c.getNodes());
        String [] lines = result.split("\n");
        cellB1.setCellValue(lines[3]);
        cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        // cellStyle.setFillPattern(0);
        cellB1.setCellStyle(cellStyle);

        /*
        // Create COL-C from row-1 and set data
        HSSFCell cellC1 = row1.createCell((short) 2);
        cellC1.setCellValue(true);

        // Create COL-D from row-1 and set data
        HSSFCell cellD1 = row1.createCell((short) 3);
        cellD1.setCellValue(new Date());
        cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
        cellD1.setCellStyle(cellStyle);*/
      }
      // Save the workbook in .xls file
      workbook.write(fos);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
