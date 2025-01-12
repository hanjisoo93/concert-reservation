package kr.hhplus.be.server.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(title = "공통 페이징 Request", description = "공통 페이징 Request")
public class BasePageResponse {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
