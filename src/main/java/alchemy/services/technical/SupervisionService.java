package alchemy.services.technical;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import alchemy.model.technical.SupervisionAlert;
import alchemy.repositories.SupervisionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupervisionService {

	private SupervisionRepository supervisionRepository;
	
	public void issueWarning(String content) {
		SupervisionAlert warning = SupervisionAlert.builder()
				.timestamp(LocalDateTime.now())
				.content(content)
				.build();
		
		supervisionRepository.save(warning);
	}
	
}
