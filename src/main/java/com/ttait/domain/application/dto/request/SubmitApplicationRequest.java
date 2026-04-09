package com.ttait.domain.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 기업 관리자가 협약 신청서를 제출할 때 사용하는 multipart/form-data DTO
 */
@Getter
@Setter
public class SubmitApplicationRequest {

    @NotBlank(message = "업종은 필수입니다.")
    @Size(max = 50, message = "업종은 50자 이하로 입력해주세요.")
    private String industryType;

    @NotNull(message = "사원수는 필수입니다.")
    @Positive(message = "사원수는 1 이상이어야 합니다.")
    private Integer employeeCount;

    @NotBlank(message = "사업장 소재지는 필수입니다.")
    @Size(max = 200, message = "사업장 소재지는 200자 이하로 입력해주세요.")
    private String address;

    @NotNull(message = "파일은 필수입니다.")
    @Size(min = 1, max = 10, message = "파일은 1개 이상 10개 이하로 첨부해주세요.")
    private List<MultipartFile> files;
}
