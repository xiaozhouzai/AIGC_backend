package com.lcy.aigc.model.dto.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class FileUploadRequest implements Serializable {
    private static final long serialVersionUID = 5246869933204321780L;

    private MultipartFile file;
}
