package org.example.video_hosting.service;

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
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private static final Path root = Paths.get("src/main/resources");

    public ApiResponse<?> saveFile(MultipartFile file) {
        String directory = determineFileType(file);
        if (directory == null) {
            return ApiResponse.error(ResponseError.notFound("Fayl yuklash uchun papka"));
        }

        long timestamp = System.currentTimeMillis();
        String filename = timestamp + "-" + file.getOriginalFilename();
        Path dirPath = root.resolve(directory);
        Path targetPath = dirPath.resolve(filename);

        try {
            Files.createDirectories(dirPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            File storedFile = new File();
            storedFile.setFileName(file.getOriginalFilename());
            storedFile.setFilepath(targetPath.toString());
            storedFile.setContentType(file.getContentType());
            storedFile.setSize(file.getSize());

            File savedFile = fileRepository.save(storedFile);
            return ApiResponse.ok(ResponseSuccess.fetched("File"), savedFile.getId());
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResFile loadFileAsResource(Long id) {
        try {
            Optional<File> fileOptional = fileRepository.findById(id);
            if (fileOptional.isPresent()) {
                File file = fileOptional.get();
                java.io.File storedFile = new java.io.File(file.getFilepath());
                if (!storedFile.exists()) {
                    throw new ResponseStatusException(NOT_FOUND, "File not found");
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
            throw new ResponseStatusException(NOT_FOUND, "File not found");
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ApiResponse<?> updateFile(Long id, MultipartFile file) {
        try {
            Optional<File> existingFileOptional = fileRepository.findById(id);
            if (existingFileOptional.isPresent()) {
                File fileToUpdate = existingFileOptional.get();
                Files.deleteIfExists(Paths.get(fileToUpdate.getFilepath()));

                String directory = determineFileType(file);
                if (directory == null) {
                    return ApiResponse.error(ResponseError.notFound("Fayl yuklash uchun papka"));
                }

                long timestamp = System.currentTimeMillis();
                String filename = timestamp + "-" + file.getOriginalFilename();
                Path dirPath = root.resolve(directory);
                Path targetPath = dirPath.resolve(filename);
                Files.createDirectories(dirPath);
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                fileToUpdate.setFileName(file.getOriginalFilename());
                fileToUpdate.setFilepath(targetPath.toString());
                fileToUpdate.setContentType(file.getContentType());

                File updatedFile = fileRepository.save(fileToUpdate);
                return ApiResponse.ok(ResponseSuccess.fetched("File"), updatedFile.getId());
            } else {
                throw new ResponseStatusException(NOT_FOUND, "File not found");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "File operation failed");
        }
    }

    public ApiResponse<?> deleteFile(Long id) {
        try {
            Optional<File> fileOptional = fileRepository.findById(id);
            if (fileOptional.isPresent()) {
                File fileToDelete = fileOptional.get();
                Files.deleteIfExists(Paths.get(fileToDelete.getFilepath()));
                fileRepository.delete(fileToDelete);
                return ApiResponse.ok(ResponseSuccess.deleted("File"));
            } else {
                throw new ResponseStatusException(NOT_FOUND, "File not found");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Error deleting file");
        }
    }

    private String determineFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null) {
            if (filename.toLowerCase().matches(".*\\.(png|jpg|jpeg|webp)$")) {
                return "img";
            } else if (isSupportedFileType(filename)) {
                return "files";
            }
        }
        return null;
    }

    private boolean isSupportedFileType(String filename) {
        return filename.toLowerCase().matches(".*\\.(pdf|docx|pptx|zip|mp4|mkv|avi|mov)$");
    }
}
