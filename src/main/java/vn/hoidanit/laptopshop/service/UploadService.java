package vn.hoidanit.laptopshop.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;

@Service
public class UploadService {

    private final ServletContext servletContext;

    public UploadService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        if(file.isEmpty()) {
            return "";
        }
        
        // absolute path
        String rootPath = this.servletContext.getRealPath("/resources/images");

        String finalName = "";
        try {
            // Lưu dưới dạng binary
            byte[] bytes = file.getBytes();

            // Tìm địa chỉ chính xác để lưu file
            // File.separator = "/"
            // targetFolder: vị trí chính xác để lưu file
            // VD: user sẽ có vị trí lưu file ảnh khác với product
            File dir = new File(rootPath + File.separator + targetFolder);
            if (!dir.exists())
                dir.mkdirs(); // make directory

            // Create name file on server
            // dir.getAbsolutePath(): lấy đường link của thư mục + "/" + thời gian lưu hiện
            // tại (VD: Sẽ lưu vào file có đường dẫn
            // "/resources/images/avatar/System.currentTimeMillis() + "-" +
            // file.getOriginalFilename()" )
            // (tránh upload cùng 1 tên file vì file sau ghi đè file trước) + tên file
            finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);

            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(serverFile));
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return finalName;
    }
}
