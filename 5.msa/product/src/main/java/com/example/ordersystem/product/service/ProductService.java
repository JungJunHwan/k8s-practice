package com.example.ordersystem.product.service;

import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.dto.ProductStockUpdateDto;
import com.example.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;

    public ProductService(ProductRepository productRepository, StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
    }

    public Product productCreate (ProductRegisterDto dto){

//         try {
//             //      member조회
//             Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//             Product product = productRepository.save(dto.toEntity(authentication.getName()));
// //            레디스에도 재고 추가
//             stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
// //            그런게 레디스는 아래에서 에러가 떠도 레디스는 트랜잭션 롤백이 안된다.(트랜잭션은 rdb의 개념이기 떄문)
// //            그래서 에러가 날 시 레디스에서 다시 재고를 빼도록 try-catch 처리가 필요함.


//             //       aws에 image저장 후에 url추출
// //               aws에 s3접근 가능한 iam계정(새끼 계정 같은 느낌) 생성, iam계정을 통해 aws에 접근가능한 접근 객체 생성
// //
//             MultipartFile image = dto.getProductImage();
//             byte[] bytes = image.getBytes();
//             String fileName = product.getId() + " "+ image.getOriginalFilename(); //여따 파일명에 아이디 값 불이려고 위에서 product뽑아냅
// //    먼저 local에 저장
//             Path path = Paths.get("C:/Users/Playdata/Desktop/temp/",fileName);
//             Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//             return product;
//         } catch (IOException e){
//             throw new RuntimeException("이미지 저장 실패");// 롤백처리를 위해 checkException으로 던져줘야함
//         }
        return null;
    }

    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto productSearchDto){
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(productSearchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.equal(root.get("category"), productSearchDto.getCategory()));
                }
                if(productSearchDto.getProductName() != null){
                    predicates.add(criteriaBuilder.like(root.get("name"),"%"+productSearchDto.getProductName()+"%"));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for (int i = 0; i < predicates.size(); i++) {
                    predicateArr[i] = predicates.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList.map(p->p.resDtoFromEntity());
    }

    public ProductResDto productDetail(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("product not found"));
        return product.resDtoFromEntity();
    }
    public Product updateStockQuantity(ProductStockUpdateDto productStockUpdateDto){
        Product product = productRepository.findById(productStockUpdateDto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
        product.updateStockQuantity(productStockUpdateDto.getProductQuantity());
        return product;
    }

    @KafkaListener(topics = "update-stock-topic", containerFactory = "kafkaListener")
    public void productConsumer(String message){
        System.out.println("컨슈머 메시지 : " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ProductStockUpdateDto productStockUpdateDto = objectMapper.readValue(message, ProductStockUpdateDto.class);
            this.updateStockQuantity(productStockUpdateDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
