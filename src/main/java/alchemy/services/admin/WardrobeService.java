package alchemy.services.admin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import alchemy.annotations.Logged;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.admin.AdminProcessError;
import alchemy.model.Account;
import alchemy.model.WardrobeItemDTO;
import alchemy.model.wardrobe.WardrobeItem;
import alchemy.repositories.AccountRepository;
import alchemy.repositories.WardrobeRepository;
import alchemy.services.auth.AuthService;
import alchemy.services.technical.SupervisionService;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardrobeService {

	@Value("${cdn.endpoint}")
	private String publicUrl;
	
	@Value("${cdn.bucket.public}")
	private String publicBucket;
	
	@Value("${cdn.bucket.private}")
	private String privateBucket;
	
	private final AccountRepository accountRepository;
	private final AuthService authService;
	private final SupervisionService supervisionService;
	private final WardrobeRepository wardrobeRepository;
	private final MinioClient minioClient;
	
	@Logged("Get Wardrobe Items")
	public List<WardrobeItem> getWardrobeItems(UUID authorId) {
		Account author = accountRepository.findById(authorId).orElseThrow(
				() -> new ProcessException(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA, HttpStatus.BAD_REQUEST, ""));
		
		return wardrobeRepository.findByAuthor(author);
	}
	
	@Transactional
	@Logged("Upload Wardrobe Item")
	public WardrobeItem uploadWardrobeItem(MultipartFile frontImage, MultipartFile backImage, WardrobeItemDTO data) {
		List<String> missingFields = new ArrayList<>();
		
		if (StringUtils.isBlank(data.getName())) {
			missingFields.add("name");
		}
		
		if (data.getPrice() == null) {
			missingFields.add("price");
		}
		
		if (frontImage == null && backImage == null) {
			missingFields.add("images");
		}
		
		if (!missingFields.isEmpty()) {
			throw new ProcessException(AdminProcessError.WARDROBE_UPLOAD_MISSING_DATA, HttpStatus.BAD_REQUEST, String.join(", ", missingFields));
		}
		
		if (wardrobeRepository.existsByName(data.getName())) {
			throw new ProcessException(AdminProcessError.WARDROBE_UPLOAD_ALREADY_EXIST, HttpStatus.BAD_REQUEST, data.getName());
		}
		
		/* Prepare the item data. */
		WardrobeItem item = WardrobeItem.builder()
				.author(authService.getAuthenticatedAccount())
				.name(data.getName())
				.price(data.getPrice())
				.build();
		
		item = wardrobeRepository.save(item);
		
		/* Try to upload the images to the CDN. If it fails, remove orphans before throwing Exception. */
		try {
			uploadFile(frontImage, item.getId().toString() + "/" + "front.png");
			item.setFrontImage(item.getId().toString() + "/" + "front.png");
			
			uploadFile(backImage, item.getId().toString() + "/" + "back.png");
			item.setBackImage(item.getId().toString() + "/" + "back.png");
		} catch (Exception e) {
			if (!StringUtils.isBlank(item.getFrontImage())) {
				deleteFile(item.getFrontImage());
			}
			
			if (!StringUtils.isBlank(item.getBackImage())) {
				deleteFile(item.getBackImage());
			}
			
			wardrobeRepository.delete(item);
			
			throw new ProcessException(AdminProcessError.WARDROBE_UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return wardrobeRepository.save(item);
	}
	
	@Transactional
	@Logged("Delete Wardrobe Item")
	public void deleteWardrobeItem(UUID itemId) {
		Account user = authService.getAuthenticatedAccount();
		
		if (!wardrobeRepository.existsById(itemId)) {
			throw new ProcessException(AdminProcessError.WARDROBE_DELETION_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST, itemId.toString());
		}
		
		WardrobeItem item = wardrobeRepository.getReferenceById(itemId);
		
		if (!user.isAdmin() && !item.getAuthor().getId().equals(user.getId())) {
			throw new ProcessException(AdminProcessError.WARDROBE_DELETION_NOT_ALLOWED, HttpStatus.UNAUTHORIZED, itemId.toString());
		}
		
		try {
			if (!StringUtils.isBlank(item.getFrontImage())) {
				removeFile(item.getFrontImage());
			}

			if (!StringUtils.isBlank(item.getBackImage())) {
				removeFile(item.getBackImage());
			}
		} catch (Exception e) {
			throw new ProcessException(AdminProcessError.WARDROBE_DELETION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		wardrobeRepository.delete(item);
	}
	
	private String uploadFile(MultipartFile file, String name) throws Exception {
		if (file == null) {
			return "";
		}
		
		try (InputStream imageStream = file.getInputStream()) {
			ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
					.stream(imageStream, file.getSize(), -1)
					.bucket(publicBucket)
					.object(name)
					.build());
			
			return new StringBuilder()
					.append(publicUrl).append("/")
					.append(publicBucket).append("/")
					.append(response.object())
					.toString();
		}
	}
	
	private void deleteFile(String object) {
		try {
			minioClient.removeObject(RemoveObjectArgs.builder()
					.bucket(publicBucket)
					.object(object)
					.build());
		} catch (Exception e) {
			supervisionService.issueWarning("The object " + object + " could not be deleted from the CDN. It may be orphaned.");
		}
	}
	
	private void removeFile(String object) throws Exception {
		minioClient.copyObject(CopyObjectArgs.builder()
				.bucket(privateBucket)
				.object(object)
				.source(CopySource.builder()
						.bucket(publicBucket)
						.object(object)
						.build())
				.build());
		
		minioClient.removeObject(RemoveObjectArgs.builder()
				.bucket(publicBucket)
				.object(object)
				.build());
	}

}
