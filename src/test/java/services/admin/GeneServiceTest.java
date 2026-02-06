package services.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
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
import alchemy.model.GeneIdentificationDTO;
import alchemy.model.pets.genes.Gene;
import alchemy.repositories.GeneRepository;
import alchemy.services.admin.GeneService;
import config.AbstractTest;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
public class GeneServiceTest extends AbstractTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private GeneRepository geneRepository;
    @Autowired private GeneService geneService;

    private void assertResultList(List<Gene> result, List<String> images) {
    	for (String image : images) {
    		assertTrue(result.stream().anyMatch(gene -> gene.getImage().equals(image)));
    	}
    	assertTrue(geneRepository.count() == images.size());
    }

    private void assertDuplicateOrMissingException(AdminProcessError expectedError, Executable call, List<String> expectedParams) {
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
			.image(image)
			.tags(List.of(tags))
			.build();

    	return entityManager.persistAndFlush(gene);
    }

    @Test
    void getAllGenesSuccessTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");

    	assertResultList(geneService.getAllGenes(), List.of(image));
    }

    @Test
    void createGenesSuccessTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");

    	assertResultList(geneService.getAllGenes(), List.of(image));

    	String creation1 = "/assets/hellhound-horns.png";
    	String creation2 = "/assets/bull-horns.png";

    	GeneIdentificationDTO dto1 = GeneIdentificationDTO.builder()
    			.image(creation1)
    			.build();

    	GeneIdentificationDTO dto2 = GeneIdentificationDTO.builder()
    			.image(creation2)
    			.build();

    	List<Gene> result = geneService.createGenes(List.of(dto1, dto2));

    	assertResultList(result, List.of(image, creation1, creation2));
    }

    @Test
    void createGenesFailureTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");

    	assertResultList(geneService.getAllGenes(), List.of(image));

    	String creation = "/assets/bull-horns.png";

    	GeneIdentificationDTO dto1 = GeneIdentificationDTO.builder()
    			.image(image)
    			.build();

    	GeneIdentificationDTO dto2 = GeneIdentificationDTO.builder()
    			.image(creation)
    			.build();

    	List<GeneIdentificationDTO> listGenesToCreate = List.of(dto1, dto2);

    	assertDuplicateOrMissingException(AdminProcessError.GENE_CREATION_ALREADY_EXIST,
    			() -> geneService.createGenes(listGenesToCreate), List.of(image));
    }

    @Test
    void deleteGenesSuccessTest() {
    	String image1 = "/assets/unicorn-horn.png";
    	String image2 = "/assets/bull-horns.png";
    	createGene(image1, "Horn", "Fae");
    	createGene(image2, "Horn", "Beast");

    	assertResultList(geneService.getAllGenes(), List.of(image1, image2));

    	String deletion = "/assets/unicorn-horn.png";

    	List<String> listImagesToDelete = List.of(deletion);
    	List<GeneIdentificationDTO> listGenesToDelete = listImagesToDelete.stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());
    	List<Gene> result = geneService.deleteGenes(listGenesToDelete);

    	assertResultList(result, List.of(image2));
    }

    @Test
    void deleteGenesFailureTest() {
    	String image = "/assets/unicorn-horn.png";
    	createGene(image, "Horn", "Fae");

    	assertResultList(geneService.getAllGenes(), List.of(image));

    	String deletion = "/assets/unicorn-horn.png";
    	String missing = "/assets/bull-horns.png";

    	List<String> listImagesToDelete = List.of(deletion, missing);
    	List<GeneIdentificationDTO> listGenesToDelete = listImagesToDelete.stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());

    	assertDuplicateOrMissingException(AdminProcessError.GENE_DELETION_DOES_NOT_EXIST,
    			() -> geneService.deleteGenes(listGenesToDelete), List.of(missing));
    }

    @Test
    void synchronizeGenesNoDeletionSuccessTest() {
    	assertResultList(geneService.getAllGenes(), Collections.emptyList());

    	String expectedImage = "/assets/unicorn-horn.png";

    	GeneIdentificationDTO dto = GeneIdentificationDTO.builder()
    			.image(expectedImage)
    			.build();

    	List<Gene> result = geneService.synchronizeGenes(List.of(dto), false);

    	assertResultList(result, List.of(expectedImage));
    }

    @Test
    void synchronizeGenesNoDeletionForcedSuccessTest() {
    	assertResultList(geneService.getAllGenes(), Collections.emptyList());

    	String expectedImage = "/assets/unicorn-horn.png";

    	GeneIdentificationDTO dto = GeneIdentificationDTO.builder()
    			.image(expectedImage)
    			.build();

    	List<Gene> result = geneService.synchronizeGenes(List.of(dto), true);

    	assertResultList(result, List.of(expectedImage));
    }

    @Test
    void synchronizeGenesDeletionFailureTest() {
    	String current = "/assets/unicorn-horn.png";
    	createGene(current, "Horn", "Fae");

    	assertResultList(geneService.getAllGenes(), List.of(current));

    	String expected = "/assets/bull-horns.png";

    	List<GeneIdentificationDTO> listExpectedGenes = List.of(expected).stream()
    			.map(img -> GeneIdentificationDTO.builder().image(img).build())
    			.collect(Collectors.toList());

    	assertConfirmationException(AdminConfirmationError.GENES_SYNCHRONIZATION_CONFIRM,
    			() -> geneService.synchronizeGenes(listExpectedGenes, false), List.of(current));
    }

    @Test
    void synchronizeGenesDeletionForcedSuccessTest() {
    	String current = "/assets/unicorn-horn.png";
    	createGene(current, "Horn", "Fae");

    	assertResultList(geneService.getAllGenes(), List.of(current));

    	String expectedImage = "/assets/unicorn-horn.png";

    	GeneIdentificationDTO dto = GeneIdentificationDTO.builder()
    			.image(expectedImage)
    			.build();

    	List<Gene> result = geneService.synchronizeGenes(List.of(dto), true);

    	assertResultList(result, List.of(expectedImage));
    }

}
