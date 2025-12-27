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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import alchemy.api.AdminApi;
import alchemy.exceptions.confirmation.admin.AdminConfirmationError;
import alchemy.mappers.GeneMapper;
import alchemy.mappers.MoveMapper;
import alchemy.model.GeneCreationRequestDTO;
import alchemy.model.GeneDeletionRequestDTO;
import alchemy.model.GeneListResponseDTO;
import alchemy.model.GeneSynchronizationRequestDTO;
import alchemy.model.MoveCreationRequestDTO;
import alchemy.model.MoveDeletionRequestDTO;
import alchemy.model.MoveListResponseDTO;
import alchemy.model.MoveUpdateRequestDTO;
import alchemy.model.pets.genes.Gene;
import alchemy.model.pets.moves.Move;
import alchemy.services.admin.GeneService;
import alchemy.services.admin.MoveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController implements AdminApi {
	
	private final GeneMapper geneMapper;
	private final GeneService geneService;
	private final MoveMapper moveMapper;
	private final MoveService moveService;

	@GetMapping("/genes")
	public ResponseEntity<GeneListResponseDTO> getGenes() {
		List<Gene> genes = geneService.getAllGenes();
		
		GeneListResponseDTO response = GeneListResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/genes")
	public ResponseEntity<GeneListResponseDTO> createGene(GeneCreationRequestDTO request) {
		List<Gene> genes = geneService.createGenes(request.getGeneIdentifications());
		
		GeneListResponseDTO response = GeneListResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/genes")
	public ResponseEntity<GeneListResponseDTO> deleteGene(GeneDeletionRequestDTO request) {
		List<Gene> genes = geneService.deleteGenes(request.getGeneIdentifications());
		
		GeneListResponseDTO response = GeneListResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/genes")
	public ResponseEntity<GeneListResponseDTO> synchronizeGenes(GeneSynchronizationRequestDTO request) {
		HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		boolean isConfirmed = Boolean.parseBoolean(httpRequest.getHeader(AdminConfirmationError.GENES_SYNCHRONIZATION_CONFIRM.confirmationKey));

		List<Gene> genes = geneService.synchronizeGenes(request.getGeneIdentifications(), isConfirmed);
		
		GeneListResponseDTO response = GeneListResponseDTO.builder()
				.genes(genes.stream().map(gene -> geneMapper.toGeneDTO(gene)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<MoveListResponseDTO> getMoves() {
		List<Move> moves = moveService.getAllMoves();
		
		MoveListResponseDTO response = MoveListResponseDTO.builder()
			.moves(moves.stream().map(move -> moveMapper.toDTO(move)).collect(Collectors.toList()))
			.build();
		
		return ResponseEntity.ok(response);
	}

	@PostMapping("/moves")
	public ResponseEntity<MoveListResponseDTO> createMove(@Valid MoveCreationRequestDTO request) {
		List<Move> moves = moveService.createMove(request.getMoveIdentifications());
		
		MoveListResponseDTO response = MoveListResponseDTO.builder()
			.moves(moves.stream().map(move -> moveMapper.toDTO(move)).collect(Collectors.toList()))
			.build();
		
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/moves")
	public ResponseEntity<MoveListResponseDTO> deleteMove(@Valid MoveDeletionRequestDTO request) {
		List<Move> moves = moveService.deleteMove(request.getMoveIdentifications());
		
		MoveListResponseDTO response = MoveListResponseDTO.builder()
				.moves(moves.stream().map(move -> moveMapper.toDTO(move)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/moves")
	public ResponseEntity<MoveListResponseDTO> updateMove(@Valid MoveUpdateRequestDTO request) {
		List<Move> moves = moveService.updateMove(request.getMove());

		MoveListResponseDTO response = MoveListResponseDTO.builder()
				.moves(moves.stream().map(move -> moveMapper.toDTO(move)).collect(Collectors.toList()))
				.build();
		
		return ResponseEntity.ok(response);
	}

}
