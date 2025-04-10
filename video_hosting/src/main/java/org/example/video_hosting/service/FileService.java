package org.example.video_hosting.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.File;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.response.ResFile;
import org.example.video_hosting.repository.FileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    private static final Path root= Paths.get("src/main/resources");

//    private static final Path root = Paths.get("/root");

    public ApiResponse<?> saveFile(MultipartFile file) {
        String directory = determineFileType(file);
        if (directory == null) {
            return ApiResponse.error(ResponseError.notFound("Fayl yuklash uchun papka"));
        }

        long timestamp = System.currentTimeMillis();
        Path targetPath = root.resolve(directory + "/" + timestamp + "-" + file.getOriginalFilename());

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            File storedFile = new File();
            storedFile.setFileName(file.getOriginalFilename());
            storedFile.setFilepath(targetPath.toString());
            storedFile.setContentType(file.getContentType());
            storedFile.setSize(file.getSize());

            File savedFile = fileRepository.save(storedFile);
            return ApiResponse.ok(ResponseSuccess.fetched("File"),savedFile.getId());
        } catch (IOException e) {
            throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.notFound(e.getMessage()))));
        }
    }

    public ResFile loadFileAsResource(Long id) {
        try {
            Optional<File> fileOptional = fileRepository.findById(id);
            if (fileOptional.isPresent()) {
                File file = fileOptional.get();
                if (file.getFilepath() == null || file.getFileName() == null || file.getContentType() == null) {
                    throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.defaultError("File data is missing"))));
                }
                java.io.File storedFile = new java.io.File(file.getFilepath());
                if (!storedFile.exists()) {
                    throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.defaultError("File not found"))));
                }
                Resource resource = new UrlResource(storedFile.toURI());

                ResFile resFile = new ResFile();
                resFile.setFillName(file.getFileName());
                resFile.setResource(resource);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(file.getContentType()));
                headers.setContentLength(file.getSize());

                resFile.setHeaders(headers);
                return resFile;
            }
            throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.notFound("File"))));
        } catch (IOException e) {
            throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.defaultError(e.getMessage()))));
        }
    }

    public ApiResponse<?> updateFile(Long id, MultipartFile file) {
        try {
            Optional<File> existingFileOptional = fileRepository.findById(id);
            if (existingFileOptional.isPresent()) {
                File fileToUpdate = existingFileOptional.get();
                Path oldFilePath = Paths.get(fileToUpdate.getFilepath());
                Files.deleteIfExists(oldFilePath);

                String filename = file.getOriginalFilename();
                String directory = determineFileType(file);
                if (directory == null) {
                    return ApiResponse.error(ResponseError.notFound("Fayl yuklash uchun papka"));
                }

                long timestamp = System.currentTimeMillis();
                Path uploadPath = root.resolve(directory + "/" + timestamp + "-" + file.getOriginalFilename());
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                if (filename != null) {
                    Path targetPath = uploadPath.resolve(filename);
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                    fileToUpdate.setFileName(filename);
                    fileToUpdate.setFilepath(targetPath.toString());
                    fileToUpdate.setContentType(file.getContentType());

                    File updatedFile = fileRepository.save(fileToUpdate);
                    return ApiResponse.ok(ResponseSuccess.fetched("File"),updatedFile.getId());
                } else {
                    return ApiResponse.error(ResponseError.notFound("File name is missing"));
                }
            } else {
                throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.notFound("File not found"))));
            }
        } catch (IOException e) {
            throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.defaultError("File operation failed"))));
        }
    }

    public ApiResponse<?> deleteFile(Long id) {
        try {
            Optional<File> fileOptional = fileRepository.findById(id);
            if (fileOptional.isPresent()) {
                File fileToDelete = fileOptional.get();
                Path filePath = Paths.get(fileToDelete.getFilepath());
                Files.deleteIfExists(filePath);
                fileRepository.delete(fileToDelete);
                return ApiResponse.ok(ResponseSuccess.deleted("File"));
            } else {
                throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.notFound("File not found"))));
            }
        } catch (IOException e) {
            throw new NotFoundException(String.valueOf(ApiResponse.error(ResponseError.defaultError("Error deleting file"))));
        }
    }

    private String determineFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null) {
            if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".webp")
                    || filename.endsWith(".PNG") || filename.endsWith(".JPG") || filename.endsWith(".JPEG") || filename.endsWith(".WEBP")) {
                return "img";
            } else if (isSupportedFileType(filename)) {
                return "files";
            }
        }
        return null;
    }

    private boolean isSupportedFileType(String filename) {
        return filename.endsWith(".pdf") || filename.endsWith(".docx") || filename.endsWith(".pptx") || filename.endsWith(".zip")
                || filename.endsWith(".PDF") || filename.endsWith(".DOCX") || filename.endsWith(".PPTX") || filename.endsWith(".ZIP")
                || filename.endsWith(".mp4") || filename.endsWith(".mkv") || filename.endsWith(".avi") || filename.endsWith(".mov");
    }
}
