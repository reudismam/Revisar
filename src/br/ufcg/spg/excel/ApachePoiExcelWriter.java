package br.ufcg.spg.excel;

import br.ufcg.spg.cluster.Cluster;
import br.ufcg.spg.cluster.ClusterFormatter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class ApachePoiExcelWriter {
  /**
   * Save list of quick fixes to file.
   */
  @SuppressWarnings("deprecation")
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
        cellB1.setCellStyle(cellStyle);
      }
      // Save the workbook in .xls file
      workbook.write(fos);
      fos.flush();
      fos.close();
      workbook.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
