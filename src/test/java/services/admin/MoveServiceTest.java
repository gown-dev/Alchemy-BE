package services.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import alchemy.config.Autoconfiguration;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.admin.AdminProcessError;
import alchemy.mappers.ConstraintMapper;
import alchemy.mappers.MoveMapper;
import alchemy.model.AttributeDTO;
import alchemy.model.AttributeRequirementDTO;
import alchemy.model.AttributeRestrictionDTO;
import alchemy.model.DamageComponentDTO;
import alchemy.model.DamageTypeDTO;
import alchemy.model.MoveDTO;
import alchemy.model.MoveIdentificationDTO;
import alchemy.model.pets.constraints.Constraint;
import alchemy.model.pets.moves.Move;
import alchemy.model.pets.moves.components.MoveComponent;
import alchemy.repositories.MoveRepository;
import alchemy.services.admin.MoveService;
import config.AbstractTest;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
public class MoveServiceTest extends AbstractTest {
	
    @Autowired private TestEntityManager entityManager;
    @Autowired private MoveMapper moveMapper;
    @Autowired private ConstraintMapper constraintMapper;
    @Autowired private MoveRepository moveRepository;
    @Autowired private MoveService moveService;
    
    private void assertResult(Move result, MoveDTO request) {
    	assertResult(result, request.getIdentification());
    	
    	if (request.getCooldown() == null) {
        	assertTrue(result.getCooldown() == 0);
    	} else {
    		assertTrue(result.getCooldown() == request.getCooldown());
    	}
    	
    	List<Constraint> expectedConstraints = request.getConstraints().stream()
    			.map(constraintDTO -> constraintMapper.toConstraintDTO(constraintDTO))
    			.collect(Collectors.toList());
    	
    	assertThat(result.getConstraints())
        	.usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        	.containsExactlyInAnyOrderElementsOf(expectedConstraints);
    	
    	List<MoveComponent> expectedComponents = request.getComponents().stream()
    			.map(componentDTO -> moveMapper.toMoveComponentEntity(componentDTO))
    			.collect(Collectors.toList());
    	
    	assertThat(result.getComponents())
	    	.usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
	    	.containsExactlyInAnyOrderElementsOf(expectedComponents);
    }
    
    private void assertResult(Move result, MoveIdentificationDTO request) {
    	assertTrue(StringUtils.equals(result.getName(), request.getName()));
    	assertTrue(result.getTags().size() == request.getTags().size());
    	assertTrue(result.getTags().stream().allMatch(tag -> request.getTags().contains(tag)));
    }
    
    private void assertResultList(List<Move> result, List<String> names) {
    	for (String name : names) {
    		assertTrue(result.stream().anyMatch(move -> move.getName().equals(name)));
    	}
    	assertTrue(moveRepository.count() == names.size());
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
    
    private Move createMove(String name, String... tags) {
    	Move move = Move.builder()
			.name(name)
			.tags(List.of(tags))
			.build();
    	
    	return entityManager.persistAndFlush(move);
    }
    
    @Test
    void getAllMovesSuccessTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));
    }
    
    @Test
    void createMoveSuccessTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));

    	String name1 = "Horn Strike";
    	
    	MoveIdentificationDTO dto1 = MoveIdentificationDTO.builder()
    			.name(name1)
    			.tags(List.of("Physical", "Horns"))
    			.build();
    	
    	List<Move> result = moveService.createMove(dto1);
    	
    	assertResultList(result, List.of(name, name1));
    }
    
    @Test
    void createMoveFailureTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));
    	
    	String creation = "Fireball";
    	
    	MoveIdentificationDTO dto1 = MoveIdentificationDTO.builder()
    			.name(creation)
    			.tags(Collections.emptyList())
    			.build();
    	    	
    	assertDuplicateOrMissingException(AdminProcessError.MOVE_CREATION_ALREADY_EXIST,
    			() -> moveService.createMove(dto1), List.of(creation));
    }
    
    @Test
    void deleteMoveSuccessTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));
    	
    	MoveIdentificationDTO dto1 = MoveIdentificationDTO.builder()
    			.name("Fireball")
    			.tags(Collections.emptyList())
    			.build();
    	
    	moveService.deleteMove(dto1);
    	
    	assertResultList(moveService.getAllMoves(), Collections.emptyList());
    }
    
    @Test
    void deleteMoveFailureTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));
    	
    	String deletion = "Fireball";
    	
    	MoveIdentificationDTO dto1 = MoveIdentificationDTO.builder()
    			.name("Horn Strike")
    			.tags(Collections.emptyList())
    			.build();
    	
    	assertDuplicateOrMissingException(AdminProcessError.MOVE_DELETION_DOES_NOT_EXIST,
    			() -> moveService.deleteMove(dto1), List.of(deletion));
    	assertResultList(moveService.getAllMoves(), List.of(name));
    }
    
    @Test
    void updateMoveSuccessTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));
    	
    	MoveIdentificationDTO identification = MoveIdentificationDTO.builder()
    			.name(name)
    			.tags(List.of("Projectile"))
    			.build();
    	
    	AttributeRequirementDTO requirement = AttributeRequirementDTO.builder()
    			.type("ATTRIBUTE_REQUIREMENT")
    			.attribute(AttributeDTO.CONSTITUTION)
    			.threshold(3)
    			.build();
    	AttributeRestrictionDTO restriction = AttributeRestrictionDTO.builder()
    			.type("ATTRIBUTE_RESTRICTION")
    			.attribute(AttributeDTO.MASTERY)
    			.threshold(5)
    			.build();
    	
    	DamageComponentDTO damage1 = DamageComponentDTO.builder()
    			.type("DAMAGE_COMPONENT")
    			.baseDamage(10)
    			.baseBypass(3)
    			.damageType(DamageTypeDTO.PHYSICAL)
    			.build();
    	
    	DamageComponentDTO damage2 = DamageComponentDTO.builder()
    			.type("DAMAGE_COMPONENT")
    			.baseDamage(5)
    			.baseBypass(5)
    			.damageType(DamageTypeDTO.MAGICAL)
    			.build();
    	
    	MoveDTO dto = MoveDTO.builder()
    			.identification(identification)
    			.constraints(List.of(requirement, restriction))
    			.components(List.of(damage1, damage2))
    			.build();
    	
    	List<Move> result = moveService.updateMove(dto);
    	Move updatedMove = moveRepository.getReferenceById(name);
    	
    	assertResult(updatedMove, dto);
    	assertResultList(result, List.of(name));
    }
    
    @Test
    void updateMoveFailureTest() {
    	String name = "Fireball";
    	createMove(name, "Magical", "Fire");
    	
    	assertResultList(moveService.getAllMoves(), List.of(name));
    	
    	String updateName = "Horn Strike";
    	
    	MoveIdentificationDTO identification = MoveIdentificationDTO.builder()
    			.name(updateName)
    			.tags(List.of("Projectile"))
    			.build();
    	
    	MoveDTO dto = MoveDTO.builder()
    			.identification(identification)
    			.constraints(List.of())
    			.components(List.of())
    			.build();
    	
    	assertDuplicateOrMissingException(AdminProcessError.MOVE_UPDATE_DOES_NOT_EXIST,
    			() -> moveService.updateMove(dto), List.of(updateName));
    	assertResultList(moveService.getAllMoves(), List.of(name));
    }

}
