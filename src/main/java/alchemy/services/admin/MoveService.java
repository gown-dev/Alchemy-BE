package alchemy.services.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import alchemy.annotations.Logged;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.admin.AdminProcessError;
import alchemy.mappers.ConstraintMapper;
import alchemy.mappers.MoveMapper;
import alchemy.model.MoveDTO;
import alchemy.model.MoveIdentificationDTO;
import alchemy.model.pets.constraints.Constraint;
import alchemy.model.pets.moves.Move;
import alchemy.model.pets.moves.components.MoveComponent;
import alchemy.repositories.MoveRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoveService {

	private final MoveMapper moveMapper;
	private final ConstraintMapper constraintMapper;
	private final MoveRepository moveRepository;

	@Logged("Get All Moves")
	public List<Move> getAllMoves() {
		return moveRepository.findAll();
	}

	@Logged("Create Move")
	@Transactional
	public List<Move> createMove(MoveIdentificationDTO dto) {
		if (moveRepository.existsById(dto.getName())) {
			throw new ProcessException(AdminProcessError.MOVE_CREATION_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
		}

		Move move = Move.builder()
			.name(dto.getName())
			.tags(new ArrayList<>(dto.getTags()))
			.build();

		moveRepository.save(move);

		return getAllMoves();
	}

	@Logged("Delete Move")
	@Transactional
	public List<Move> deleteMove(MoveIdentificationDTO dto) {
		Move move = moveRepository.findById(dto.getName()).orElseThrow(() -> {
			throw new ProcessException(AdminProcessError.MOVE_DELETION_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST);
		});

		moveRepository.delete(move);

		return getAllMoves();
	}

	@Logged("Update Move")
	@Transactional
	public List<Move> updateMove(MoveDTO dto) {
		Move move = moveRepository.findById(dto.getIdentification().getName()).orElseThrow(() -> {
			throw new ProcessException(AdminProcessError.MOVE_UPDATE_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST);
		});

		move.setTags(dto.getIdentification().getTags() != null ?
				new ArrayList<>(dto.getIdentification().getTags()) : Collections.emptyList());
		move.setCooldown(dto.getCooldown() != null ? dto.getCooldown() : 0);
		move.setConstraints(dto.getConstraints().stream()
				.map(constraint -> constraintMapper.toConstraintDTO(constraint))
				.collect(Collectors.toCollection(() -> new ArrayList<Constraint>())));
		move.setComponents(dto.getComponents().stream()
				.map(component -> moveMapper.toMoveComponentEntity(component))
				.collect(Collectors.toCollection(() -> new ArrayList<MoveComponent>())));

		moveRepository.save(move);

		return getAllMoves();
	}

}
