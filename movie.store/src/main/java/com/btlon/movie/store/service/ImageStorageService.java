package com.btlon.movie.store.service;

import com.btlon.movie.store.web.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageStorageService {
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private final Path directory;

    public ImageStorageService(@Value("${moviestore.upload.directory:uploads/movies}") String directory) {
        Path configured = Path.of(directory);
        // Both documented commands (from repo root or movie.store) use the same upload folder.
        if (!configured.isAbsolute() && Files.isRegularFile(Path.of("movie.store", "pom.xml")))
            configured = Path.of("movie.store").resolve(configured);
        this.directory = configured.toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        // 1. Kiểm tra file được chọn và dung lượng.
        if (file.isEmpty())
            throw new BadRequestException("Vui lòng chọn ảnh.");
        if (file.getSize() > MAX_BYTES)
            throw new BadRequestException("Ảnh tối đa 5 MB.");

        // 2. Xác định đuôi ảnh và tạo tên mới để tránh trùng tên.
        String extension = getExtension(file);
        String filename = UUID.randomUUID() + "." + extension;
        Path imagePath = directory.resolve(filename);

        // 3. Tạo thư mục nếu chưa có, sau đó lưu file.
        Files.createDirectories(directory);
        file.transferTo(imagePath);

        // 4. Trả URL để CMS lưu vào posterUrl của phim.
        return "/api/uploads/images/" + filename;
    }

    public Path getDirectory() {
        return directory;
    }

    private static String getExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            throw new BadRequestException("Không xác định được loại ảnh.");

        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new BadRequestException("Chỉ chấp nhận ảnh JPG, PNG hoặc WebP.");
        };
    }
}
