package com.aidotnet.erp.common.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

    public <T> void importExcel(InputStream inputStream, Class<T> clazz, Consumer<List<T>> batchConsumer, int batchSize) {
        EasyExcel.read(inputStream, clazz, new PageReadListener<>(batchConsumer, batchSize))
                .sheet()
                .doRead();
        log.debug("Excel import completed: class={}", clazz.getSimpleName());
    }

    public <T> void importExcel(InputStream inputStream, Class<T> clazz, Consumer<List<T>> batchConsumer) {
        importExcel(inputStream, clazz, batchConsumer, 500);
    }

    public <T> List<T> readAll(InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream).head(clazz).sheet().doReadSync();
    }

    public <T> void exportExcel(HttpServletResponse response, String fileName, Class<T> clazz, List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(fileName)
                .doWrite(data);
        log.debug("Excel export completed: file={}, rows={}", fileName, data.size());
    }

    public <T> byte[] exportToBytes(String sheetName, Class<T> clazz, List<T> data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
        return outputStream.toByteArray();
    }
}
