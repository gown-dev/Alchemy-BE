package services.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import alchemy.config.Autoconfiguration;
import alchemy.exceptions.ConfirmationException;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.confirmation.admin.AdminConfirmationError;
import alchemy.exceptions.process.admin.AdminProcessError;
import alchemy.model.Gene;
import alchemy.model.GeneIdentificationDTO;
import alchemy.repositories.GeneRepository;
import alchemy.services.AdminService;
import config.AbstractTest;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
public class AdminServiceTest extends AbstractTest {
	
    @Autowired private TestEntityManager entityManager;
    @Autowired private GeneRepository geneRepository;
    @Autowired private AdminService adminService;
    
    private void assertSavedList(List<String> images) {
    	for (String image : images) {
    		assertTrue(geneRepository.existsById(image));
    	}
    	assertTrue(geneRepository.count() == images.size());
    }
    
    private void assertResultList(List<Gene> result, List<String> images) {
    	for (String image : images) {
    		assertTrue(result.stream().anyMatch(gene -> gene.getId().equals(image)));
    	}
    	assertTrue(geneRepository.count() == images.size());
    }
    
    private void assertAdminDuplicateOrMissingException(AdminProcessError expectedError, Executable call, List<String> expectedParams) {
    	ProcessException exception = assertThrows(ProcessException.class, call);
    	assertTrue(expectedError.code.equals(exception.getError().getCode()));
    	assertTrue(expectedError.description.equals(exception.getError().getDescription()));
    	assertTrue(expectedError.message.equals(exception.getError().getMessage()));
    	
    	List<String> parameters = List.of(exception.getParameters());
    	parameters.forEach(param -> {
    		assertTrue(expectedParams.contains(param));
    	});
    }
    
    private void assertConfirmationException(AdminConfirmationError expectedError, Executable call, List<String> expectedParams) {
    	ConfirmationException exception = assertThrows(ConfirmationException.class, call);
    	assertTrue(expectedError.code.equals(exception.getError().getCode()));
    	assertTrue(expectedError.description.equals(exception.getError().getDescription()));
    	assertTrue(expectedError.message.equals(exception.getError().getMessage()));
    	assertTrue(expectedError.confirmationKey.equals(exception.getError().getConfirmationKey()));
    	
    	List<String> parameters = List.of(exception.getParameters());
    	parameters.forEach(param -> {
    		assertTrue(expectedParams.contains(param));
    	});
    }
    
    private Gene createGene(String image, String... tags) {
    	Gene gene = Gene.builder()
			.id(image)
			.tags(List.of(tags))
			.build();
    	
    	return entityManager.persistAndFlush(gene);
    }
    
    @Test
    void getAllGenesSuccessTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");
    	
    	assertSavedList(List.of(image));
    	assertResultList(adminService.getAllGenes(), List.of(image));
    }
    
    @Test
    void createGenesSuccessTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");
    	
    	assertSavedList(List.of(image));
    	
    	String creation1 = "/assets/hellhound-horns.png";
    	String creation2 = "/assets/bull-horns.png";
    	
    	List<String> listImagesToCreate = List.of(creation1, creation2);
    	List<GeneIdentificationDTO> listGenesToCreate = listImagesToCreate.stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	List<Gene> result = adminService.createGenes(listGenesToCreate);
    	
    	assertResultList(result, List.of(image, creation1, creation2));
    }
    
    @Test
    void createGenesFailureTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");
    	
    	assertSavedList(List.of(image));
    	
    	String duplicate = "/assets/unicorn-horn.png";
    	String creation = "/assets/bull-horns.png";
    	
    	List<String> listImagesToCreate = List.of(duplicate, creation);
    	List<GeneIdentificationDTO> listGenesToCreate = listImagesToCreate.stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	
    	assertAdminDuplicateOrMissingException(AdminProcessError.GENE_CREATION_ALREADY_EXIST,
    			() -> adminService.createGenes(listGenesToCreate), List.of(duplicate));
    }
    
    @Test
    void deleteGenesSuccessTest() {
    	String image1 = "/assets/unicorn-horn.png";
    	String image2 = "/assets/bull-horns.png";
    	createGene(image1, "Horn", "Fae");
    	createGene(image2, "Horn", "Beast");
    	
    	assertTrue(geneRepository.count() == 2);
    	
    	String deletion = "/assets/unicorn-horn.png";
    	
    	List<String> listImagesToDelete = List.of(deletion);
    	List<GeneIdentificationDTO> listGenesToDelete = listImagesToDelete.stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	List<Gene> result = adminService.deleteGenes(listGenesToDelete);
    	
    	assertResultList(result, List.of(image2));
    }
    
    @Test
    void deleteGenesFailureTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");
    	
    	assertTrue(geneRepository.count() == 1);
    	
    	String deletion = "/assets/unicorn-horn.png";
    	String missing = "/assets/bull-horns.png";
    	
    	List<String> listImagesToDelete = List.of(deletion, missing);
    	List<GeneIdentificationDTO> listGenesToDelete = listImagesToDelete.stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	
    	assertAdminDuplicateOrMissingException(AdminProcessError.GENE_DELETION_DOES_NOT_EXIST,
    			() -> adminService.deleteGenes(listGenesToDelete), List.of(missing));
    }
    
    @Test
    void synchronizeGenesNoDeletionSuccessTest() {
    	assertTrue(geneRepository.count() == 0);
    	
    	String expected = "/assets/unicorn-horn.png";
    	
    	List<GeneIdentificationDTO> listExpectedGenes = List.of(expected).stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	
    	List<Gene> result = adminService.synchronizeGenes(listExpectedGenes, false);

    	assertResultList(result, List.of(expected));
    }
    
    @Test
    void synchronizeGenesNoDeletionForcedSuccessTest() {
    	assertTrue(geneRepository.count() == 0);
    	
    	String expected = "/assets/unicorn-horn.png";
    	
    	List<GeneIdentificationDTO> listExpectedGenes = List.of(expected).stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	
    	List<Gene> result = adminService.synchronizeGenes(listExpectedGenes, true);

    	assertResultList(result, List.of(expected));
    }
    
    @Test
    void synchronizeGenesDeletionFailureTest() {
    	String current = "/assets/unicorn-horn.png";
    	createGene(current, "Horn", "Fae");
    	
    	assertTrue(geneRepository.count() == 1);
    	
    	String expected = "/assets/bull-horns.png";
    	
    	List<GeneIdentificationDTO> listExpectedGenes = List.of(expected).stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());

    	assertConfirmationException(AdminConfirmationError.GENES_SYNCHRONIZATION_CONFIRM, 
    			() -> adminService.synchronizeGenes(listExpectedGenes, false), List.of(current));
    }
    
    @Test
    void synchronizeGenesDeletionForcedSuccessTest() {
    	String current = "/assets/unicorn-horn.png";
    	createGene(current, "Horn", "Fae");
    	
    	assertTrue(geneRepository.count() == 1);
    	
    	String expected = "/assets/bull-horns.png";
    	
    	List<GeneIdentificationDTO> listExpectedGenes = List.of(expected).stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	
    	List<Gene> result = adminService.synchronizeGenes(listExpectedGenes, true);

    	assertResultList(result, List.of(expected));
    }

}
