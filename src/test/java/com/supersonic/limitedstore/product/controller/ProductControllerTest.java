package com.supersonic.limitedstore.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.product.presentation.controller.ProductController;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.res.ProductResponseDto;
import com.supersonic.limitedstore.domain.product.service.ProductService;
import com.supersonic.limitedstore.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 상품_생성_성공() throws Exception {
        // given
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("테스트 상품")
            .description("설명입니다")
            .price(10000)
            .stock(10)
            .releaseAt("2025-12-05 12:03:02")
            .build();

        ProductResponseDto response = ProductResponseDto.builder()
            .id(UUID.randomUUID())
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .releaseAt(LocalDateTime.now())
            .build();

        when(productService.createProduct(any())).thenReturn(ApiResponse.ok(response));

        // when & then
        mockMvc.perform(post("/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(dto.getName()));
    }

    @Test
    void 상품_단건조회_성공() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        ProductResponseDto response = ProductResponseDto.builder()
            .id(id)
            .name("상품명")
            .price(5000)
            .stock(3)
            .releaseAt(LocalDateTime.now())
            .build();

        when(productService.getProductById(id)).thenReturn(ApiResponse.ok(response));

        // when & then
        mockMvc.perform(get("/v1/products/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(id.toString()));
    }
}
