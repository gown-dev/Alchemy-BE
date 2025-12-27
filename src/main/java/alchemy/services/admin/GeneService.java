package alchemy.services.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import alchemy.annotations.Logged;
import alchemy.exceptions.ConfirmationException;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.confirmation.admin.AdminConfirmationError;
import alchemy.exceptions.process.admin.AdminProcessError;
import alchemy.mappers.GeneMapper;
import alchemy.model.GeneIdentificationDTO;
import alchemy.model.pets.genes.Gene;
import alchemy.repositories.GeneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneService {

	private final GeneRepository geneRepository;
	private final GeneMapper geneMapper;
	
	@Logged("Get All Genes")
	public List<Gene> getAllGenes() {
		return geneRepository.findAll();
	}
	
	@Transactional
	@Logged("Gene Synchronization")
	public List<Gene> synchronizeGenes(List<GeneIdentificationDTO> expectedGenes, boolean confirmed) {
		List<String> expectedImageKeys = expectedGenes.stream().map(gene -> gene.getImage()).collect(Collectors.toList());
		
		log.debug("Expected genes with image keys : {}", expectedImageKeys.stream().collect(Collectors.joining(", ")));

		List<String> savedKeys = getAllGenes().stream()
				.map(gene -> gene.getImage())
				.collect(Collectors.toList());
		
		log.debug("Current genes image keys in database : {}", savedKeys.stream().collect(Collectors.joining(", ")));
		
		List<Gene> genesToCreate = expectedGenes.stream()
				.filter(gene -> !savedKeys.contains(gene.getImage()))
				.map(gene -> geneMapper.toGeneEntity(gene))
				.collect(Collectors.toList());
		
		log.debug("Genes synchronization (creation) for image keys : {}", genesToCreate.stream()
				.map(gene -> gene.getImage())
				.collect(Collectors.joining(", ")));
		
		List<String> genesToDelete = savedKeys.stream()
				.filter(gene -> !expectedImageKeys.contains(gene))
				.collect(Collectors.toList());
		
		log.debug("Genes synchronization (deletion) for keys : {}", genesToDelete.stream()
				.map(id -> id.toString().substring(0, 4))
				.collect(Collectors.joining(", ")));
	
		if (!confirmed && genesToDelete.size() > 0) {
			log.debug("Synchronization was not confirmed via the related header but causes deletion. Confirmation exception raised.");
			String messageDetails = genesToDelete.stream().collect(Collectors.joining(", "));
			throw new ConfirmationException(AdminConfirmationError.GENES_SYNCHRONIZATION_CONFIRM, messageDetails);
		}
		
		delete(genesToDelete);
		save(genesToCreate);
		log.debug("Synchronization task done successfully.");

		List<Gene> finalGenes = geneRepository.findAll();
		log.debug("Final list of genes : {}", finalGenes.stream().map(gene -> gene.getImage()).collect(Collectors.joining(", ")));
		
		return finalGenes;
	}
	
	@Transactional
	@Logged("Gene Creation")
	public List<Gene> createGenes(List<GeneIdentificationDTO> genes) {
		List<Gene> genesToCreate = genes.stream()
				.map(gene -> geneMapper.toGeneEntity(gene))
				.collect(Collectors.toList());
		
		log.debug("Genes creation for image keys : {}", genesToCreate.stream()
				.map(gene -> gene.getImage())
				.collect(Collectors.joining(", ")));

		List<String> geneImageKeys = genesToCreate.stream().map(gene -> gene.getImage()).collect(Collectors.toList());
		
		if (geneRepository.existsByImageIn(geneImageKeys)) {
			log.debug("Upon creation, duplicates were found in the database. Raising exception.");
			List<Gene> duplicates = geneRepository.findAllById(geneImageKeys);
			String messageDetails = duplicates.stream().map(geneId -> geneId.getImage()).collect(Collectors.joining(", "));
			throw new ProcessException(AdminProcessError.GENE_CREATION_ALREADY_EXIST, HttpStatus.BAD_REQUEST, messageDetails);
		}
		
		save(genesToCreate);
		log.debug("Creation task done successfully.");
		
		List<Gene> finalGenes = geneRepository.findAll();
		log.debug("Final list of genes images : {}", finalGenes.stream().map(gene -> gene.getImage()).collect(Collectors.joining(", ")));
		
		return finalGenes;
	}
	
	@Transactional
	@Logged("Gene Deletion")
	public List<Gene> deleteGenes(List<GeneIdentificationDTO> genes) {
		List<String> genesToDelete = genes.stream()
				.map(gene -> gene.getImage())
				.collect(Collectors.toCollection(() -> new ArrayList<String>()));
		
		log.debug("Genes deletion for keys : {}", genesToDelete.stream().map(id -> id.toString())
				.collect(Collectors.joining(", ")));

		List<String> existingGenes = geneRepository.findAllById(genesToDelete).stream()
				.map(gene -> gene.getImage())
				.collect(Collectors.toList());
		
		if (existingGenes.size() < genesToDelete.size()) {
			genesToDelete.removeAll(existingGenes);
			
			String messageDetails = genesToDelete.stream().collect(Collectors.joining(", "));
			throw new ProcessException(AdminProcessError.GENE_DELETION_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST, messageDetails);
		}
		
		delete(genesToDelete);
		log.debug("Deletion task done successfully.");
		
		List<Gene> finalGenes = geneRepository.findAll();
		log.debug("Final list of genes : {}", finalGenes.stream().map(gene -> gene.getImage()).collect(Collectors.joining(", ")));
		
		return finalGenes;
	}
	
	private void save(List<Gene> genes) {	
		genes.forEach(gene -> geneRepository.save(gene));
		log.debug("Creation step done successfully.");
	}
	
	private void delete(List<String> geneImageKeys) {		
		geneRepository.deleteAllById(geneImageKeys);
		log.debug("Deletion step done successfully.");
	}
	
}
