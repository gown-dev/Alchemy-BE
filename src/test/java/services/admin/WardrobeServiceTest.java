package services.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import alchemy.config.Autoconfiguration;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.admin.AdminProcessError;
import alchemy.model.Account;
import alchemy.model.WardrobeItemCategoryDTO;
import alchemy.model.WardrobeItemDTO;
import alchemy.model.wardrobe.WardrobeItem;
import alchemy.model.wardrobe.WardrobeItemCategory;
import alchemy.repositories.AccountRepository;
import alchemy.repositories.WardrobeRepository;
import alchemy.services.admin.WardrobeService;
import config.AbstractTest;
import io.minio.CopyObjectArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

@SpringBootTest(classes = { WardrobeService.class })
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WardrobeServiceTest extends AbstractTest {

	@Autowired private AccountRepository accountRepository;
	@Autowired private WardrobeRepository wardrobeRepository;
	@Autowired private WardrobeService wardrobeService;
	@MockitoBean private MinioClient minioClient;
	
	@Value("${cdn.bucket.public}")
    private String bucket;
	
	@BeforeAll
	void setupAuthenticatedAccount() {
		accountRepository.save(Account.builder()
				.username("Gown")
				.password("p455w0rd")
				.authorities(new ArrayList<String>(List.of("ADMIN")))
				.build());
	}
	
	@BeforeEach
	void cleanUp() {
		wardrobeRepository.deleteAll();
	}
	
	@BeforeEach
	void setupMinioClient() throws Exception {
		ObjectWriteResponse response = new ObjectWriteResponse(null, "", "", "", "", "");
		doReturn(response).when(minioClient).putObject(any(PutObjectArgs.class));
	}
	
	@Test
	@WithUserDetails("Gown")
    void shouldUploadBothImagesSuccessfully() throws Exception {
		MockMultipartFile frontImage = new MockMultipartFile("file", "front-image.png", "image/png", "content".getBytes());
		MockMultipartFile backImage = new MockMultipartFile("file", "back-image.png", "image/png", "content".getBytes());

		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.name("test-item")
				.price(10)
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
        
		ObjectWriteResponse response = new ObjectWriteResponse(null, "", "", "", "", "");
		doReturn(response).when(minioClient).putObject(any(PutObjectArgs.class));
		
        wardrobeService.uploadWardrobeItem(frontImage, backImage, data);
        
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(2)).putObject(captor.capture());
    }
	
	@Test
	@WithUserDetails("Gown")
    void shouldUploadOnlyFrontImageSuccessfully() throws Exception {
		MockMultipartFile frontImage = new MockMultipartFile("file", "front-image.png", "image/png", "content".getBytes());
		MockMultipartFile backImage = null;

		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.name("test-item")
				.price(10)
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
		
        wardrobeService.uploadWardrobeItem(frontImage, backImage, data);

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(1)).putObject(captor.capture());
    }
	
	@Test
	@WithUserDetails("Gown")
    void shouldUploadOnlyBackImageSuccessfully() throws Exception {
		MockMultipartFile frontImage = null;
		MockMultipartFile backImage = new MockMultipartFile("file", "back-image.png", "image/png", "content".getBytes());

		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.name("test-item")
				.price(10)
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
		
        wardrobeService.uploadWardrobeItem(frontImage, backImage, data);

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(1)).putObject(captor.capture());
    }
	
	@Test
	@WithUserDetails("Gown")
    void shouldThrowIfDuplicateExists() throws Exception {
		Account author = accountRepository.findByUsername("Gown").get();
		
		MockMultipartFile frontImage = new MockMultipartFile("file", "front-image.png", "image/png", "content".getBytes());
		MockMultipartFile backImage = new MockMultipartFile("file", "back-image.png", "image/png", "content".getBytes());

		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.name("test-item")
				.price(10)
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
		
		WardrobeItem item = WardrobeItem.builder()
				.author(author)
				.name("test-item")
				.category(WardrobeItemCategory.HEAD)
				.build();
		
		wardrobeRepository.save(item);
		
		ProcessException exception = assertThrows(ProcessException.class, () -> wardrobeService.uploadWardrobeItem(frontImage, backImage, data));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_ALREADY_EXIST.getCode().equals(exception.getError().getCode()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_ALREADY_EXIST.getDescription().equals(exception.getError().getDescription()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_ALREADY_EXIST.getMessage().equals(exception.getError().getMessage()));
    }
	
	@Test
    void shouldThrowIfNoImages() throws Exception {
		MockMultipartFile frontImage = null;
		MockMultipartFile backImage = null;

		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.name("test-item")
				.price(10)
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
		
		ProcessException exception = assertThrows(ProcessException.class, () -> wardrobeService.uploadWardrobeItem(frontImage, backImage, data));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getCode().equals(exception.getError().getCode()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getDescription().equals(exception.getError().getDescription()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getMessage().equals(exception.getError().getMessage()));
    }
	
	@Test
    void shouldThrowIfNameMissing() throws Exception {
		MockMultipartFile frontImage = null;
		MockMultipartFile backImage = new MockMultipartFile("file", "back-image.png", "image/png", "content".getBytes());

		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.price(10)
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
		
		ProcessException exception = assertThrows(ProcessException.class, () -> wardrobeService.uploadWardrobeItem(frontImage, backImage, data));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getCode().equals(exception.getError().getCode()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getDescription().equals(exception.getError().getDescription()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getMessage().equals(exception.getError().getMessage()));
    }
	
	@Test
	@WithUserDetails("Gown")
    void shouldThrowIfPriceMissing() throws Exception {
		MockMultipartFile frontImage = null;
		MockMultipartFile backImage = new MockMultipartFile("file", "back-image.png", "image/png", "content".getBytes());
		
		WardrobeItemDTO data = WardrobeItemDTO.builder()
				.name("test-item")
				.category(WardrobeItemCategoryDTO.HEAD)
				.build();
		
		ProcessException exception = assertThrows(ProcessException.class, () -> wardrobeService.uploadWardrobeItem(frontImage, backImage, data));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getCode().equals(exception.getError().getCode()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getDescription().equals(exception.getError().getDescription()));
		assertTrue(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA.getMessage().equals(exception.getError().getMessage()));
    }
	
	@Test
	@WithUserDetails("Gown")
    void shouldThrowIfDeleteMissingItem() throws Exception {
		ProcessException exception = assertThrows(ProcessException.class, () -> wardrobeService.deleteWardrobeItem(UUID.fromString("00000000-0000-0000-0000-000000000000")));
		assertTrue(AdminProcessError.WARDROBE_DELETION_DOES_NOT_EXIST.getCode().equals(exception.getError().getCode()));
		assertTrue(AdminProcessError.WARDROBE_DELETION_DOES_NOT_EXIST.getDescription().equals(exception.getError().getDescription()));
		assertTrue(AdminProcessError.WARDROBE_DELETION_DOES_NOT_EXIST.getMessage().equals(exception.getError().getMessage()));		
    }
	
	@Test
	@WithUserDetails("Gown")
    void shouldDeleteItemSuccesfully() throws Exception {
		Account author = accountRepository.findByUsername("Gown").get();

		WardrobeItem item = WardrobeItem.builder()
				.author(author)
				.name("test-item")
				.category(WardrobeItemCategory.HEAD)
				.build();
		
		item = wardrobeRepository.save(item);
		
		assertTrue(wardrobeRepository.count() == 1);
		
		doReturn(null).when(minioClient).copyObject(any(CopyObjectArgs.class));
		doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));
		
		wardrobeService.deleteWardrobeItem(item.getId());
		
		assertTrue(wardrobeRepository.count() == 0);
    }

}
