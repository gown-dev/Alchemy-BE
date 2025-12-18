package alchemy.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import alchemy.api.AdminApi;
import alchemy.exceptions.confirmation.admin.AdminConfirmationError;
import alchemy.mappers.GeneMapper;
import alchemy.model.Gene;
import alchemy.model.GeneIdentificationRequestDTO;
import alchemy.model.GenesResponseDTO;
import alchemy.services.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController implements AdminApi {
	
    private HttpServletRequest httpRequest;
	private GeneMapper geneMapper;
	private AdminService adminService;

	@GetMapping("/getGenes")
	public ResponseEntity<GenesResponseDTO> getGenes() {
		List<Gene> genes = adminService.getAllGenes();
		
		GenesResponseDTO response = GenesResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/synchronizeGenes")
	public ResponseEntity<GenesResponseDTO> createGene(GeneIdentificationRequestDTO request) {
		List<Gene> genes = adminService.createGenes(request.getGenes());
		
		GenesResponseDTO response = GenesResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/synchronizeGenes")
	public ResponseEntity<GenesResponseDTO> deleteGene(GeneIdentificationRequestDTO request) {
		List<Gene> genes = adminService.deleteGenes(request.getGenes());
		
		GenesResponseDTO response = GenesResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/synchronizeGenes")
	public ResponseEntity<GenesResponseDTO> synchronizeGenes(GeneIdentificationRequestDTO request) {
		boolean isConfirmed = Boolean.parseBoolean(httpRequest.getHeader(AdminConfirmationError.GENES_SYNCHRONIZATION_CONFIRM.confirmationKey));

		List<Gene> genes = adminService.synchronizeGenes(request.getGenes(), isConfirmed);
		
		GenesResponseDTO response = GenesResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}

}
